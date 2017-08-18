package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{SBuild, ServerPaths, WebLinks}

import scala.collection.JavaConverters._

class MessageBuilder(build: SBuild, context: MessageBuilderContext) {
  import MessageBuilder._
  import Helpers.Implicits._

  def compile(template: String): SlackAttachment = {
    def status = if (build.getBuildStatus.isSuccessful) "succeeded" else "failed"

    def artifacts = s"<${context.getDownloadAllArtifactsUrl(build)}|Download all artifacts>"

    def artifactsRelUrl = build.getArtifactsDirectory.getPath.stripPrefix(context.getArtifactsPath)

    def changes = build.getContainingChanges.asScala.take(5).map { change ⇒
      val name = change.getCommitters.asScala.headOption.map(_.getUsername).getOrElse("unknown")
      val filesCount = change.getChangeCount

      s" $filesCount files by $name: ${change.getDescription}"
    } mkString "\n"

    def mentions = if (build.getBuildStatus.isSuccessful) "" else {
      build.committees.map(context.nickByEmail).collect { case Some(x) ⇒ s"@$x" }.mkString(" ")
    }

    val text = """\{([\s\w._%]+)\}""".r.replaceAllIn(template, m ⇒ m.group(1) match {
      case "name" ⇒ build.getFullName
      case "number" ⇒ build.getBuildNumber
      case "branch" ⇒ build.getBranch.getDisplayName
      case "status" ⇒ status
      case "changes" ⇒ changes
      case "allArtifactsDownloadUrl" ⇒ artifacts
      case "artifactsRelUrl" ⇒ artifactsRelUrl
      case "link" ⇒ context.getViewResultsUrl(build)
      case "mentions" ⇒ mentions
      case x if x.startsWith("%") && x.endsWith("%") ⇒
        context.getBuildParameter(build, x.substring(1, x.length - 1).trim) match {
        case Some(value) ⇒ value
        case _ ⇒ m.group(0)
      }
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

class MessageBuilderFactory(webLinks: WebLinks, gateway: SlackGateway, paths: ServerPaths) {
  private val context = new MessageBuilderContext(webLinks, gateway, paths)

  def createForBuild(build: SBuild) = new MessageBuilder(build, context)
}

class MessageBuilderContext(webLinks: WebLinks, gateway: SlackGateway, paths: ServerPaths) {
  def getViewResultsUrl: (SBuild) ⇒ String = webLinks.getViewResultsUrl

  def getDownloadAllArtifactsUrl: (SBuild) ⇒ String = webLinks.getDownloadAllArtefactsUrl

  def nickByEmail: (String) ⇒ Option[String] = email ⇒ Option(gateway.session.get.findUserByEmail(email)).map(_.getUserName)

  def getArtifactsPath: String = paths.getArtifactsDirectory.getPath

  def getBuildParameter: (SBuild, String) ⇒ Option[String] = (build, name) ⇒
    Option(build.getBuildType.getParameter(name).getValue)
}

