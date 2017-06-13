package com.fpd.teamcity.slackNotify.pages

import java.util
import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slackNotify.Strings
import jetbrains.buildServer.serverSide.{ProjectManager, SBuildType}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab

class BuildPage(manager: WebControllerManager, projectManager: ProjectManager, descriptor: PluginDescriptor) extends BuildTypeTab(
  Strings.tabId,
  Strings.label,
  manager: WebControllerManager,
  projectManager: ProjectManager,
  descriptor.getPluginResourcesPath(BuildPage.includeUrl)) with SlackPage {

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest, buildType: SBuildType, user: SUser): Unit = ???
}

object BuildPage {
  private def includeUrl: String = "buildPage.jsp"
}
