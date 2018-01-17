package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.ConfigManager.{BuildSetting, BuildSettingFlag, Config}
import com.fpd.teamcity.slack.SlackGateway.{SlackAttachment, SlackChannel, SlackUser}
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{Branch, SBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.vcs.SVcsModification
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class NotificationSenderTest extends FlatSpec with MockFactory with Matchers {
  private trait Context extends CommonMocks {
    val gateway: SlackGateway = stub[SlackGateway]
    val messageBuilderFactory: MessageBuilderFactory = stub[MessageBuilderFactory]

    val builder = stub[MessageBuilder]
    builder.compile _ when(*, *) returns SlackAttachment("", "")
    messageBuilderFactory.createForBuild _ when * returns builder

    val sender = new NotificationSenderStub(manager, gateway, messageBuilderFactory)

    def settingFlags: Set[BuildSettingFlag]
    val channelName = "general"
    val setting = BuildSetting("buildTypeId", "my-branch", channelName, "", settingFlags)
    val build: SBuild = stub[SBuild]
    val branch: Branch = stub[Branch]

    manager.setConfig(Config("", Map("some-key" → setting)))

    branch.getDisplayName _ when() returns setting.branchMask
    build.getBuildTypeId _ when() returns setting.buildTypeId
    build.getBranch _ when() returns branch
    build.getContainingChanges _ when() returns List.empty[SVcsModification].asJava
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

  "NotificationSender.shouldSendPersonal" should "return true" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure)
    manager.setConfig(manager.config.get.copy(personalEnabled = Some(true)))
    build.getBuildStatus _ when() returns Status.FAILURE

    sender.shouldSendPersonal(build) shouldEqual true
  }

  "NotificationSender.shouldSendPersonal" should "return false when personalEnabled is false" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure)
    manager.setConfig(manager.config.get.copy(personalEnabled = Some(false)))
    build.getBuildStatus _ when() returns Status.FAILURE

    sender.shouldSendPersonal(build) shouldEqual false
  }

  "NotificationSender.shouldSendPersonal" should "return false when build is success" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure)
    build.getBuildStatus _ when() returns Status.NORMAL

    sender.shouldSendPersonal(build) shouldEqual false
  }

  "NotificationSender.send" should "send message to channel for non-personal build" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure)
    build.getBuildStatus _ when() returns Status.FAILURE
    build.isPersonal _ when() returns false

    gateway.sendMessage _ when(*, *) returns None
    sender.send(build, Set(BuildSettingFlag.failure)) shouldEqual Map(SlackChannel(channelName).toString → None)
  }

  "NotificationSender.send" should "send private message to build's owner for personal build" in new Context {
    def settingFlags = Set(BuildSettingFlag.failure)
    build.getBuildStatus _ when() returns Status.FAILURE
    build.isPersonal _ when() returns true

    val email = "some@email.com"
    val user = stub[SUser]
    user.getEmail _ when() returns email
    build.getOwner _ when() returns user

    gateway.sendMessage _ when(*, *) returns None
    sender.send(build, Set(BuildSettingFlag.failure)) shouldEqual Map(SlackUser(email).toString → None)
  }

  class NotificationSenderStub(val configManager: ConfigManager,
                               val gateway: SlackGateway,
                               val messageBuilderFactory: MessageBuilderFactory
                              ) extends NotificationSender {

  }
}
