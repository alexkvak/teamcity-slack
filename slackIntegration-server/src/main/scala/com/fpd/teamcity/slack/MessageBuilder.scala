package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.MessageBuilder._
import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import com.fpd.teamcity.slack.Strings.MessageBuilder._
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts.BuildArtifactsProcessor.Continuation
import jetbrains.buildServer.serverSide.artifacts.{BuildArtifact, BuildArtifactsViewMode}
import jetbrains.buildServer.serverSide.{SBuild, ServerPaths, WebLinks}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class MessageBuilder(build: SBuild, context: MessageBuilderContext) {
  import Helpers.Implicits._

  def compile(template: String, setting: Option[BuildSetting] = None): SlackAttachment = {
    def status = if (build.getDuration == 0) {
      if (build.getBuildStatus.isSuccessful)
        statusStarted
      else
        statusCanceled
    }
    else if (build.getBuildStatus.isSuccessful) statusSucceeded
    else statusFailed

    def artifacts = s"<${context.getDownloadAllArtifactsUrl(build)}|Download all artifacts>"

    lazy val artifactsRelUrl = build.getArtifactsDirectory.getPath.stripPrefix(context.getArtifactsPath).stripPrefix("/")

    def artifactLinks = if (setting.isDefined && !setting.get.artifactsMask.isEmpty) {
      val links = ArrayBuffer.empty[String]
      val compiledMask = setting.get.artifactsMask.r
      val publicUrl = context.artifactsPublicUrl.getOrElse("").reverse.dropWhile(_ == '/').reverse

      build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT_WITH_ARCHIVES_CONTENT).iterateArtifacts((artifact: BuildArtifact) ⇒ {
        if (artifact.isFile && compiledMask.findFirstIn(artifact.getName).isDefined) {
          links += s"$publicUrl/$artifactsRelUrl/${artifact.getRelativePath}"
        }

        if (!artifact.isArchive && (artifact.getRelativePath == "" || !artifact.isDirectory || (artifact.isDirectory && setting.get.deepLookup)))
          Continuation.CONTINUE
        else
          Continuation.SKIP_CHILDREN
      })

      links.mkString("\n")
    } else ""

    def changes = build.getContainingChanges.asScala.take(5).map { change ⇒
      val name = change.getCommitters.asScala.headOption.map(_.getDescriptiveName).getOrElse(change.getUserName)
      s"- ${change.getDescription.replace("\n", " ").trim} [$name]"
    } mkString "\n"

    def mentions = if (build.getBuildStatus.isSuccessful) "" else {
      build.committeeEmails.map(context.userByEmail).collect { case Some(x) ⇒ s"<@$x>" }.mkString(" ")
    }

    def users = if (build.getBuildStatus.isSuccessful) "" else {
      build.committees.map(user ⇒ user.getDescriptiveName).mkString(", ")
    }

    def reason = if (build.getBuildStatus.isSuccessful) "" else {
      "Reason: " + (if (build.getFailureReasons.isEmpty) unknownReason else build.getFailureReasons.asScala.map(_.getDescription).mkString("\n"))
    }

    val text = """\{([\s\w._%]+)\}""".r.replaceAllIn(template, m ⇒ m.group(1) match {
      case "name" ⇒ encodeText(build.getFullName)
      case "number" ⇒ build.getBuildNumber
      case "branch" ⇒ Option(build.getBranch).map(_.getDisplayName).getOrElse(unknownBranch)
      case "status" ⇒ status
      case "changes" ⇒ encodeText(changes)
      case "allArtifactsDownloadUrl" ⇒ artifacts
      case "artifactsRelUrl" ⇒ artifactsRelUrl
      case "artifactLinks" ⇒ artifactLinks
      case "link" ⇒ context.getViewResultsUrl(build)
      case "mentions" ⇒ mentions
      case "users" ⇒ users
      case "reason" ⇒ encodeText(reason)
      case x if x.startsWith("%") && x.endsWith("%") ⇒
        context.getBuildParameter(build, x.substring(1, x.length - 1).trim) match {
        case Some(value) ⇒ encodeText(value)
        case _ ⇒ unknownParameter
      }
      case _ ⇒ m.group(0)
    })

    SlackAttachment(text.trim, statusColor(build.getBuildStatus), statusEmoji(build.getBuildStatus))
  }
}

object MessageBuilder {
  lazy val statusNormalColor = "#02c456"

  lazy val defaultMessage: String =
    """<{link}|{name} - {number}>
      |Branch: {branch}
      |Status: {status}
      |{mentions}
    """.stripMargin.trim

  private def statusColor(status: Status) = if (status == Status.NORMAL) statusNormalColor else status.getHtmlColor

  private def statusEmoji(status: Status) = status match {
    case Status.NORMAL ⇒ "✅"
    case Status.FAILURE ⇒ "⛔"
    case _ ⇒ "⚪"
  }

  case class MessageBuilderContext(webLinks: WebLinks, gateway: SlackGateway, paths: ServerPaths, configManager: ConfigManager) {
    def getViewResultsUrl: SBuild ⇒ String = webLinks.getViewResultsUrl

    def getDownloadAllArtifactsUrl: SBuild ⇒ String = webLinks.getDownloadAllArtefactsUrl

    def userByEmail: String ⇒ Option[String] = email ⇒ gateway.session.flatMap(s ⇒ Option(s.findUserByEmail(email))).map(_.getId)

    def getArtifactsPath: String = paths.getArtifactsDirectory.getPath

    def artifactsPublicUrl: Option[String] = configManager.publicUrl

    def getBuildParameter: (SBuild, String) ⇒ Option[String] = (build, name) ⇒
      Option(build.getParametersProvider.get(name))
  }

  private def encodeText(text: String) =
    text.replaceAllLiterally("&", "&amp;")
      .replaceAllLiterally("<", "&lt;")
      .replaceAllLiterally(">", "&gt;")
}

class MessageBuilderFactory(webLinks: WebLinks, gateway: SlackGateway, paths: ServerPaths, configManager: ConfigManager) {
  private val context = MessageBuilderContext(webLinks, gateway, paths, configManager)

  def createForBuild(build: SBuild) = new MessageBuilder(build, context)
}

