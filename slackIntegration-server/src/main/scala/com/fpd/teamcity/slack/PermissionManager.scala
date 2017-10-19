package com.fpd.teamcity.slack

import javax.servlet.http.HttpServletRequest

import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.util.SessionUser

import scala.collection.JavaConverters._
import scala.language.implicitConversions

class PermissionManager(
                         projectManager: ProjectManager,
                         configManager: ConfigManager
                       ) {

  import PermissionManager._

  def accessPermitted(request: Request): Boolean = configManager.isAvailable && isAdmin(request)

  def settingAccessPermitted(request: Request, settingId: String): Boolean = configManager.isAvailable &&
    (isAdmin(request) || request.exists(settingIdPermitted(_, settingId)))

  def buildAccessPermitted(request: Request, buildTypeId: String): Boolean = configManager.isAvailable &&
    (isAdmin(request) || request.exists(buildTypeIdPermitted(_, buildTypeId)))

  private def isAdmin(request: Request): Boolean =
    request.exists(_.isPermissionGrantedGlobally(Permission.CHANGE_SERVER_SETTINGS))

  private def settingIdPermitted(user: SUser, settingId: String): Boolean = configManager.buildSetting(settingId)
    .map(_.buildTypeId)
    .exists(buildTypeIdPermitted(user, _))

  private def buildTypeIdPermitted(user: SUser, buildTypeId: String): Boolean =
    Some(projectManager.findProjectId(buildTypeId)).exists(isProjectAdmin(user, _))

  private def isProjectAdmin(user: SUser, projectId: String): Boolean = {
    lazy val parents = projectManager.findProjectById(projectId).getProjectPath.asScala.map(_.getProjectId)

    user.getRoles.asScala.exists { entry ⇒
      entry.getRole.getId == "PROJECT_ADMIN" && parents.contains(entry.getScope.getProjectId)
    }
  }
}

object PermissionManager {
  type Request = HttpServletRequest

  implicit def requestToUser(request: HttpServletRequest): Option[SUser] = Option(SessionUser.getUser(request))
}