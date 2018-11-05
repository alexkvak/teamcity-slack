package com.fpd.teamcity.slack.pages

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.Helpers.Implicits._
import com.fpd.teamcity.slack.controllers.SlackController
import com.fpd.teamcity.slack.{ConfigManager, PermissionManager, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.springframework.web.servlet.ModelAndView

import scala.collection.JavaConverters._

class BuildSettingListPage(controllerManager: WebControllerManager,
                           descriptor: PluginDescriptor,
                           config: ConfigManager,
                           val permissionManager: PermissionManager,
                           projectManager: ProjectManager
                          ) extends BaseController with SlackController {
  controllerManager.registerController(Resources.buildSettingList.controllerUrl, this)

  override def handle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val view = descriptor.getPluginResourcesPath(Resources.buildSettingList.view)
    new ModelAndView(view, Map("list" → config.buildSettingList(request.param("buildTypeId").get).asJava).asJava)
  }

  override protected def checkPermission(request: HttpServletRequest): Boolean =
    request.param("buildTypeId").exists(id ⇒ permissionManager.buildAccessPermitted(request, id))
}
