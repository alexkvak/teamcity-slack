package com.fpd.teamcity.slack.pages

import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slack.Helpers
import jetbrains.buildServer.web.openapi.SimplePageExtension

trait SlackExtension extends SimplePageExtension {
  override def isAvailable(request: HttpServletRequest): Boolean = Helpers.checkPermission(request)
}
