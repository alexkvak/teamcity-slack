package com.fpd.teamcity.slackNotify.pages

import java.util
import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slackNotify.Strings
import jetbrains.buildServer.serverSide.{ProjectManager, SBuildType}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.WebControllerManager
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab

class BuildPage(manager: WebControllerManager, projectManager: ProjectManager) extends BuildTypeTab(
  Strings.tabId,
  Strings.label,
  manager: WebControllerManager,
  projectManager: ProjectManager,
  BuildPage.includeUrl) {

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest, buildType: SBuildType, user: SUser): Unit = ???
}

object BuildPage {
  private def includeUrl: String = "/buildPage.jsp"
}
