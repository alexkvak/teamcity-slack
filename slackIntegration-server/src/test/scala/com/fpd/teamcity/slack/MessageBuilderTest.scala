package com.fpd.teamcity.slack

import java.io.File

import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.{Branch, SBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.vcs.SVcsModification
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class MessageBuilderTest extends FlatSpec with MockFactory with Matchers {
  import MessageBuilderTest._

  "MessageBuilder.compile" should "compile default template" in {
    implicit val build = stub[SBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.NORMAL

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    messageBuilder(viewResultsUrl).compile(MessageBuilder.defaultMessage) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Full name - 2>
        |Branch: default
        |Status: succeeded
      """.stripMargin.trim, MessageBuilder.statusNormalColor)
  }

  "MessageBuilder.compile" should "compile failure template" in {
    implicit val build = stub[SBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    messageBuilder(viewResultsUrl).compile(MessageBuilder.defaultMessage) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Full name - 2>
        |Branch: default
        |Status: failed
        |@nick1 @nick2
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with unknown placeholders" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE

    val messageTemplate = """{name}
                            |{unknown}
                          """.stripMargin

    messageBuilder().compile(messageTemplate) shouldEqual SlackAttachment(
      """Full name
        |{unknown}
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with mentions placeholders and replace it with empty string" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildStatus _ when() returns Status.NORMAL

    val messageTemplate = """{name}
                            |{mentions}
                          """.stripMargin

    messageBuilder().compile(messageTemplate) shouldEqual SlackAttachment("Full name", MessageBuilder.statusNormalColor)
  }

  "MessageBuilder.compile" should "compile template with mentions placeholders" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{mentions}
                          """.stripMargin

    messageBuilder().compile(messageTemplate) shouldEqual SlackAttachment(
      """Full name
        |@nick1 @nick2
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with changes placeholders" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{changes}
                          """.stripMargin

    messageBuilder().compile(messageTemplate) shouldEqual SlackAttachment(
      """Full name
        | 5 files by name1: Did some changes
        | 1 files by name2: Did another changes
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with reason placeholders" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    val reasons = List("some reason 1", "some reason 2")
    build.getFailureReasons _ when() returns mockReasons(reasons)

    val messageTemplate = """{name}
                            |Reason: {reason}
                          """.stripMargin

    messageBuilder().compile(messageTemplate) shouldEqual SlackAttachment(
      s"""Full name
        |Reason: ${reasons.mkString("\n")}
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with artifacts placeholders" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{allArtifactsDownloadUrl}
                          """.stripMargin

    val downloadUrl = "http://my.teamcity/download/artifacts.zip"
    messageBuilder(downloadArtifactsUrl = downloadUrl).compile(messageTemplate) shouldEqual SlackAttachment(
      s"""Full name
        |<$downloadUrl|Download all artifacts>
      """.stripMargin.trim, Status.FAILURE.getHtmlColor)
  }

  "MessageBuilder.compile" should "compile template with artifactsRelUrl placeholders" in {
    implicit val build = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getContainingChanges _ when() returns mockChanges
    build.getArtifactsDirectory _ when() returns new File("/full/artifacts/path/my/build/folder/")

    val messageTemplate = """{name}
                            |{artifactsRelUrl}
                          """.stripMargin


    messageBuilder(artifactsPath = "/full/artifacts/path/").compile(messageTemplate) shouldEqual SlackAttachment(
      s"""Full name
        |my/build/folder
      """.stripMargin.trim, MessageBuilder.statusNormalColor)
  }

  "MessageBuilder.compile" should "compile template with parameter placeholders" in {
    implicit val build = stub[SBuild]

    val teamcityParam = "teamcity.param"
    val teamcityParamValue = "teamcity.param.value"

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = s"""{name}
                            |{% $teamcityParam%}
                          """.stripMargin


    messageBuilder(params = Map(teamcityParam → teamcityParamValue)).compile(messageTemplate) shouldEqual SlackAttachment(
      s"""Full name
        |$teamcityParamValue
      """.stripMargin.trim, MessageBuilder.statusNormalColor)
  }

//  TODO: artifactLinks test

  private def mockChanges = {
    val vcsModification1 = stub[SVcsModification]
    val vcsModification2 = stub[SVcsModification]
    val user1 = stub[SUser]
    val user2 = stub[SUser]
    user1.getEmail _ when() returns "nick1"
    user1.getUsername _ when() returns "name1"
    user2.getEmail _ when() returns "nick2"
    user2.getUsername _ when() returns "name2"
    vcsModification1.getCommitters _ when() returns Set(user1).asJava
    vcsModification2.getCommitters _ when() returns Set(user2).asJava
    vcsModification1.getDescription _ when() returns "Did some changes"
    vcsModification1.getChangeCount _ when() returns 5
    vcsModification2.getDescription _ when() returns "Did another changes"
    vcsModification2.getChangeCount _ when() returns 1

    List(vcsModification1, vcsModification2).asJava
  }

  private def mockReasons(reasons: List[String]) = reasons.map(reason ⇒ BuildProblemData.createBuildProblem("identity", "custom", reason)).asJava
}

object MessageBuilderTest extends MockFactory {
  import MessageBuilder._
  def messageBuilder(viewResultsUrl: String = "", downloadArtifactsUrl: String = "", artifactsPath: String = "", params: Map[String, String] = Map.empty)(implicit build: SBuild) = {
    val context = stub[MessageBuilderContext]
    context.getArtifactsPath _ when() returns artifactsPath
    context.getViewResultsUrl _ when() returns (_ ⇒ viewResultsUrl)
    context.getDownloadAllArtifactsUrl _ when() returns (_ ⇒ downloadArtifactsUrl)
    context.nickByEmail _ when() returns (x ⇒ Some(x))
    context.getBuildParameter _ when() returns ((_, name) ⇒ params.get(name))

    new MessageBuilder(build, context)
  }
}
