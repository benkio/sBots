package com.benkio.xahleebot

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.RandomSelection
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomLinkCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogWriter

object CommandRepliesData {

  def values[F[_]: Async](
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      botName: String
  )(implicit
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    RandomLinkCommand.searchShowReplyBundleCommand[F](
      dbShow = dbLayer.dbShow,
      botName = botName,
    ),
    SubscribeUnsubscribeCommand.subscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand[F](
      dbSubscription = dbLayer.dbSubscription,
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("ass"),
      mediafiles = List(
        MediaFile("xah_Asshole.mp3"),
        MediaFile("xah_CCppSucksStonkyAss.mp3"),
        MediaFile("xah_EmacsLispPainAss.mp3"),
        MediaFile("xah_FakingAss.mp3"),
        MediaFile("xah_GuideAss.mp3"),
        MediaFile("xah_IdiotAss.mp3"),
        MediaFile("xah_InstanceMyAss.mp3"),
        MediaFile("xah_MethodYourAss.mp3"),
        MediaFile("xah_MyAss.mp3"),
        MediaFile("xah_MyAss2.mp3"),
        MediaFile("xah_StonkyAssSucks.mp3"),
        MediaFile("xah_SuckLessYourAss-001.mp3"),
        MediaFile("xah_SuckLessYourAss.mp3"),
        MediaFile("xah_YourAss.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("ccpp"),
      mediafiles = List(
        MediaFile("xah_CCppFakhead.mp3"),
        MediaFile("xah_CCppJavaPythonPHPJavascriptWorstFak.mp3"),
        MediaFile("xah_CCppSucksStonkyAss.mp3"),
        MediaFile("xah_CFak.mp3"),
        MediaFile("xah_CFakhead-001.mp3"),
        MediaFile("xah_CFakhead.mp3"),
        MediaFile("xah_CIdiot.mp3"),
        MediaFile("xah_CProgrammersIdiots.mp3"),
        MediaFile("xah_CSyntaxWorstFak.mp3"),
        MediaFile("xah_CUnixCrap.mp3"),
        MediaFile("xah_CUnixIdiots.mp3"),
        MediaFile("xah_CWorstFak.mp3"),
        MediaFile("xah_FakAll.mp3"),
        MediaFile("xah_JavascriptCCppCompilerFak.mp3"),
        MediaFile("xah_LaughCFakhead.mp3"),
        MediaFile("xah_LinuxCCppFakheads.mp3"),
        MediaFile("xah_ProgrammersCCppIdiots.mp3"),
        MediaFile("xah_UnixCIdiot.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("crap"),
      mediafiles = List(
        MediaFile("xah_BitmapCrap.mp3"),
        MediaFile("xah_CUnixCrap.mp3"),
        MediaFile("xah_CommonLispCrap.mp3"),
        MediaFile("xah_Crap-001.mp3"),
        MediaFile("xah_Crap-002.mp3"),
        MediaFile("xah_Crap-003.mp3"),
        MediaFile("xah_Crap-004.mp3"),
        MediaFile("xah_Crap.mp3"),
        MediaFile("xah_Crap2.mp3"),
        MediaFile("xah_Crap3.mp3"),
        MediaFile("xah_Crap4.mp3"),
        MediaFile("xah_Crap5.mp3"),
        MediaFile("xah_Crap6.mp3"),
        MediaFile("xah_Crap7.mp3"),
        MediaFile("xah_CrapShit.mp3"),
        MediaFile("xah_CrapShit2.mp3"),
        MediaFile("xah_DesignFakCrap.mp3"),
        MediaFile("xah_EmacsCalculatorShit.mp3"),
        MediaFile("xah_GiveShit.mp3"),
        MediaFile("xah_IncomprehensibleShit.mp3"),
        MediaFile("xah_IntFloatCrap.mp3"),
        MediaFile("xah_NetscapeCrap.mp3"),
        MediaFile("xah_NodeJSCrapNPMFak.mp3"),
        MediaFile("xah_PythonShit.mp3"),
        MediaFile("xah_Shit-001.mp3"),
        MediaFile("xah_Shit.mp3"),
        MediaFile("xah_ShitCrap.mp3"),
        MediaFile("xah_ThirtyYearsFSFGNURichardStallmanUnixPhilosophyFakheadCrap.mp3"),
        MediaFile("xah_TwitterDramaShit.mp3"),
        MediaFile("xah_UnixCrap.mp3"),
        MediaFile("xah_UnixCrap2.mp3"),
        MediaFile("xah_UnixPhilosophyCrap.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("emacs"),
      mediafiles = List(
        MediaFile("xah_EmacsAlanMackenzieFak.mp3"),
        MediaFile("xah_EmacsCalculatorShit.mp3"),
        MediaFile("xah_EmacsEliteIdiots.mp3"),
        MediaFile("xah_EmacsFak.mp3"),
        MediaFile("xah_EmacsFakhead.mp3"),
        MediaFile("xah_EmacsIdiots.mp3"),
        MediaFile("xah_EmacsLispPainAss.mp3"),
        MediaFile("xah_FakAll.mp3"),
        MediaFile("xah_FakEmacs.mp3"),
        MediaFile("xah_FakRichardStallman.mp3"),
        MediaFile("xah_IdiotsLinuxVimEmacsFanaticts.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("fakhead"),
      mediafiles = List(
        MediaFile("xah_AlanMackenzieFakhead.mp3"),
        MediaFile("xah_CCppFakhead.mp3"),
        MediaFile("xah_CFakhead-001.mp3"),
        MediaFile("xah_CFakhead.mp3"),
        MediaFile("xah_EmacsFakhead-001.mp3"),
        MediaFile("xah_EmacsFakhead.mp3"),
        MediaFile("xah_Fakhead-001.mp3"),
        MediaFile("xah_Fakhead.mp3"),
        MediaFile("xah_JSStandardStyleFakhead.mp3"),
        MediaFile("xah_LaughCFakhead.mp3"),
        MediaFile("xah_LiarsFakhead.mp3"),
        MediaFile("xah_LinuxCCppFakheads.mp3"),
        MediaFile("xah_LinuxIdiotsFakheads.mp3"),
        MediaFile("xah_LinuxUnixFakheads.mp3"),
        MediaFile("xah_MacFanaticsFakhead.mp3"),
        MediaFile("xah_MillennialFakhead.mp3"),
        MediaFile("xah_MillennialIdiotsFakhead.mp3"),
        MediaFile("xah_OpensourceFakhead.mp3"),
        MediaFile("xah_OpensourceFakheads.mp3"),
        MediaFile("xah_ProgrammerUnixFakhead.mp3"),
        MediaFile("xah_PythonFakhead.mp3"),
        MediaFile("xah_PythonFanaticsFakhead.mp3"),
        MediaFile("xah_RacismSocialJusticeFakhead.mp3"),
        MediaFile("xah_RichardStallmanFakhead-001.mp3"),
        MediaFile("xah_RichardStallmanFakhead.mp3"),
        MediaFile("xah_SocialJusticeFakhead-001.mp3"),
        MediaFile("xah_SocialJusticeFakhead.mp3"),
        MediaFile("xah_ThereWasFakhead.mp3"),
        MediaFile("xah_ThirtyYearsFSFGNURichardStallmanUnixPhilosophyFakheadCrap.mp3"),
        MediaFile("xah_UnixFakhead-001.mp3"),
        MediaFile("xah_UnixFakhead.mp3"),
        MediaFile("xah_UnixFakhead2.mp3"),
        MediaFile("xah_UnixHackFakheads.mp3"),
        MediaFile("xah_UnixLinuxFakhead.mp3"),
        MediaFile("xah_UnixLinuxFakhead2.mp3"),
        MediaFile("xah_UnixLinuxOpensourceFakheads.mp3"),
        MediaFile("xah_UnixPhilosophyFakhead-001.mp3"),
        MediaFile("xah_UnixPhilosophyFakhead.mp3"),
        MediaFile("xah_WindowMicrosoftFakhead.mp3"),
        MediaFile("xah_WindowsFakhead.mp3"),
        MediaFile("xah_WriteProgrammingLanguageFakhead.mp3"),
        MediaFile("xah_ZuckerbergFakface.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("fak"),
      mediafiles = List(
        MediaFile("xah_APIFak.mp3"),
        MediaFile("xah_AlanFak.mp3"),
        MediaFile("xah_AltTabFak.mp3"),
        MediaFile("xah_BitmapFak.mp3"),
        MediaFile("xah_BloadedFak.mp3"),
        MediaFile("xah_CCppJavaPythonPHPJavascriptWorstFak.mp3"),
        MediaFile("xah_CFak.mp3"),
        MediaFile("xah_CSyntaxWorstFak.mp3"),
        MediaFile("xah_CWorstFak.mp3"),
        MediaFile("xah_ComplexityFak.mp3"),
        MediaFile("xah_DesignFak.mp3"),
        MediaFile("xah_EmacsAlanMackenzieFak.mp3"),
        MediaFile("xah_EmacsFak.mp3"),
        MediaFile("xah_FacebookGoogleFak.mp3"),
        MediaFile("xah_Fak-001.mp3"),
        MediaFile("xah_Fak.mp3"),
        MediaFile("xah_Fak2.mp3"),
        MediaFile("xah_Fak3.mp3"),
        MediaFile("xah_FakAll.mp3"),
        MediaFile("xah_FakEmacs.mp3"),
        MediaFile("xah_FakFakingIdiocyLinux.mp3"),
        MediaFile("xah_FakGoogle.mp3"),
        MediaFile("xah_FakProgrammersUnix.mp3"),
        MediaFile("xah_FakRichardStallman.mp3"),
        MediaFile("xah_FakVivaldiUnix.mp3"),
        MediaFile("xah_IdiodicFakPython.mp3"),
        MediaFile("xah_FakVivaliOpensourceFakheads.mp3"),
        MediaFile("xah_FakX11.mp3"),
        MediaFile("xah_FakingAss.mp3"),
        MediaFile("xah_FakingLinuxUnixIdiots.mp3"),
        MediaFile("xah_FakingShit.mp3"),
        MediaFile("xah_FakLisp.mp3"),
        MediaFile("xah_FanatismFak-001.mp3"),
        MediaFile("xah_FanatismFak.mp3"),
        MediaFile("xah_FourtyPercentFak-001.mp3"),
        MediaFile("xah_FourtyPercentFak.mp3"),
        MediaFile("xah_GNUNoUnixFak.mp3"),
        MediaFile("xah_GoFakYourself.mp3"),
        MediaFile("xah_GuidoVanRossumFak.mp3"),
        MediaFile("xah_HackerFanatismFak.mp3"),
        MediaFile("xah_IdioticFak.mp3"),
        MediaFile("xah_IteratorEnumenatorOOPPythonFak.mp3"),
        MediaFile("xah_JargonFak.mp3"),
        MediaFile("xah_JavaTutorialWorstFak.mp3"),
        MediaFile("xah_JavascriptCCppCompilerFak.mp3"),
        MediaFile("xah_JavascriptFak.mp3"),
        MediaFile("xah_LaTeXFak.mp3"),
        MediaFile("xah_LaughLinuxFak.mp3"),
        MediaFile("xah_LinuxFakIdiots.mp3"),
        MediaFile("xah_LookAtThisFak.mp3"),
        MediaFile("xah_MarketingFak.mp3"),
        MediaFile("xah_MeaninglessFak.mp3"),
        MediaFile("xah_Memes.mp3"),
        MediaFile("xah_MillennialGenFak.mp3"),
        MediaFile("xah_MillennialsFak.mp3"),
        MediaFile("xah_NetscapeFak.mp3"),
        MediaFile("xah_NoMarketingNoFanatismFak.mp3"),
        MediaFile("xah_NodeJSCrapNPMFak.mp3"),
        MediaFile("xah_OOPFak.mp3"),
        MediaFile("xah_OOPJavaFak.mp3"),
        MediaFile("xah_OhFak.mp3"),
        MediaFile("xah_OneAnnoyingFak.mp3"),
        MediaFile("xah_OpenSourceFanaticsFak.mp3"),
        MediaFile("xah_OpensourceFak.mp3"),
        MediaFile("xah_OperaGoogleAppleFak.mp3"),
        MediaFile("xah_OpersourceFanaticsFak.mp3"),
        MediaFile("xah_PrivateMessageFak.mp3"),
        MediaFile("xah_PythonDocWorstFak.mp3"),
        MediaFile("xah_PythonDocumentationFak.mp3"),
        MediaFile("xah_PythonExtremeIdiocyFak.mp3"),
        MediaFile("xah_RichardStallmanFak.mp3"),
        MediaFile("xah_ShutTheFakUp.mp3"),
        MediaFile("xah_SloppyFak.mp3"),
        MediaFile("xah_ThemeFak.mp3"),
        MediaFile("xah_ThemeFak2.mp3"),
        MediaFile("xah_ThisFak.mp3"),
        MediaFile("xah_ThreadFak.mp3"),
        MediaFile("xah_TwoInchScreenFak.mp3"),
        MediaFile("xah_UnixFak-001.mp3"),
        MediaFile("xah_UnixFak.mp3"),
        MediaFile("xah_UnixFak2.mp3"),
        MediaFile("xah_UnixFak3.mp3"),
        MediaFile("xah_UnixPhilosophyFak.mp3"),
        MediaFile("xah_UnixPhilosophyFak2.mp3"),
        MediaFile("xah_UnixWorstFak-001.mp3"),
        MediaFile("xah_UnixWorstFak.mp3"),
        MediaFile("xah_VivaliFak.mp3"),
        MediaFile("xah_VivaliFakOpensourceFakheads.mp3"),
        MediaFile("xah_WhateverCFak.mp3"),
        MediaFile("xah_WhateverFak.mp3"),
        MediaFile("xah_WindowsUIFak.mp3"),
        MediaFile("xah_WorstFak.mp3"),
        MediaFile("xah_X11Fak.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("idiocy"),
      mediafiles = List(
        MediaFile("xah_BashIdiocy.mp3"),
        MediaFile("xah_CompleteUnixIdiocy.mp3"),
        MediaFile("xah_DesignIdiocy.mp3"),
        MediaFile("xah_ExtremeIdiocy-001.mp3"),
        MediaFile("xah_ExtremeIdiocy.mp3"),
        MediaFile("xah_ExtremeIdiocy2.mp3"),
        MediaFile("xah_ExtremeIdiocy3.mp3"),
        MediaFile("xah_ExtremeIdiocy4.mp3"),
        MediaFile("xah_ExtremeIndustryIdiocy.mp3"),
        MediaFile("xah_FakFakingIdiocyLinux.mp3"),
        MediaFile("xah_FakingIdiocy.mp3"),
        MediaFile("xah_FakingIdiocy2.mp3"),
        MediaFile("xah_Idiocy.mp3"),
        MediaFile("xah_Idiocy2.mp3"),
        MediaFile("xah_LinuxIdiocy.mp3"),
        MediaFile("xah_OpensourceIdiocy.mp3"),
        MediaFile("xah_OperaIdiocy.mp3"),
        MediaFile("xah_PatientIdiocy.mp3"),
        MediaFile("xah_PythonExtremeIdiocyFak.mp3"),
        MediaFile("xah_PythonIdiocy.mp3"),
        MediaFile("xah_UnixIdiocy.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("idiots"),
      mediafiles = List(
        MediaFile("xah_AlanMackenzieIdiot-001.mp3"),
        MediaFile("xah_AlanMackenzieIdiot.mp3"),
        MediaFile("xah_AllIdiots.mp3"),
        MediaFile("xah_AppleIdiots.mp3"),
        MediaFile("xah_CIdiot.mp3"),
        MediaFile("xah_CProgrammersIdiots.mp3"),
        MediaFile("xah_CUnixIdiots.mp3"),
        MediaFile("xah_EmacsEliteIdiots.mp3"),
        MediaFile("xah_EmacsIdiots.mp3"),
        MediaFile("xah_ExtremeIdiot.mp3"),
        MediaFile("xah_ExtremeIdiots.mp3"),
        MediaFile("xah_FakingIdiots.mp3"),
        MediaFile("xah_FakingLinuxUnixIdiots.mp3"),
        MediaFile("xah_FirefoxIdiots.mp3"),
        MediaFile("xah_FloatPointersIdiots.mp3"),
        MediaFile("xah_GuidoVanRossumIdiot.mp3"),
        MediaFile("xah_GuidoVanRossumIdiot2.mp3"),
        MediaFile("xah_HackerTypesIdiots.mp3"),
        MediaFile("xah_Idiot-001.mp3"),
        MediaFile("xah_Idiot-002.mp3"),
        MediaFile("xah_Idiot.mp3"),
        MediaFile("xah_Idiot2.mp3"),
        MediaFile("xah_IdiotAss.mp3"),
        MediaFile("xah_Idiots-001.mp3"),
        MediaFile("xah_Idiots.mp3"),
        MediaFile("xah_Idiots2.mp3"),
        MediaFile("xah_Idiots3.mp3"),
        MediaFile("xah_Idiots4.mp3"),
        MediaFile("xah_Idiots5.mp3"),
        MediaFile("xah_Idiots6.mp3"),
        MediaFile("xah_IdiotsLinuxVimEmacsFanaticts.mp3"),
        MediaFile("xah_LaughIdiots.mp3"),
        MediaFile("xah_LinuxFakIdiots.mp3"),
        MediaFile("xah_LinuxIdiots.mp3"),
        MediaFile("xah_LinuxIdiots2.mp3"),
        MediaFile("xah_LinuxIdiots3.mp3"),
        MediaFile("xah_LinuxIdiots4.mp3"),
        MediaFile("xah_LinuxMostIdiots.mp3"),
        MediaFile("xah_LinuxUnixIdiots.mp3"),
        MediaFile("xah_MemesIdiot.mp3"),
        MediaFile("xah_MillenGenIdiots.mp3"),
        MediaFile("xah_MillennialGenIdiots.mp3"),
        MediaFile("xah_MillennialGenIdiots2.mp3"),
        MediaFile("xah_MillennialGenerationIdiots.mp3"),
        MediaFile("xah_MillennialIdiot-001.mp3"),
        MediaFile("xah_MillennialIdiot-002.mp3"),
        MediaFile("xah_MillennialIdiot.mp3"),
        MediaFile("xah_MillennialIdiots-001.mp3"),
        MediaFile("xah_MillennialIdiots.mp3"),
        MediaFile("xah_MillennialIdiotsFakhead.mp3"),
        MediaFile("xah_MillennialsGenIdiots3.mp3"),
        MediaFile("xah_NerdIdiots.mp3"),
        MediaFile("xah_NerdsIdiot.mp3"),
        MediaFile("xah_PlankIdiots.mp3"),
        MediaFile("xah_ProgrammerIdiots.mp3"),
        MediaFile("xah_ProgrammerIdiots2.mp3"),
        MediaFile("xah_ProgrammersCCppIdiots.mp3"),
        MediaFile("xah_PythonFanaticsIdiot.mp3"),
        MediaFile("xah_PythonIdiots-001.mp3"),
        MediaFile("xah_PythonIdiots.mp3"),
        MediaFile("xah_TechnoIdiots.mp3"),
        MediaFile("xah_UnixCIdiot.mp3"),
        MediaFile("xah_UnixIdiots-001.mp3"),
        MediaFile("xah_UnixIdiots.mp3"),
        MediaFile("xah_UnixIdiots2.mp3"),
        MediaFile("xah_UnixLinuxIdiots.mp3"),
        MediaFile("xah_UnixWindowsIdiots.mp3"),
        MediaFile("xah_VimIdiot.mp3"),
        MediaFile("xah_WhatIdiot.mp3"),
        MediaFile("xah_XahTrollIdiot.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("laugh"),
      mediafiles = List(
        MediaFile("xah_Laugh-001.mp3"),
        MediaFile("xah_Laugh-002.mp3"),
        MediaFile("xah_Laugh-003.mp3"),
        MediaFile("xah_Laugh-004.mp3"),
        MediaFile("xah_Laugh-005.mp3"),
        MediaFile("xah_Laugh-006.mp3"),
        MediaFile("xah_Laugh-007.mp3"),
        MediaFile("xah_Laugh-008.mp3"),
        MediaFile("xah_Laugh-009.mp3"),
        MediaFile("xah_Laugh-010.mp3"),
        MediaFile("xah_Laugh.mp3"),
        MediaFile("xah_Laugh10.mp3"),
        MediaFile("xah_Laugh11.mp3"),
        MediaFile("xah_Laugh12.mp3"),
        MediaFile("xah_Laugh13.mp3"),
        MediaFile("xah_Laugh14.mp3"),
        MediaFile("xah_Laugh15.mp3"),
        MediaFile("xah_Laugh16.mp3"),
        MediaFile("xah_Laugh2.mp3"),
        MediaFile("xah_Laugh3.mp3"),
        MediaFile("xah_Laugh4.mp3"),
        MediaFile("xah_Laugh5.mp3"),
        MediaFile("xah_Laugh6.mp3"),
        MediaFile("xah_Laugh7.mp3"),
        MediaFile("xah_Laugh8.mp3"),
        MediaFile("xah_Laugh9.mp3"),
        MediaFile("xah_LaughCFakhead.mp3"),
        MediaFile("xah_LaughIdiots.mp3"),
        MediaFile("xah_LaughLinuxFak.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("linux"),
      mediafiles = List(
        MediaFile("xah_FakFakingIdiocyLinux.mp3"),
        MediaFile("xah_FakingLinuxUnixIdiots.mp3"),
        MediaFile("xah_IdiotsLinuxVimEmacsFanaticts.mp3"),
        MediaFile("xah_LaughLinuxFak.mp3"),
        MediaFile("xah_LinuxCCppFakheads.mp3"),
        MediaFile("xah_LinuxFakIdiots.mp3"),
        MediaFile("xah_LinuxIdiocy.mp3"),
        MediaFile("xah_LinuxIdiots.mp3"),
        MediaFile("xah_LinuxIdiots2.mp3"),
        MediaFile("xah_LinuxIdiots3.mp3"),
        MediaFile("xah_LinuxIdiots4.mp3"),
        MediaFile("xah_LinuxIdiotsFakheads.mp3"),
        MediaFile("xah_LinuxMostIdiots.mp3"),
        MediaFile("xah_LinuxUnixFakheads.mp3"),
        MediaFile("xah_LinuxUnixFakheads2.mp3"),
        MediaFile("xah_LinuxUnixIdiots.mp3"),
        MediaFile("xah_UnixLinuxFakhead.mp3"),
        MediaFile("xah_UnixLinuxFakhead2.mp3"),
        MediaFile("xah_UnixLinuxIdiots.mp3"),
        MediaFile("xah_UnixLinuxOpensourceFakheads.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("millennial"),
      mediafiles = List(
        MediaFile("xah_MillenGenIdiots.mp3"),
        MediaFile("xah_MillenialGenerationFak.mp3"),
        MediaFile("xah_MillennialFakhead.mp3"),
        MediaFile("xah_MillennialGenFak.mp3"),
        MediaFile("xah_MillennialGenIdiots.mp3"),
        MediaFile("xah_MillennialGenIdiots2.mp3"),
        MediaFile("xah_MillennialGenerationIdiots.mp3"),
        MediaFile("xah_MillennialIdiot-001.mp3"),
        MediaFile("xah_MillennialIdiot-002.mp3"),
        MediaFile("xah_MillennialIdiot.mp3"),
        MediaFile("xah_MillennialIdiots-001.mp3"),
        MediaFile("xah_MillennialIdiots.mp3"),
        MediaFile("xah_MillennialIdiotsFakhead.mp3"),
        MediaFile("xah_MillennialsFak.mp3"),
        MediaFile("xah_MillennialsGenIdiots3.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("opensource"),
      mediafiles = List(
        MediaFile("xah_FakVivaliOpensourceFakheads.mp3"),
        MediaFile("xah_OpenSourceFak.mp3"),
        MediaFile("xah_OpenSourceFak2.mp3"),
        MediaFile("xah_OpenSourceFanaticsFak.mp3"),
        MediaFile("xah_OpensourceFakhead.mp3"),
        MediaFile("xah_OpensourceFakheads.mp3"),
        MediaFile("xah_OpensourceIdiocy.mp3"),
        MediaFile("xah_OpersourceFanaticsFak.mp3"),
        MediaFile("xah_ThirtyYearsFSFGNURichardStallmanUnixPhilosophyFakheadCrap.mp3"),
        MediaFile("xah_UnixLinuxOpensourceFakheads.mp3"),
        MediaFile("xah_VivaliFakOpensourceFakheads.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("python"),
      mediafiles = List(
        MediaFile("xah_CCppJavaPythonPHPJavascriptWorstFak.mp3"),
        MediaFile("xah_FakAll.mp3"),
        MediaFile("xah_GuidoVanRossumFak.mp3"),
        MediaFile("xah_GuidoVanRossumIdiot.mp3"),
        MediaFile("xah_GuidoVanRossumIdiot2.mp3"),
        MediaFile("xah_IdiodicFakPython.mp3"),
        MediaFile("xah_IteratorEnumenatorOOPPythonFak.mp3"),
        MediaFile("xah_PythonDocWorstFak.mp3"),
        MediaFile("xah_PythonDocumentationFak.mp3"),
        MediaFile("xah_PythonDocumentationSucks.mp3"),
        MediaFile("xah_PythonExtremeIdiocyFak.mp3"),
        MediaFile("xah_PythonFakhead.mp3"),
        MediaFile("xah_PythonFanaticsFakhead.mp3"),
        MediaFile("xah_PythonFanaticsIdiot.mp3"),
        MediaFile("xah_PythonIdiocy.mp3"),
        MediaFile("xah_PythonIdiots-001.mp3"),
        MediaFile("xah_PythonIdiots.mp3"),
        MediaFile("xah_PythonShit.mp3"),
        MediaFile("xah_WhyPythonSucks.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("rantcompilation"),
      mediafiles = List(
        MediaFile("Xah3.mp3"),
        MediaFile("xah.mp3"),
        MediaFile("xah2.mp3"),
        MediaFile("xah4.mp3"),
        MediaFile("xah5.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("sucks"),
      mediafiles = List(
        MediaFile("xah_CCppSucksStonkyAss.mp3"),
        MediaFile("xah_ListComprehensionSucks.mp3"),
        MediaFile("xah_MacSucks.mp3"),
        MediaFile("xah_OperaSucks.mp3"),
        MediaFile("xah_PythonDocumentationSucks.mp3"),
        MediaFile("xah_StonkyAssSucks.mp3"),
        MediaFile("xah_SuckLessYourAss-001.mp3"),
        MediaFile("xah_SuckLessYourAss.mp3"),
        MediaFile("xah_WhyPythonSucks.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("unix"),
      mediafiles = List(
        MediaFile("xah_CUnixCrap.mp3"),
        MediaFile("xah_CUnixIdiots.mp3"),
        MediaFile("xah_CompleteUnixIdiocy.mp3"),
        MediaFile("xah_FakAll.mp3"),
        MediaFile("xah_FakProgrammersUnix.mp3"),
        MediaFile("xah_FakVivaldiUnix.mp3"),
        MediaFile("xah_FakingLinuxUnixIdiots.mp3"),
        MediaFile("xah_GNUNoUnixFak.mp3"),
        MediaFile("xah_LinuxUnixFakheads.mp3"),
        MediaFile("xah_LinuxUnixFakheads2.mp3"),
        MediaFile("xah_LinuxUnixIdiots.mp3"),
        MediaFile("xah_ProgrammerUnixFakhead.mp3"),
        MediaFile("xah_ThirtyYearsFSFGNURichardStallmanUnixPhilosophyFakheadCrap.mp3"),
        MediaFile("xah_UnixCIdiot.mp3"),
        MediaFile("xah_UnixCrap.mp3"),
        MediaFile("xah_UnixCrap2.mp3"),
        MediaFile("xah_UnixFak-001.mp3"),
        MediaFile("xah_UnixFak.mp3"),
        MediaFile("xah_UnixFak2.mp3"),
        MediaFile("xah_UnixFak3.mp3"),
        MediaFile("xah_UnixFakhead-001.mp3"),
        MediaFile("xah_UnixFakhead.mp3"),
        MediaFile("xah_UnixHackFakheads.mp3"),
        MediaFile("xah_UnixIdiocy.mp3"),
        MediaFile("xah_UnixIdiots-001.mp3"),
        MediaFile("xah_UnixIdiots.mp3"),
        MediaFile("xah_UnixIdiots2.mp3"),
        MediaFile("xah_UnixLinuxFakhead.mp3"),
        MediaFile("xah_UnixLinuxFakhead2.mp3"),
        MediaFile("xah_UnixLinuxIdiots.mp3"),
        MediaFile("xah_UnixLinuxOpensourceFakheads.mp3"),
        MediaFile("xah_UnixPhilosophyCrap.mp3"),
        MediaFile("xah_UnixPhilosophyDispise.mp3"),
        MediaFile("xah_UnixPhilosophyFak-001.mp3"),
        MediaFile("xah_UnixPhilosophyFak.mp3"),
        MediaFile("xah_UnixPhilosophyFakhead-001.mp3"),
        MediaFile("xah_UnixPhilosophyFakhead.mp3"),
        MediaFile("xah_UnixWindowsIdiots.mp3"),
        MediaFile("xah_UnixWorstFak-001.mp3"),
        MediaFile("xah_UnixWorstFak.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("wtf"),
      mediafiles = List(
        MediaFile("xah_NoPictureWhatTheFak.mp3"),
        MediaFile("xah_WhatTheFak-001.mp3"),
        MediaFile("xah_WhatTheFak-002.mp3"),
        MediaFile("xah_WhatTheFak-003.mp3"),
        MediaFile("xah_WhatTheFak-004.mp3"),
        MediaFile("xah_WhatTheFak.mp3"),
        MediaFile("xah_WhatTheFak2.mp3"),
        MediaFile("xah_WhatTheFak3.mp3"),
        MediaFile("xah_WhatTheFak4.mp3"),
        MediaFile("xah_WhatTheFak5.mp3"),
        MediaFile("xah_WhatTheFak6.mp3"),
        MediaFile("xah_WhatTheFak7.mp3"),
        MediaFile("xah_WhyTheFak.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("extra"),
      mediafiles = List(MediaFile("xah_JesusOMG.mp3")),
      replySelection = RandomSelection
    ),
  )
}