package com.benkio.chatcore.http

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.http.MegaClient.MegaEncryptedFileErrorEmptyResponse
import com.benkio.chatcore.Logger.given
import fs2.Chunk
import fs2.Stream
import io.circe.Json
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import munit.CatsEffectSuite
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.syntax.literals.*
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri

import java.nio.file.Files
import java.util.Base64
import scala.concurrent.duration.*

/** The download of a Mega file: decrypted while it arrives, with the memory of a few chunks instead of the whole file.
  */
class MegaClientDownloadSpec extends CatsEffectSuite {

  override val munitIOTimeout: FiniteDuration = 2.minutes

  // MEGA key layout: [k0 k1 k2 k3 n0 n1 m0 m1], 32 bytes
  private val rawKey: Array[Byte] = Array.tabulate(32)(i => (i * 7 + 3).toByte)
  private val megaUrl: Uri        =
    uri"https://mega.nz/file/ebhDGaoY".withFragment(Base64.getUrlEncoder.withoutPadding.encodeToString(rawKey))

  private def encryptCipher: Cipher = {
    val aesKey = Array.tabulate(16)(i => (rawKey(i) ^ rawKey(16 + i)).toByte)
    val iv     = new Array[Byte](16)
    System.arraycopy(rawKey, 16, iv, 0, 8)
    val cipher = Cipher.getInstance("AES/CTR/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv))
    cipher
  }

  private def encrypt(plain: Array[Byte]): Array[Byte] = encryptCipher.doFinal(plain)

  private def content(size: Int): Array[Byte] = Array.tabulate(size)(i => ((i * 31) % 251).toByte)

  /** What Mega answers: the addresses to download from, then the encrypted file in small chunks, like the network. */
  private def megaStub(downloadUris: List[String], answers: Map[String, Either[Status, Array[Byte]]]): Client[IO] =
    Client.fromHttpApp(HttpApp[IO] { request =>
      if request.method == Method.POST then {
        val json = Json.arr(
          Json.obj(
            "s"   -> Json.fromInt(1),
            "at"  -> Json.fromString("x"),
            "msd" -> Json.fromInt(1),
            "g"   -> Json.arr(downloadUris.map(Json.fromString)*),
            "ip"  -> Json.arr(),
            "fh"  -> Json.fromString("fh")
          )
        )
        IO.pure(Response[IO](Status.Ok).withEntity(json))
      } else
        answers.get(request.uri.renderString) match {
          case Some(Right(encrypted)) =>
            val chunks = Stream.emits(encrypted.grouped(16 * 1024).toList.map(Chunk.array(_))).flatMap(Stream.chunk)
            IO.pure(Response[IO](Status.Ok, body = chunks))
          case Some(Left(status)) => IO.pure(Response[IO](status))
          case None               => IO.pure(Response[IO](Status.NotFound))
        }
    })

  private val primary = "https://download.example/primary.bin"
  private val mirror  = "https://download.example/mirror.bin"

  private def fetchedBytes(client: Client[IO], filename: String = "mega_download_test.bin"): IO[Array[Byte]] =
    MegaClient[IO](client).flatMap(_.fetchFile(filename, megaUrl).use(path => IO.blocking(Files.readAllBytes(path))))

  // sizes around the AES block (16 bytes) and the chunk of the network (16 KiB)
  List(1, 15, 16, 17, 16 * 1024 - 1, 16 * 1024, 16 * 1024 + 1, 1024 * 1024 + 13, 3 * 1024 * 1024).foreach { size =>
    test(s"a file of $size bytes is decrypted byte for byte") {
      val plain = content(size)
      fetchedBytes(megaStub(List(primary), Map(primary -> Right(encrypt(plain))))).map(bytes =>
        assert(bytes.sameElements(plain), s"the decrypted content of $size bytes differs")
      )
    }
  }

  test("the next address is used when the first one answers an error") {
    val plain = content(100 * 1024)
    val stub  =
      megaStub(List(primary, mirror), Map(primary -> Left(Status.InternalServerError), mirror -> Right(encrypt(plain))))
    fetchedBytes(stub).map(bytes => assert(bytes.sameElements(plain)))
  }

  test("an error status is not decrypted as if it was the file: with no address left the fetch fails") {
    val stub = megaStub(List(primary, mirror), Map(primary -> Left(Status.Forbidden), mirror -> Left(Status.NotFound)))
    interceptIO[MegaEncryptedFileErrorEmptyResponse](fetchedBytes(stub))
  }

  test("the temporary file of a failed download does not stay on the disk") {
    val before =
      Option(Files.list(java.nio.file.Path.of(System.getProperty("java.io.tmpdir")))).map(_.count()).getOrElse(0L)
    val stub = megaStub(List(primary), Map(primary -> Left(Status.Forbidden)))
    for {
      _     <- interceptIO[MegaEncryptedFileErrorEmptyResponse](fetchedBytes(stub, "leftover_check.bin"))
      names <- IO.blocking(
        Files.list(java.nio.file.Path.of(System.getProperty("java.io.tmpdir"))).toArray.toList.map(_.toString)
      )
    } yield assert(!names.exists(_.contains("leftover_check")), s"a temporary file was left: $names (before: $before)")
  }

  private def usedHeap(): Long = {
    System.gc()
    System.gc()
    val runtime = Runtime.getRuntime
    runtime.totalMemory - runtime.freeMemory
  }

  /** The encrypted file of `size` bytes generated while it is read, never held in memory: `atHalf` runs when half of it
    * was sent.
    */
  private def generatedEncryptedBody(size: Int, atHalf: IO[Unit]): Stream[IO, Byte] = {
    val chunkSize = 16 * 1024
    val chunks    = size / chunkSize
    Stream.suspend {
      val cipher = encryptCipher
      Stream
        .range(0, chunks)
        .evalMap { i =>
          val plain = Array.tabulate(chunkSize)(j => (((i.toLong * chunkSize + j) * 31) % 251).toByte)
          IO(Chunk.array(cipher.update(plain))) <* atHalf.whenA(i == chunks / 2)
        }
        .flatMap(Stream.chunk)
    }
  }

  test("the heap in the middle of a download of 96 MiB holds a few chunks, not the file read so far") {
    val size       = 96 * 1024 * 1024
    val heapAtHalf = new java.util.concurrent.atomic.AtomicLong(-1L)
    val client     = Client.fromHttpApp(HttpApp[IO] { request =>
      if request.method == Method.POST then IO.pure(
        Response[IO](Status.Ok).withEntity(
          Json.arr(
            Json.obj(
              "s"   -> Json.fromInt(1),
              "at"  -> Json.fromString("x"),
              "msd" -> Json.fromInt(1),
              "g"   -> Json.fromString(primary),
              "ip"  -> Json.arr(),
              "fh"  -> Json.fromString("fh")
            )
          )
        )
      )
      else
        IO.pure(Response[IO](Status.Ok, body = generatedEncryptedBody(size, IO(heapAtHalf.set(usedHeap())))))
    })
    for {
      megaClient <- MegaClient[IO](client)
      baseline   <- IO(usedHeap())
      _ <- megaClient.fetchFile("heap_test.bin", megaUrl).use(path => IO(assertEquals(Files.size(path), size.toLong)))
      growth = heapAtHalf.get - baseline
      _ <- IO.println(
        f"[MegaClientDownloadSpec] heap growth at half of a file of ${size / 1048576}%d MiB: ${growth / 1048576.0}%.1f MiB"
      )
    } yield {
      assert(heapAtHalf.get >= 0, "the middle of the download was never reached")
      // reading the whole file in memory would be about 48 MiB here
      assert(growth < size / 4, s"the heap grew of ${growth / 1048576} MiB at half of a file of ${size / 1048576} MiB")
    }
  }
}
