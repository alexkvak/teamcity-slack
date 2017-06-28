package com.fpd.teamcity.slackNotify

object Resources {

  // TODO: inherit case classes
  case class TabPage(view: String)

  case class ActionPage(url: String)

  case class RegularPage(url: String, view: String)

  lazy val buildPage = TabPage("buildPage.jsp")
  lazy val buildSettingList = RegularPage("/app/slackNotify/buildSettingList.html", "buildSettingListPage.jsp")
  lazy val buildSettingEdit = RegularPage("/app/slackNotify/buildSettingEdit.html", "buildSettingEditPage.jsp")
  lazy val buildSettingSave = ActionPage("/app/slackNotify/buildSettingSave.html")
  lazy val configPage = RegularPage("/app/slackNotify/config", "configPage.jsp")
}
