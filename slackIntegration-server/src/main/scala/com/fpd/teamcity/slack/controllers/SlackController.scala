package com.fpd.teamcity.slack.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slack.{PermissionManager, Resources}
import jetbrains.buildServer.controllers.{BaseController, SimpleView}
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.web.servlet.ModelAndView

trait SlackController extends BaseController {
  protected val permissionManager: PermissionManager

  def handle(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): ModelAndView

  override def doHandle(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): ModelAndView =
    if (checkPermission(request))
      handle(request, response)
    else
      SimpleView.createTextView("Access denied")

  def ajaxView(
      message: String
  )(implicit descriptor: PluginDescriptor): ModelAndView = {
    val modelAndView = new ModelAndView(
      descriptor.getPluginResourcesPath(Resources.ajaxView.view)
    )
    modelAndView.getModel.put("message", message)
    modelAndView
  }

  protected def checkPermission(request: HttpServletRequest): Boolean =
    permissionManager.accessPermitted(request)
}
