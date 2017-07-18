package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{SBuild, WebLinks}

import scala.collection.JavaConverters._

class MessageBuilder(build: SBuild, viewResultsUrl: String, nickByEmail: (String) ⇒ Option[String]) {
  import MessageBuilder._

  def compile(template: String): SlackAttachment = {
    def status = if (build.getBuildStatus.isSuccessful) "succeeded" else "failed"

    // TODO: implement
    def changes = ""
    def artifacts = ""
    def mentions = if (build.getBuildStatus.isSuccessful) "" else {
      committees(build).map(nickByEmail).collect { case Some(x) ⇒ s"@$x" }.mkString(" ")
    }

    val text = """\{(\w+)\}""".r.replaceAllIn(template, m ⇒ m.group(1) match {
      case "name" ⇒ build.getFullName
      case "number" ⇒ build.getBuildNumber
      case "branch" ⇒ build.getBranch.getDisplayName
      case "status" ⇒ status
      case "changes" ⇒ changes
      case "artifacts" ⇒ artifacts
      case "link" ⇒ viewResultsUrl
      case "mentions" ⇒ mentions
      case _ ⇒ m.group(0)
    })

    SlackAttachment(text.trim, statusColor(build.getBuildStatus))
  }
}

object MessageBuilder {
  lazy val statusNormalColor = "#02c456"

  def defaultMessage: String =
    """<{link}|{name} - {number}>
      |Branch: {branch}
      |Status: {status}
      |{mentions}
    """.stripMargin

  private def statusColor(status: Status) = if (status == Status.NORMAL) statusNormalColor else status.getHtmlColor

  def committees(build: SBuild): Vector[String] = {
    val users = build.getContainingChanges.asScala.toVector.flatMap(_.getCommitters.asScala).distinct
    users.map(user ⇒ Option(user.getEmail).getOrElse("")).filter(_.length > 0)
  }
}

class MessageBuilderFactory(webLinks: WebLinks, gateway: SlackGateway) {
  private def nickByEmail(email: String): Option[String] = Option(gateway.session.get.findUserByEmail(email)).map(_.getUserName)

  def createForBuild(build: SBuild) = new MessageBuilder(build, webLinks.getViewResultsUrl(build), nickByEmail)
}

