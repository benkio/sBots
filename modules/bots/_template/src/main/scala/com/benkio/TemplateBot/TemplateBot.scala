package com.benkio.TemplateBot

import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName

object TemplateBot {

  val sBotInfo: SBotInfo = SBotInfo(SBotId("tpl"), SBotName("TemplateBot"))

}
