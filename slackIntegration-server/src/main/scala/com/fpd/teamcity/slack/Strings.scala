package com.fpd.teamcity.slack

object Strings {
  lazy val logCategory = "Slack Integration"
  def label: String = "Slack"
  def tabId: String = "Slack"
  lazy val channelMessageOwner = "TeamCity"

  object MessageBuilder {
    lazy val unknownBranch = "Unknown"
    lazy val unknownReason = "Unknown"
    lazy val statusSucceeded = "succeeded"
    lazy val statusFailed = "failed"
    lazy val statusCanceled = "canceled"
  }
}
