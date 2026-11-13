package com.benkio.chatircadapter.connection

import cats.effect.IO
import cats.effect.Resource

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CopyOnWriteArrayList
import scala.jdk.CollectionConverters.*

/** A server that speaks just enough IRC for a client to register and join channels, on a local port with no TLS. It
  * records the connections and the channels they joined, and it can drop the connection it has now.
  */
final class FakeIrcServer private (server: ServerSocket) {

  private val connectionsCount          = new AtomicInteger(0)
  private val current                   = new AtomicReference[Option[Socket]](None)
  private val joinedChannels            = new CopyOnWriteArrayList[String]()
  private val received                  = new CopyOnWriteArrayList[String]()
  @volatile private var closed: Boolean = false

  def port: Int                   = server.getLocalPort
  def connections: Int            = connectionsCount.get()
  def joined: List[String]        = joinedChannels.asScala.toList
  def linesReceived: List[String] = received.asScala.toList

  /** Closes the connection of the client as a network drop does. */
  def dropConnection: IO[Unit] = IO.blocking(current.get().foreach(_.close()))

  /** Sends a line to the client, as the server would. */
  def push(line: String): IO[Unit] = IO.blocking(current.get().foreach(send(_, line)))

  private def send(socket: Socket, line: String): Unit = {
    val out = socket.getOutputStream
    out.write((line + "\r\n").getBytes("UTF-8"))
    out.flush()
  }

  private def serve(socket: Socket): Unit = {
    var nick: Option[String] = None
    var user: Boolean        = false
    var welcomed: Boolean    = false
    var capEnded: Boolean    = false
    try {
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream, "UTF-8"))
      Iterator.continually(in.readLine()).takeWhile(_ != null).foreach { line =>
        received.add(line)
        val parts = line.split(" ")
        parts(0) match {
          case "CAP" if parts.lift(1).contains("LS")  => send(socket, ":fake CAP * LS :")
          case "CAP" if parts.lift(1).contains("END") => capEnded = true
          case "NICK"                                 => nick = parts.lift(1)
          case "USER"                                 => user = true
          case "PING" => send(socket, s":fake PONG fake :${parts.lift(1).getOrElse("")}")
          case "JOIN" =>
            val channel = parts(1)
            joinedChannels.add(channel)
            send(socket, s":${nick.getOrElse("n")}!n@host JOIN $channel")
            send(socket, s":fake 366 ${nick.getOrElse("n")} $channel :End of /NAMES list.")
          case "QUIT" => socket.close()
          case _      => ()
        }
        if !welcomed && capEnded && user && nick.nonEmpty then {
          welcomed = true
          val n = nick.get
          send(socket, s":fake 001 $n :Welcome")
          // Kitteh takes the registration as complete at the version of the server
          send(socket, s":fake 004 $n fake fake-1.0 iow biklmnopstv")
          send(socket, s":fake 005 $n CHANTYPES=# NETWORK=Fake :are supported by this server")
          send(socket, s":fake 376 $n :End of /MOTD command.")
        }
      }
    } catch { case _: java.io.IOException => () }
    finally if !socket.isClosed then socket.close()
  }

  private def acceptLoop(): Unit =
    while !closed do try {
      val socket = server.accept()
      connectionsCount.incrementAndGet()
      current.set(Some(socket))
      val t = new Thread(() => serve(socket), "fake-irc-connection")
      t.setDaemon(true)
      t.start()
    } catch { case _: java.io.IOException => () }

  private def stop(): Unit = {
    closed = true
    current.get().foreach(_.close())
    server.close()
  }
}

object FakeIrcServer {

  def resource: Resource[IO, FakeIrcServer] =
    Resource.make(IO.blocking {
      val fake = new FakeIrcServer(new ServerSocket(0))
      val t    = new Thread(() => fake.acceptLoop(), "fake-irc-accept")
      t.setDaemon(true)
      t.start()
      fake
    })(fake => IO.blocking(fake.stop()))
}
