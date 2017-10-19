package com.fpd.teamcity.slack.pages

import java.util
import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slack.{ConfigManager, PermissionManager, Resources, Strings}
import jetbrains.buildServer.serverSide.{ProjectManager, SBuildType}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}

class BuildPage(
                 manager: WebControllerManager,
                 projectManager: ProjectManager,
                 descriptor: PluginDescriptor,
                 configManager: ConfigManager,
                 val permissionManager: PermissionManager
               )
  extends
    BuildTypeTab(
      Strings.tabId,
      Strings.label,
      manager: WebControllerManager,
      projectManager: ProjectManager,
      descriptor.getPluginResourcesPath(Resources.buildPage.view))
    with SlackExtension {

  addCssFile(descriptor.getPluginResourcesPath("css/slack-notifier.css"))
  addJsFile(descriptor.getPluginResourcesPath("js/slack-notifier.js"))

  override def isAvailable(request: HttpServletRequest): Boolean =  permissionManager.buildAccessPermitted(request, getBuildType(request).getInternalId)

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest, buildType: SBuildType, user: SUser): Unit = {
    model.put("buildTypeId", buildType.getBuildTypeId)
    model.put("buildSettingListUrl", Resources.buildSettingList.url)
    model.put("buildSettingEditUrl", Resources.buildSettingEdit.url)
    model.put("buildSettingSaveUrl", Resources.buildSettingSave.url)
    model.put("buildSettingDeleteUrl", Resources.buildSettingDelete.url)
    model.put("buildSettingTryUrl", Resources.buildSettingTry.url)
  }
}
