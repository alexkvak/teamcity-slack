package com.fpd.teamcity.slack

object Resources {

  case class View(view: String)

  case class Action(url: String) {
    lazy val controllerUrl = withLeadingSlash(url)
  }

  case class Page(url: String, view: String) {
    lazy val controllerUrl = withLeadingSlash(url)
  }

  private def withLeadingSlash(url: String) = {
    if (url.startsWith("/")) url
    else s"/$url"
  }

  lazy val buildPage = View("buildPage.jsp")
  lazy val buildSettingList = Page("app/slackIntegration/buildSettingList.html", "buildSettingListPage.jsp")
  lazy val buildSettingEdit = Page("app/slackIntegration/buildSettingEdit.html", "buildSettingEditPage.jsp")
  lazy val buildSettingSave = Action("app/slackIntegration/buildSettingSave.html")
  lazy val buildSettingDelete = Action("app/slackIntegration/buildSettingDelete.html")
  lazy val buildSettingTry = Action("app/slackIntegration/buildSettingTry.html")
  lazy val configPage = Page("app/slackIntegration/config", "configPage.jsp")
  lazy val ajaxView = View("ajaxView.jsp")
}
