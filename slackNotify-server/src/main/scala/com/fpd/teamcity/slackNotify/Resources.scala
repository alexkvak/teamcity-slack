package com.fpd.teamcity.slackNotify

object Resources {
  case class RegularPage(url: String, view: String)

  lazy val buildContentPage = RegularPage("/app/slackNotify/buildContentPage.html", "buildContentPage.jsp")
  lazy val configPage = RegularPage("/app/slackNotify/config", "configPage.jsp")
}
