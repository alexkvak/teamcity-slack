package com.fpd.teamcity.slackNotify.controllers

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fpd.teamcity.slackNotify.ConfigManager.BuildSetting
import com.fpd.teamcity.slackNotify.{ConfigManager, Resources}
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView

import scala.util.Try

class BuildSettingsSave(configManager: ConfigManager, controllerManager: WebControllerManager) extends BaseController {

  import com.fpd.teamcity.slackNotify.Helpers._

  controllerManager.registerController(Resources.buildSettingSave.url, this)

  override def doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {

    val result = for {
      branchMask ← request.param("branchMask")
      slackChannel ← request.param("slackChannel")
      result ← configManager.updateBuildSetting(BuildSetting(branchMask, slackChannel), request.param("key")) if Try(branchMask.r).isSuccess
    } yield result

    simpleView(result.filter(_ == true).map(_ ⇒ "") getOrElse "Something went wrong")
  }
}
