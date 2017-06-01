package com.fpd.teamcity.slackNotify

import java.util

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import jetbrains.buildServer.Build
import jetbrains.buildServer.notification.Notificator
import jetbrains.buildServer.responsibility.{ResponsibilityEntry, TestNameResponsibilityEntry}
import jetbrains.buildServer.serverSide.mute.MuteInfo
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo
import jetbrains.buildServer.serverSide.{SBuildType, SProject, SRunningBuild, STest}
import jetbrains.buildServer.tests.TestName
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.vcs.VcsRoot

object SlackNotifier {
  private def sendMessage(message: String)(implicit config: ConfigManager) = {
    val session = SlackSessionFactory.createWebSocketSlackSession(config.oauthKey.getOrElse(""))
    session.connect()
    val channel = session.findChannelByName("#general") //make sure bot is a member of the channel.
    session.sendMessage(channel, message)
  }
}

class SlackNotifier(implicit val config: ConfigManager) extends Notificator {
  import SlackNotifier._

  override def notifyTestsMuted(tests: util.Collection[STest], muteInfo: MuteInfo, users: util.Set[SUser]): Unit = ???

  override def notifyBuildSuccessful(build: SRunningBuild, users: util.Set[SUser]): Unit = ???

  override def getNotificatorType: String = ???

  override def notifyBuildProbablyHanging(build: SRunningBuild, users: util.Set[SUser]): Unit = ???

  override def notifyBuildFailed(build: SRunningBuild, users: util.Set[SUser]): Unit = ???

  override def notifyBuildProblemResponsibleChanged(buildProblems: util.Collection[BuildProblemInfo], entry: ResponsibilityEntry, project: SProject, users: util.Set[SUser]): Unit = ???

  override def notifyBuildProblemResponsibleAssigned(buildProblems: util.Collection[BuildProblemInfo], entry: ResponsibilityEntry, project: SProject, users: util.Set[SUser]): Unit = ???

  override def notifyLabelingFailed(build: Build, root: VcsRoot, exception: Throwable, users: util.Set[SUser]): Unit = ???

  override def notifyResponsibleAssigned(buildType: SBuildType, users: util.Set[SUser]): Unit = ???

  override def notifyResponsibleAssigned(oldValue: TestNameResponsibilityEntry, newValue: TestNameResponsibilityEntry, project: SProject, users: util.Set[SUser]): Unit = ???

  override def notifyResponsibleAssigned(testNames: util.Collection[TestName], entry: ResponsibilityEntry, project: SProject, users: util.Set[SUser]): Unit = ???

  override def notifyBuildFailing(build: SRunningBuild, users: util.Set[SUser]): Unit = ???

  override def notifyBuildFailedToStart(build: SRunningBuild, users: util.Set[SUser]): Unit = ???

  override def notifyBuildProblemsUnmuted(buildProblems: util.Collection[BuildProblemInfo], muteInfo: MuteInfo, user: SUser, users: util.Set[SUser]): Unit = ???

  override def notifyResponsibleChanged(buildType: SBuildType, users: util.Set[SUser]): Unit = ???

  override def notifyResponsibleChanged(oldValue: TestNameResponsibilityEntry, newValue: TestNameResponsibilityEntry, project: SProject, users: util.Set[SUser]): Unit = ???

  override def notifyResponsibleChanged(testNames: util.Collection[TestName], entry: ResponsibilityEntry, project: SProject, users: util.Set[SUser]): Unit = ???

  override def getDisplayName: String = ???

  override def notifyTestsUnmuted(tests: util.Collection[STest], muteInfo: MuteInfo, user: SUser, users: util.Set[SUser]): Unit = ???

  override def notifyBuildProblemsMuted(buildProblems: util.Collection[BuildProblemInfo], muteInfo: MuteInfo, users: util.Set[SUser]): Unit = ???

  override def notifyBuildStarted(build: SRunningBuild, users: util.Set[SUser]): Unit = sendMessage(build.getBuildDescription)
}
