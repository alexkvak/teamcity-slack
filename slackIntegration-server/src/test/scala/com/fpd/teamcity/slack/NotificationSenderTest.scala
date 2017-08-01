package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag, Config}
import jetbrains.buildServer.serverSide.{Branch, SBuild}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class NotificationSenderTest extends FlatSpec with MockFactory with Matchers {
  "NotificationSender.prepareSettings" should "work" in new CommonMocks {
    private val gateway = stub[SlackGateway]
    private val messageBuilderFactory = stub[MessageBuilderFactory]
    private val sender = new NotificationSenderStub(manager, gateway, messageBuilderFactory)
    private val setting = BuildSetting("buildTypeId", "my-branch", "", "", Set(BuildSettingFlag.success))
    manager.setConfig(Config("", Map("some-key" → setting)))

    private val build = stub[SBuild]
    private val branch = stub[Branch]
    branch.getName _ when() returns setting.branchMask
    build.getBuildTypeId _ when() returns setting.buildTypeId
    build.getBranch _ when() returns branch

    sender.prepareSettings(build, setting.flags).toSet shouldEqual Set(setting)
  }

  class NotificationSenderStub(val configManager: ConfigManager,
                               val gateway: SlackGateway,
                               val messageBuilderFactory: MessageBuilderFactory
                              ) extends NotificationSender {

  }
}
