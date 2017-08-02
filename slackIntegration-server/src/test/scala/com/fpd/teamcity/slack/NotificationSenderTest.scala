package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag, Config}
import jetbrains.buildServer.serverSide.{Branch, SBuild}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class NotificationSenderTest extends FlatSpec with MockFactory with Matchers {
  private trait Context extends CommonMocks {
    val gateway: SlackGateway = stub[SlackGateway]
    val messageBuilderFactory: MessageBuilderFactory = stub[MessageBuilderFactory]
    val sender = new NotificationSenderStub(manager, gateway, messageBuilderFactory)

    def settingFlags: Set[BuildSettingFlag]
    val setting = BuildSetting("buildTypeId", "my-branch", "", "", settingFlags)
    val build: SBuild = stub[SBuild]
    val branch: Branch = stub[Branch]

    manager.setConfig(Config("", Map("some-key" → setting)))

    branch.getDisplayName _ when() returns setting.branchMask
    build.getBuildTypeId _ when() returns setting.buildTypeId
    build.getBranch _ when() returns branch
  }

  "NotificationSender.prepareSettings" should "return setting if build success" in new Context {
    def settingFlags = Set(BuildSettingFlag.success)

    sender.prepareSettings(build, Set(BuildSettingFlag.success)).toSet shouldEqual Set(setting)
  }

  "NotificationSender.prepareSettings" should "not return setting if build success" in new Context {
    def settingFlags = Set(BuildSettingFlag.success, BuildSettingFlag.failureToSuccess)

    sender.prepareSettings(build, Set(BuildSettingFlag.success)).toSet shouldEqual Set.empty[BuildSetting]
  }

  "NotificationSender.prepareSettings" should "not return setting if build fails" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure, BuildSettingFlag.successToFailure)

    sender.prepareSettings(build, Set(BuildSettingFlag.failure)).toSet shouldEqual Set.empty[BuildSetting]
  }

  "NotificationSender.prepareSettings" should "return setting if build fails" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure)

    sender.prepareSettings(build, Set(BuildSettingFlag.failure)).toSet shouldEqual Set(setting)
  }

  class NotificationSenderStub(val configManager: ConfigManager,
                               val gateway: SlackGateway,
                               val messageBuilderFactory: MessageBuilderFactory
                              ) extends NotificationSender {

  }
}
