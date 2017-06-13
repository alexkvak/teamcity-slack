package com.fpd.teamcity.slackNotify.pages

import java.util
import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slackNotify.Strings
import jetbrains.buildServer.serverSide.{ProjectManager, SProject}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.{PagePlaces, PluginDescriptor}
import jetbrains.buildServer.web.openapi.project.ProjectTab

class ProjectPage(pagePlaces: PagePlaces, projectManager: ProjectManager, descriptor: PluginDescriptor) extends
  ProjectTab(
    Strings.tabId,
    Strings.label,
    pagePlaces: PagePlaces,
    projectManager: ProjectManager,
    descriptor.getPluginResourcesPath(ProjectPage.includeUrl)) {

  override def fillModel(model: util.Map[String, AnyRef], request: HttpServletRequest, project: SProject, user: SUser): Unit = ???
}

object ProjectPage {
  private def includeUrl: String = "projectPage.jsp"
}
