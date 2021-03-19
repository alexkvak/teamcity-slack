package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.Helpers.Request
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.{Permission, RoleEntry}
import jetbrains.buildServer.users.SUser

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

class PermissionManager(
    projectManager: ProjectManager,
    configManager: ConfigManager
) {

  def accessPermitted(request: Request): Boolean = isAdmin(request)

  def settingAccessPermitted(request: Request, settingId: String): Boolean =
    configManager.isAvailable &&
      (isAdmin(request) || request.exists(settingIdPermitted(_, settingId)))

  def buildAccessPermitted(request: Request, buildTypeId: String): Boolean =
    configManager.isAvailable &&
      (isAdmin(request) || request.exists(buildTypeIdPermitted(_, buildTypeId)))

  private def isAdmin(request: Request): Boolean =
    request.exists(
      _.isPermissionGrantedGlobally(Permission.CHANGE_SERVER_SETTINGS)
    )

  private def settingIdPermitted(user: SUser, settingId: String): Boolean =
    configManager
      .buildSetting(settingId)
      .map(_.buildTypeId)
      .exists(buildTypeIdPermitted(user, _))

  private def buildTypeIdPermitted(user: SUser, buildTypeId: String): Boolean =
    Some(projectManager.findProjectId(buildTypeId))
      .exists(isProjectAdmin(user, _))

  private def isProjectAdmin(user: SUser, projectId: String): Boolean = {
    lazy val parentProjects = projectManager
      .findProjectById(projectId)
      .getProjectPath
      .asScala
      .map(_.getProjectId)

    lazy val directRoles = user.getRoles.asScala
    lazy val parentRoles =
      user.getParentHolders.asScala.flatMap(_.getRoles.asScala)

    def projectAdmin(entry: RoleEntry): Boolean =
      entry.getRole.getId == "PROJECT_ADMIN" && parentProjects.contains(
        entry.getScope.getProjectId
      )

    directRoles.exists(projectAdmin) || parentRoles.exists(projectAdmin)
  }
}
