package com.fpd.teamcity.slack.pages

import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slack.PermissionManager
import jetbrains.buildServer.web.openapi.SimplePageExtension

trait SlackExtension extends SimplePageExtension {
  protected val permissionManager: PermissionManager

  override def isAvailable(request: HttpServletRequest): Boolean =
    permissionManager.accessPermitted(request)
}
