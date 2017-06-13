package com.fpd.teamcity.slackNotify.pages

import javax.servlet.http.HttpServletRequest

import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.web.openapi.SimplePageExtension
import jetbrains.buildServer.web.util.SessionUser

trait SlackPage extends SimplePageExtension {
  override def isAvailable(request: HttpServletRequest): Boolean = checkPermission(request, Permission.CHANGE_SERVER_SETTINGS)

  private def checkPermission(request: HttpServletRequest, permission: Permission): Boolean =
    Option(SessionUser.getUser(request)).exists(user ⇒ user.isPermissionGrantedGlobally(permission))
}
