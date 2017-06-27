package com.fpd.teamcity.slackNotify

object Resources {
  // TODO: inherit case classes
  case class TabPage(view: String)
  case class RegularPage(url: String, view: String)

  lazy val buildPage = TabPage("buildPage.jsp")
  lazy val buildSettingListPage = RegularPage("/app/slackNotify/buildSettingListPage.html", "buildSettingListPage.jsp")
  lazy val buildSettingEditPage = RegularPage("/app/slackNotify/buildSettingEditPage.html", "buildSettingEditPage.jsp")
  lazy val configPage = RegularPage("/app/slackNotify/config", "configPage.jsp")
}
