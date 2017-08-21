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
    val sendPersonal = build.getBuildStatus.isFailed

    settings.foreach { setting ⇒
      val attachment = messageBuilder.compile(setting.messageTemplate, Some(setting))
      gateway.sendMessage(SlackChannel(setting.slackChannel), attachment)

      // if build failed all committees should receive the message
      if (sendPersonal) {
        emails.foreach { email ⇒
          gateway.sendMessage(SlackUser(email), attachment)
        }
      }
    }
  }

  def prepareSettings(build: SBuild, flags: Set[BuildSettingFlag]): Iterable[BuildSetting] =
    configManager.buildSettingList(build.getBuildTypeId).values.filter { x ⇒
      x.pureFlags.intersect(flags).nonEmpty && build.matchBranch(x.branchMask)
    }
}
