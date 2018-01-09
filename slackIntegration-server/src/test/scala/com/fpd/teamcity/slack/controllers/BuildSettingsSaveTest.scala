package com.fpd.teamcity.slack.controllers

import javax.servlet.http.HttpServletRequest

import com.fpd.teamcity.slack.ConfigManager.Config
import com.fpd.teamcity.slack.Strings.BuildSettingsController._
import com.fpd.teamcity.slack.{CommonMocks, PermissionManager, SlackGateway}
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackSession}
import jetbrains.buildServer.web.openapi.{PluginDescriptor, WebControllerManager}
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

class BuildSettingsSaveTest extends FlatSpec with Matchers {

  import BuildSettingsSaveTest._

  "BuildSettingsSave.handleSave" should "save correct input values" in new Context {
    val requestParams = correctRequestParams
    val request = stubRequest(requestParams)

    session.findChannelByName _ when requestParams("slackChannel") returns stub[SlackChannel]
    gateway.sessionByConfig _ when * returns Success(session)

    buildSettingsSave.handleSave(request) shouldEqual ""

    val settingList = manager.allBuildSettingList
    settingList.size shouldEqual 1

    val setting = settingList.head._2
    setting.buildTypeId shouldEqual requestParams("buildTypeId")
    setting.messageTemplate shouldEqual requestParams("messageTemplate")
    setting.slackChannel shouldEqual requestParams("slackChannel")
    setting.branchMask shouldEqual requestParams("branchMask")
  }

  "BuildSettingsSave.handleSave" should "save empty channel name and checked notify committers flag" in new Context {
    val requestParams = correctRequestParams ++ Map(
      "slackChannel" → "",
      "notifyCommitter" → "1"
    )
    val request = stubRequest(requestParams)

    session.findChannelByName _ when requestParams("slackChannel") returns stub[SlackChannel]
    gateway.sessionByConfig _ when * returns Success(session)

    buildSettingsSave.handleSave(request) shouldEqual ""

    val settingList = manager.allBuildSettingList
    settingList.size shouldEqual 1

    val setting = settingList.head._2
    setting.buildTypeId shouldEqual requestParams("buildTypeId")
    setting.messageTemplate shouldEqual requestParams("messageTemplate")
    setting.slackChannel shouldEqual requestParams("slackChannel")
    setting.notifyCommitter shouldEqual true
    setting.branchMask shouldEqual requestParams("branchMask")
  }

  "BuildSettingsSave.handleSave" should "fail in case of missed required params" in new Context {
    forAll(data) { (requestParams: Map[String, String]) ⇒
      buildSettingsSave.handleSave(stubRequest(requestParams)) shouldEqual requirementsError
    }

    def data =
      Table(
        "requestParams", // First tuple defines column names
        // Subsequent tuples define the data
        Map("branchMask" → ".*"),
        Map("buildTypeId" → "MyAwesomeBuildId"),
        Map("messageTemplate" → "Build was done")
      )
  }

  "BuildSettingsSave.handleSave" should "fail in case of channel and notify committers are empty" in new Context {
    val requestParams = correctRequestParams ++ Map("slackChannel" → "")
    val request = stubRequest(requestParams)

    buildSettingsSave.handleSave(request) shouldEqual channelOrNotifyCommitterError
  }

  "BuildSettingsSave.handleSave" should "fail in case of broken branch mask regular expression" in new Context {
    val requestParams = correctRequestParams ++ Map("branchMask" → ".{1,0}")
    val request = stubRequest(requestParams)

    session.findChannelByName _ when requestParams("slackChannel") returns stub[SlackChannel]
    gateway.sessionByConfig _ when * returns Success(session)

    buildSettingsSave.handleSave(request) shouldEqual compileBranchMaskError
  }

  "BuildSettingsSave.handleSave" should "fail in case of broken artifacts mask regular expression" in new Context {
    val requestParams = correctRequestParams ++ Map("artifactsMask" → ".{1,0}")
    val request = stubRequest(requestParams)

    session.findChannelByName _ when requestParams("slackChannel") returns stub[SlackChannel]
    gateway.sessionByConfig _ when * returns Success(session)

    buildSettingsSave.handleSave(request) shouldEqual compileArtifactsMaskError
  }

  "BuildSettingsSave.handleSave" should "fail in case of unknown channel" in new Context {
    val request = stubRequest(correctRequestParams)
    gateway.sessionByConfig _ when * returns Success(session)

    buildSettingsSave.handleSave(request) shouldEqual s"Unable to find channel with name ${correctRequestParams("slackChannel")}"
  }

  "BuildSettingsSave.handleSave" should "fail in case of failed session creation" in new Context {
    val request = stubRequest(correctRequestParams)
    val exception = new Exception("error")
    gateway.sessionByConfig _ when * returns Failure(new Exception("error"))

    buildSettingsSave.handleSave(request) shouldEqual sessionByConfigError(exception.getMessage)
  }
}

object BuildSettingsSaveTest extends MockFactory {
  val correctRequestParams = Map(
    "branchMask" → ".*",
    "slackChannel" → "someChannel",
    "buildTypeId" → "MyAwesomeBuildId",
    "messageTemplate" → "Build was done"
  )

  private trait Context extends CommonMocks {
    val controllerManager: WebControllerManager = stub[WebControllerManager]
    val gateway: SlackGateway = stub[SlackGateway]
    val permissionManager: PermissionManager = stub[PermissionManager]
    implicit private val pluginDescriptor: PluginDescriptor = stub[PluginDescriptor]
    val session = stub[SlackSession]

    manager.setConfig(Config(""))

    val buildSettingsSave = new BuildSettingsSave(manager, controllerManager, gateway, permissionManager, pluginDescriptor)
  }

  def stubRequest(params: Map[String, String]) = {
    val request = stub[HttpServletRequest]
    request.getParameter _ when * onCall { key: String ⇒ params.get(key).orNull }
    request
  }
}
