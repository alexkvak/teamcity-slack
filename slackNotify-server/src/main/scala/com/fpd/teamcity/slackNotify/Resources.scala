package com.fpd.teamcity.slackNotify

object Resources {

  // TODO: inherit case classes
  case class View(view: String)

  case class Action(url: String)

  case class Page(url: String, view: String)

  lazy val buildPage = View("buildPage.jsp")
  lazy val buildSettingList = Page("/app/slackNotify/buildSettingList.html", "buildSettingListPage.jsp")
  lazy val buildSettingEdit = Page("/app/slackNotify/buildSettingEdit.html", "buildSettingEditPage.jsp")
  lazy val buildSettingSave = Action("/app/slackNotify/buildSettingSave.html")
  lazy val buildSettingDelete = Action("/app/slackNotify/buildSettingDelete.html")
  lazy val configPage = Page("/app/slackNotify/config", "configPage.jsp")
  lazy val ajaxView = View("ajaxView.jsp")
}
