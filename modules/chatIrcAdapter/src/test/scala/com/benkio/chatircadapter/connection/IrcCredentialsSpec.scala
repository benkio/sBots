package com.benkio.chatircadapter.connection

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import java.nio.file.Files

class IrcCredentialsSpec extends CatsEffectSuite {

  private def bot(name: String): SBotInfo = SBotInfo(SBotInfo.SBotId("abc"), SBotInfo.SBotName(name))

  test("the nick is the name of the bot") {
    assertEquals(IrcCredentials.nick(bot("RichardPHJBensonBot")), "RichardPHJBensonBot")
  }

  test("the nick keeps only what a nick can have, does not start with a digit and is at most 30 characters") {
    assertEquals(IrcCredentials.nick(bot("Pino Scotto!Bot")), "PinoScottoBot")
    assertEquals(IrcCredentials.nick(bot("7Bot")), "_7Bot")
    assertEquals(IrcCredentials.nick(bot("a" * 40)).length, 30)
    assertEquals(IrcCredentials.nick(bot("Zoë")), "Zo")
  }

  test("the password is the document <botId>_<botName>_irc.token") {
    assertEquals(IrcCredentials.passwordFilename(bot("TestBot")), "abc_TestBot_irc.token")
  }

  test("the password is read, without the spaces around it") {
    val file = Files.createTempFile("irc", ".token")
    Files.writeString(file, "  secret\n")
    file.toFile.deleteOnExit()
    val repository = new RepositoryMock(getResourceFileHandler =
      _ => IO.pure(Right(NonEmptyList.one(MediaResourceFile[IO](Resource.pure(file)))))
    )
    IrcCredentials.password[IO](bot("TestBot"), repository).use(IO.pure).map(assertEquals(_, Some("secret")))
  }

  test("a bot without a password document connects with no identification") {
    val repository = new RepositoryMock(getResourceFileHandler =
      mediaFile => IO.pure(Left(RepositoryError.NoResourcesFoundFile(mediaFile)))
    )
    IrcCredentials.password[IO](bot("TestBot"), repository).use(IO.pure).map(assertEquals(_, None))
  }
}
