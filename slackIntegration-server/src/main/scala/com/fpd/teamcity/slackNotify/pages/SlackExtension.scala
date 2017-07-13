package com.fpd.teamcity.slackNotify.pages

import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slackNotify.Helpers
import jetbrains.buildServer.web.openapi.SimplePageExtension

trait SlackExtension extends SimplePageExtension {
  override def isAvailable(request: HttpServletRequest): Boolean = Helpers.checkPermission(request)
}
