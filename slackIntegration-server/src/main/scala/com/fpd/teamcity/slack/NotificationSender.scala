package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.SlackGateway.{SlackChannel, SlackUser}
import jetbrains.buildServer.serverSide.SBuild

trait NotificationSender {

  val configManager: ConfigManager
  val gateway: SlackGateway
  val messageBuilderFactory: MessageBuilderFactory

  import Helpers.Implicits._

  def send(build: SBuild, flags: Set[BuildSettingFlag]): Unit = {
    val settings = prepareSettings(build, flags)

    lazy val emails = build.committees
    lazy val messageBuilder = messageBuilderFactory.createForBuild(build)
    lazy val sendPersonal = shouldSendPersonal(build)

    settings.foreach { setting ⇒
      val attachment = messageBuilder.compile(setting.messageTemplate, Some(setting))
      if (setting.slackChannel.nonEmpty) {
        gateway.sendMessage(SlackChannel(setting.slackChannel), attachment)
      }

      /**
        * if build fails all committees should receive the message
        * if personal notification explicitly enabled in build settings let's notify all committees
        */
      if (setting.notifyCommitter || sendPersonal) {
        emails.foreach { email ⇒
          gateway.sendMessage(SlackUser(email), attachment)
        }
      }
    }
  }

  def shouldSendPersonal(build: SBuild): Boolean = build.getBuildStatus.isFailed && configManager.personalEnabled.exists(x ⇒ x)

  def prepareSettings(build: SBuild, flags: Set[BuildSettingFlag]): Iterable[BuildSetting] =
    configManager.buildSettingList(build.getBuildTypeId).values.filter { x ⇒
      x.pureFlags.intersect(flags).nonEmpty && build.matchBranch(x.branchMask)
    }
}
