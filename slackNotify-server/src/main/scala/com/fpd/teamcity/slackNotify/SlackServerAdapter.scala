package com.fpd.teamcity.slackNotify

import com.fpd.teamcity.slackNotify.ConfigManager.BuildSettingFlag
import com.fpd.teamcity.slackNotify.ConfigManager.BuildSettingFlag.BuildSettingFlag
import jetbrains.buildServer.serverSide.{BuildServerAdapter, SBuildServer, SRunningBuild}

class SlackServerAdapter(sBuildServer: SBuildServer,
                         configManager: ConfigManager,
                         gateway: SlackGateway
                        ) extends BuildServerAdapter {

  sBuildServer.addListener(this)

  def notify(build: SRunningBuild, flag: BuildSettingFlag): Unit = {
    def matchBranch(mask: String) = Option(build.getBranch).map(branch ⇒ mask.r.findFirstIn(branch.getName)).isDefined

    val settings = configManager.buildSettingList(build.getBuildTypeId).values.filter(x ⇒ matchBranch(x.branchMask))
    settings.foreach { setting ⇒
      gateway.sendMessage(setting.slackChannel, flag.toString)
    }
  }

  override def buildFinished(build: SRunningBuild): Unit = {
    if (build.getBuildStatus.isSuccessful) {
      notify(build, BuildSettingFlag.success)
    }
  }
}
