package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag
import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.SlackGateway.{SlackChannel, SlackUser}
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{BuildServerAdapter, SBuildServer, SRunningBuild}

import scala.collection.JavaConverters._

class SlackServerAdapter(sBuildServer: SBuildServer,
                         configManager: ConfigManager,
                         gateway: SlackGateway
                        ) extends BuildServerAdapter {

  import SlackServerAdapter._

  sBuildServer.addListener(this)

  private def notify(build: SRunningBuild, flags: Set[BuildSettingFlag]): Unit = {
    def matchBranch(mask: String) = Option(build.getBranch).map(branch ⇒ mask.r.findFirstIn(branch.getName)).isDefined

    val settings = configManager.buildSettingList(build.getBuildTypeId).values.filter { x ⇒
      x.flags.intersect(flags).nonEmpty && matchBranch(x.branchMask)
    }

    settings.foreach { setting ⇒
      val message = generateMessage(build)
      gateway.sendMessage(SlackChannel(setting.slackChannel), message)

      // if build failed all committees should receive the message
      if (build.getBuildStatus.isFailed) {
        val committees = build.getContainingChanges.asScala.flatMap(change ⇒ change.getCommitters.asScala).toSet
        val emails = committees.map(user ⇒ Option(user.getEmail).getOrElse("")).filter(_.length > 0)
        emails.map(SlackUser).foreach(gateway.sendMessage(_, message))
      }
    }
  }

  private def generateMessage(build: SRunningBuild): String = {
    val status = if (build.getBuildStatus.isSuccessful) {
      "succeeded"
    } else {
      "failed"
    }

    s"${build.getFullName} #${build.getBuildId} $status"
  }

  override def buildFinished(build: SRunningBuild): Unit = {
    val previousStatus = sBuildServer.getHistory.getEntriesBefore(build, false).asScala
      .find(!_.isPersonal)
      .map(_.getBuildStatus)
      .getOrElse(Status.UNKNOWN)

    val flags = calculateFlags(previousStatus, build.getBuildStatus)
    if (flags.nonEmpty) {
      notify(build, flags)
    }
  }
}

object SlackServerAdapter {
  def calculateFlags(previous: Status, current: Status): Set[BuildSettingFlag] = {
    import BuildSettingFlag._

    def changed = statusChanged(previous, current)

    def applyIfChanged(flag1: BuildSettingFlag, flag2: BuildSettingFlag) = if (changed) {
      Set(flag1, flag2)
    } else {
      Set(flag1)
    }

    if (current.isSuccessful) {
      applyIfChanged(success, failureToSuccess)
    } else if (current.isFailed) {
      applyIfChanged(failure, successToFailure)
    } else {
      Set()
    }
  }

  def statusChanged(previous: Status, current: Status): Boolean = {
    import Helpers._

    if (previous.isSuccessful) {
      current.isFailed
    } else if (previous.isFailed) {
      current.isSuccessful
    } else if (previous.isUnknown) {
      false
    } else {
      current.isSuccessful || (!previous.isUnknown && current.isUnknown)
    }
  }
}
