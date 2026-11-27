package com.benkio.chatcore.llm

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class PersonaSpec extends CatsEffectSuite {

  test("the persona file of a bot is named after the bot id") {
    val config = SBotConfig.fromSBotInfo(SBotInfo(SBotInfo.SBotId("eng"), SBotInfo.SBotName("EngineBot")))
    assertEquals(config.personaFilename, "eng_persona.md")
  }

  test("the persona is the text of the file in the resources") {
    Persona.load[IO]("eng_persona.md").map(persona => assert(persona.text.contains("EngineBot"), persona.text))
  }

  test("a missing persona file is an error") {
    Persona.load[IO]("missing_persona.md").intercept[Repository.RepositoryError]
  }

  test("a blank persona file is an error") {
    Persona.load[IO]("blank_persona.md").intercept[Persona.EmptyPersona]
  }
}
