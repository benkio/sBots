package com.benkio.main

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

/** The persona of every registered bot is on the classpath of `main`, so the LLM engine can play it. */
class PersonaFilesSpec extends CatsEffectSuite {

  test("every bot of the registry has a persona file that can be loaded") {
    BotRegistry
      .toList(BotRegistry.value)
      .traverse(entry =>
        Persona.load[IO](entry.sBotConfig.personaFilename).map(persona => entry.sBotInfo.botId.value -> persona)
      )
      .map(personas => assertEquals(personas.size, BotRegistry.toList(BotRegistry.value).size))
  }
}
