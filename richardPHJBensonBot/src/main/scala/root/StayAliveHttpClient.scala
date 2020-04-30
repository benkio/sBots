package root

import cats.effect._
import cats.implicits._
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.duration._

object StayAliveHttpClient {

  implicit val global           = scala.concurrent.ExecutionContext.global
  implicit val contextShift     = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  def stayAlive: IO[Unit] =
    (for {
      x <- BlazeClientBuilder[IO](global).resource
        .use(_.expect[String](Uri.uri("https://yesno.wtf/api")))
      _ = println(x)
      _ <- IO.sleep(15.minute)(timer)
    } yield ()).foreverM
}
