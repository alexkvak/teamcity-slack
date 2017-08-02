package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{SBuild, WebLinks}

import scala.collection.JavaConverters._

class MessageBuilder(
                      build: SBuild,
                      viewResultsUrl: (SBuild) ⇒ String,
                      nickByEmail: (String) ⇒ Option[String],
                      downloadArtifactsUrl: (SBuild) ⇒ String
                    ) {
  import MessageBuilder._
  import Helpers.Implicits._

  def compile(template: String): SlackAttachment = {
    def status = if (build.getBuildStatus.isSuccessful) "succeeded" else "failed"

    def artifacts = s"<${downloadArtifactsUrl(build)}|Download all artifacts>"

    def changes = build.getContainingChanges.asScala.take(5).map { change ⇒
      val name = change.getCommitters.asScala.headOption.map(_.getUsername).getOrElse("unknown")
      val filesCount = change.getChangeCount

      s" $filesCount files by $name: ${change.getDescription}"
    } mkString "\n"

    def mentions = if (build.getBuildStatus.isSuccessful) "" else {
      build.committees.map(nickByEmail).collect { case Some(x) ⇒ s"@$x" }.mkString(" ")
    }

    val text = """\{(\w+)\}""".r.replaceAllIn(template, m ⇒ m.group(1) match {
      case "name" ⇒ build.getFullName
      case "number" ⇒ build.getBuildNumber
      case "branch" ⇒ build.getBranch.getDisplayName
      case "status" ⇒ status
      case "changes" ⇒ changes
      case "artifacts" ⇒ artifacts
      case "link" ⇒ viewResultsUrl(build)
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
}

class MessageBuilderFactory(webLinks: WebLinks, gateway: SlackGateway) {
  private def nickByEmail(email: String): Option[String] = Option(gateway.session.get.findUserByEmail(email)).map(_.getUserName)

  def createForBuild(build: SBuild) = new MessageBuilder(build, webLinks.getViewResultsUrl, nickByEmail, webLinks.getDownloadAllArtefactsUrl)
}

