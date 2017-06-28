package com.fpd.teamcity.slackNotify

import javax.servlet.http.HttpServletRequest

object Helpers {
  implicit class RichHttpServletRequest(request: HttpServletRequest) {
    def param(key: String): Option[String] = Option(request.getParameter(key)).map(_.trim).filterNot(_.isEmpty)
  }
}
