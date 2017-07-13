package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag
import com.fpd.teamcity.slack.ConfigManager.BuildSettingFlag.BuildSettingFlag
import com.fpd.teamcity.slack.SlackGateway.{SlackChannel, SlackUser}
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

  override def buildFinished(build: SRunningBuild): Unit =
    calculateFlags(build, sBuildServer).foreach(flags ⇒ notify(build, flags))
}

object SlackServerAdapter {
  private def calculateFlags(implicit build: SRunningBuild, sBuildServer: SBuildServer) = {
    import BuildSettingFlag._

    val flags = collection.mutable.Set.empty[BuildSettingFlag]

    if (build.getBuildStatus.isSuccessful) {
      flags += success
      if (statusChanged) {
        flags += failureToSuccess
      }
    } else if (build.getBuildStatus.isFailed) {
      flags += failure
      if (statusChanged) {
        flags += successToFailure
      }
    }

    flags.size match {
      case 0 ⇒ None
      case _ ⇒ Some(flags.toSet)
    }
  }

  private def statusChanged(implicit build: SRunningBuild, sBuildServer: SBuildServer) = {
    val previousStatus = sBuildServer.getHistory.getEntriesBefore(build, false).asScala.find(!_.isPersonal).map(_.getBuildStatus)
    val current = build.getBuildStatus

    previousStatus.forall { prev ⇒
      if (prev.isSuccessful) {
        current.isFailed
      } else if (prev.isFailed) {
        current.isSuccessful
      } else {
        true
      }
    }
  }
}
