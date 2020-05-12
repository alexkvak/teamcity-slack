package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.Destination

object Strings {
  lazy val logCategory = "Slack Integration"
  def label: String = "Slack"
  def tabId: String = "Slack"

  private def unableToCreateSessionByConfig(reason: String): String = s"Unable to create session by config: $reason"

  object MessageBuilder {
    lazy val unknownBranch = "Unknown"
    lazy val unknownReason = "Unknown"
    lazy val unknownParameter = ""
    lazy val statusSucceeded = "Succeeded"
    lazy val statusFailed = "Failed"
    lazy val statusCanceled = "Canceled"
    lazy val statusStarted = "Started"
    lazy val statusQueued = "Queued"
  }

  object BuildSettingsController {
    lazy val channelOrNotifyCommitterError = "Either specify Slack channel name or check Notify committer flag"
    lazy val compileBranchMaskError = "Unable to compile branch mask"
    lazy val compileArtifactsMaskError = "Unable to compile artifacts mask"
    def sessionByConfigError(reason: String): String = unableToCreateSessionByConfig(reason)
    def channelNotFoundError(channel: String): String = s"Unable to find channel with name $channel"
    lazy val emptyConfigError = "Config is empty"
    lazy val requirementsError = "One or more required params are missing"
  }

  object ConfigController {
    lazy val oauthTokenUpdateFailed = "Failed to update OAuth Access Token"
    def sessionByConfigError(reason: String): String = unableToCreateSessionByConfig(reason)
    lazy val oauthKeyParamMissing = "Param oauthKey is missing"
  }

  object BuildSettingsTry {
    lazy val unknownDestination = "Unable to detect destination"
    lazy val emptyIdParam = "Param id is empty"
    lazy val buildSettingNotFound = "Build setting not found"
    lazy val previousBuildNotFound = "Previous build not found"
    def messageSent(destination: String) = s"Message sent to $destination"
  }

  object SlackGateway {
    def failedToSendToDestination(destination: Destination, error: String) = s"Message to $destination wasn't sent. Reason: $error"
    def messageSent(destination: Destination) = s"Message sent to $destination"
    def channelNotFound(channel: String) = s"Channel #$channel not found"
    def userNotFound(email: String) = s"User for $email not found"
    lazy val unknownDestination = "Destination is unknown"
    lazy val emptySession = "Unable to connect your Slack account. Please check auth credentials"
  }
}
