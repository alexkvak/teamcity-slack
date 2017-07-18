package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{SBuild, WebLinks}

class MessageBuilder(template: String) {
  import MessageBuilder._

  def compile(build: SBuild, webLinks: WebLinks): SlackAttachment = {
    compile(build, viewResultsUrl(build, webLinks))
  }

  def compile(build: SBuild, viewResultsUrl: String): SlackAttachment = {
    def status = if (build.getBuildStatus.isSuccessful) "succeeded" else "failed"
    def linkToBuild = s"<$viewResultsUrl|Open>"

    // TODO: implement
    def changes = ""
    def artifacts = ""

    val text = """\{(\w+)\}""".r.replaceAllIn(template, _.group(1) match {
      case "name" ⇒ build.getFullName
      case "number" ⇒ build.getBuildNumber
      case "branch" ⇒ build.getBranch.getDisplayName
      case "status" ⇒ status
      case "changes" ⇒ changes
      case "artifacts" ⇒ artifacts
    })

    SlackAttachment(text.trim + "\n" + linkToBuild, statusColor(build.getBuildStatus))
  }
}

object MessageBuilder {
  lazy val statusNormalColor = "#02c456"

  def defaultMessage: String =
    """{name} - {number}
      |Branch: {branch}
      |Status: {status}
    """.stripMargin

  def apply(template: String): MessageBuilder = new MessageBuilder(template)

  private def statusColor(status: Status) = if (status == Status.NORMAL) statusNormalColor else status.getHtmlColor

  private def viewResultsUrl(build: SBuild, webLinks: WebLinks): String = webLinks.getViewResultsUrl(build)
}
