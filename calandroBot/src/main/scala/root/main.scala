package root

import telegramium.bots.high._
import telegramium.bots.high.implicits._
import org.http4s.client.blaze._
import org.http4s.client._
import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import com.benkio.telegramBotInfrastructure.Configurations

object main extends IOApp with Configurations {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    BlazeClientBuilder[IO](global).resource
      .use { client =>
        implicit val api: Api[IO] = BotApi(client, baseUrl = s"https://api.telegram.org/bot$token")
        new CalandroBot[IO].start()
      }
      .as(ExitCode.Success)
}
