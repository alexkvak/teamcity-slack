package com.fpd.teamcity.slack

import java.io.File

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.SlackGateway.SlackAttachment
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.artifacts.{BuildArtifact, BuildArtifacts, BuildArtifactsViewMode}
import jetbrains.buildServer.serverSide.{Branch, SBuild, SQueuedBuild}
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.vcs.SVcsModification
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class SBuildMessageBuilderTest extends FlatSpec with MockFactory with Matchers {
  import SBuildMessageBuilderTest._

  private val buildSetting = BuildSetting("", "", "art", "", artifactsMask = ".*")

  "MessageBuilder.compile" should "compile default template" in {
    implicit val build: SBuild = stub[SBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getDuration _ when() returns 100
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.NORMAL

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    messageBuilder(viewResultsUrl).compile(SBuildMessageBuilder.defaultMessage, buildSetting) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Full name - 2>
        |Branch: default
        |Status: ${Strings.MessageBuilder.statusSucceeded}
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile default template with encoded build name" in {
    implicit val build: SBuild = stub[SBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Deploy -> test-host.io & demo-host.io <-"
    build.getDuration _ when() returns 100
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.NORMAL

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    messageBuilder(viewResultsUrl).compile(SBuildMessageBuilder.defaultMessage, buildSetting) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Deploy -&gt; test-host.io &amp; demo-host.io &lt;- - 2>
        |Branch: default
        |Status: ${Strings.MessageBuilder.statusSucceeded}
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile failure template" in {
    implicit val build: SBuild = stub[SBuild]
    val branch = stub[Branch]

    build.getFullName _ when() returns "Full name"
    build.getDuration _ when() returns 100
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns branch
    branch.getDisplayName _ when() returns "default"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    messageBuilder(viewResultsUrl).compile(SBuildMessageBuilder.defaultMessage, buildSetting) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Full name - 2>
        |Branch: default
        |Status: ${Strings.MessageBuilder.statusFailed}
        |<@nick1> <@nick2>
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template with unknown placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE

    val messageTemplate = """{name}
                            |{unknown}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      """Full name
        |{unknown}
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template with mentions placeholders and replace it with empty string" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildStatus _ when() returns Status.NORMAL

    val messageTemplate = """{name}
                            |{mentions}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual
      SlackAttachment("Full name", SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with mentions placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{mentions}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      """Full name
        |<@nick1> <@nick2>
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template with changes placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{changes}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      """Full name
        |- Did some changes [name1]
        |- Did another changes [name2]
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template with reason placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    val reasons = List("some reason 1", "some reason 2")
    build.getFailureReasons _ when() returns mockReasons(reasons)

    val messageTemplate = """{name}
                            |{reason}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |Reason: ${reasons.mkString("\n")}
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template without reason placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    val reasons = List("some reason 1", "some reason 2")
    build.getFailureReasons _ when() returns mockReasons(reasons)

    val messageTemplate = """{name}
                            |{reason}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual
      SlackAttachment("Full name", SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with artifacts placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{allArtifactsDownloadUrl}
                          """.stripMargin

    val downloadUrl = "http://my.teamcity/download/artifacts.zip"
    messageBuilder(downloadArtifactsUrl = downloadUrl).compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |<$downloadUrl|Download all artifacts>
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template with artifactsRelUrl placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getContainingChanges _ when() returns mockChanges
    build.getArtifactsDirectory _ when() returns new File("/full/artifacts/path/my/build/folder/")

    val messageTemplate = """{name}
                            |{artifactsRelUrl}
                          """.stripMargin


    messageBuilder(artifactsPath = "/full/artifacts/path/").compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |my/build/folder
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with parameter placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    val teamcityParam = "teamcity.param"
    val teamcityParamValue = "teamcity.param.value"

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = s"""{name}
                            |{% $teamcityParam%}
                          """.stripMargin


    messageBuilder(params = Map(teamcityParam → teamcityParamValue)).compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |$teamcityParamValue
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with parameter placeholder containing emphasis in name" in {
    implicit val build: SBuild = stub[SBuild]

    val teamcityParam = "teamcity-param"
    val teamcityParamValue = "teamcity.param.value"

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = s"""{name}
                            |{%$teamcityParam%}
                          """.stripMargin


    messageBuilder(params = Map(teamcityParam → teamcityParamValue)).compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |$teamcityParamValue
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with changes placeholders with non-teamcity committer" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockUnknownChange

    val messageTemplate = """{name}
                            |{changes}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      """Full name
        |- Did some changes [user@unknown.com]
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template for canceled build" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getDuration _ when() returns 0
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.UNKNOWN

    val messageTemplate = """{name}
                            |{status}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |${Strings.MessageBuilder.statusCanceled}
      """.stripMargin.trim, Status.UNKNOWN.getHtmlColor, "⚪")
  }

  "MessageBuilder.compile" should "compile template for started build" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getDuration _ when() returns 0
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL

    val messageTemplate = """{name}
                            |{status}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      s"""Full name
        |${Strings.MessageBuilder.statusStarted}
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor,"✅")
  }

  "MessageBuilder.compile" should "compile template with artifactLinks placeholder" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getDuration _ when() returns 100
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getArtifactsDirectory _ when() returns new File("directory")

    val artifactsViewer = stub[BuildArtifacts]
    build.getArtifacts _ when BuildArtifactsViewMode.VIEW_DEFAULT_WITH_ARCHIVES_CONTENT returns artifactsViewer
    val artifacts = Seq(
      new TestBuildArtifact("artifact.txt", "artifact.txt", true),
      new TestBuildArtifact("artifact2.txt", "folder/artifact2.txt", true)
    )
    artifactsViewer.iterateArtifacts _ when * onCall { processor: BuildArtifacts.BuildArtifactsProcessor ⇒
      artifacts.foreach(processor.processBuildArtifact)
    }

    val messageTemplate = """{name}
                            |{artifactLinks}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, BuildSetting("", "", "art", "", artifactsMask = ".*")) shouldEqual SlackAttachment(
      s"""Full name
        |${artifactsPublicUrl}directory/artifact.txt
        |${artifactsPublicUrl}directory/folder/artifact2.txt
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template for build without branch" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getDuration _ when() returns 100
    build.getBuildNumber _ when() returns "2"
    build.getBranch _ when() returns null
    build.getBuildStatus _ when() returns Status.NORMAL

    val viewResultsUrl = "http://localhost:8111/viewLog.html?buildId=2"
    messageBuilder(viewResultsUrl).compile(SBuildMessageBuilder.defaultMessage, buildSetting) shouldEqual SlackAttachment(
      s"""<$viewResultsUrl|Full name - 2>
                          |Branch: ${Strings.MessageBuilder.unknownBranch}
                          |Status: ${Strings.MessageBuilder.statusSucceeded}
      """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with users placeholders" in {
    implicit val build: SBuild = stub[SBuild]

    build.getFullName _ when() returns "Full name"
    build.getBuildNumber _ when() returns "2"
    build.getBuildStatus _ when() returns Status.FAILURE
    build.getContainingChanges _ when() returns mockChanges

    val messageTemplate = """{name}
                            |{users}
                          """.stripMargin

    messageBuilder().compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
      """Full name
        |name1, name2
      """.stripMargin.trim, Status.FAILURE.getHtmlColor, "⛔")
  }

  "MessageBuilder.compile" should "compile template with limited changes placeholders" in {
    implicit val build: SBuild = stub[SBuild]
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getContainingChanges _ when() returns mockChanges
    val messageTemplate = "{changes}"
    messageBuilder().compile(messageTemplate, BuildSetting("", "", "", "", maxVcsChanges = 1)) shouldEqual SlackAttachment(
      "- Did some changes [name1]", SBuildMessageBuilder.statusNormalColor, "✅")
  }

  "MessageBuilder.compile" should "compile template with formattedDuration placeholder" in {
    implicit val build: SBuild = stub[SBuild]
    build.getBuildStatus _ when() returns Status.NORMAL
    build.getDuration _ when() returns 10L
    val messageTemplate = "{formattedDuration}"
    messageBuilder().compile(messageTemplate, BuildSetting("", "", "", "", maxVcsChanges = 1)) shouldEqual SlackAttachment(
      "10s", SBuildMessageBuilder.statusNormalColor, "✅")
  }

  private def mockChanges = {
    val vcsModification1 = stub[SVcsModification]
    val vcsModification2 = stub[SVcsModification]
    val user1 = stub[SUser]
    val user2 = stub[SUser]
    user1.getEmail _ when() returns "nick1"
    user1.getDescriptiveName _ when() returns "name1"
    user2.getEmail _ when() returns "nick2"
    user2.getDescriptiveName _ when() returns "name2"
    vcsModification1.getCommitters _ when() returns Set(user1).asJava
    vcsModification2.getCommitters _ when() returns Set(user2).asJava
    vcsModification1.getDescription _ when() returns "Did some changes\nSecond line"
    vcsModification1.getChangeCount _ when() returns 5
    vcsModification2.getDescription _ when() returns "Did another changes\n"
    vcsModification2.getChangeCount _ when() returns 1

    List(vcsModification1, vcsModification2).asJava
  }

  private def mockUnknownChange = {
    val vcsModification = stub[SVcsModification]
    vcsModification.getCommitters _ when() returns Set.empty[SUser].asJava
    vcsModification.getDescription _ when() returns "Did some changes\n"
    vcsModification.getUserName _ when() returns "user@unknown.com"

    List(vcsModification).asJava
  }

  private def mockReasons(reasons: List[String]) = reasons.map(reason ⇒ BuildProblemData.createBuildProblem("identity", "custom", reason)).asJava
}

object SBuildMessageBuilderTest extends MockFactory {
  import SBuildMessageBuilder._

  private val artifactsPublicUrl = "https://team.city/download/"

  def messageBuilder(viewResultsUrl: String = "", downloadArtifactsUrl: String = "", artifactsPath: String = "", params: Map[String, String] = Map.empty)(implicit build: SBuild): SBuildMessageBuilder = {
    val context = stub[MessageBuilderContext]
    context.getArtifactsPath _ when() returns artifactsPath
    context.getViewResultsUrl _ when() returns (_ ⇒ viewResultsUrl)
    context.getDownloadAllArtifactsUrl _ when() returns (_ ⇒ downloadArtifactsUrl)
    context.userByEmail _ when() returns (x ⇒ Some(x))
    context.getBuildParameter _ when() returns ((_, name) ⇒ params.get(name))
    context.artifactsPublicUrl _ when() returns Some(artifactsPublicUrl)

    new SBuildMessageBuilder(build, context)
  }

  class SQueuedBuildMessageBuilderTest extends FlatSpec with MockFactory with Matchers {
    import SQueuedBuildMessageBuilderTest._

    private val buildSetting = BuildSetting("", "", "art", "", artifactsMask = ".*")

    "SQueuedBuildMessageBuilder.compile" should "compile template with name branch status and link placeholder" in {
      implicit val build: SQueuedBuild = stub[SQueuedBuild]

      val messageTemplate = """{name}
                              |{branch}
                              |{status}
                              |{link}
                            """.stripMargin

      queuedMessageBuilder("Full name", "Branch name", "/some/build/url").compile(messageTemplate, buildSetting) shouldEqual SlackAttachment(
        s"""Full name
           |Branch name
           |${Strings.MessageBuilder.statusQueued}
           |/some/build/url
        """.stripMargin.trim, SBuildMessageBuilder.statusNormalColor, "⚪")
    }
  }

  object SQueuedBuildMessageBuilderTest extends MockFactory {
    def queuedMessageBuilder(queuedBuildName: String, queuedBuildBranch: String ,queuedBuildUrl: String, params: Map[String, String] = Map.empty)(implicit build: SQueuedBuild): SQueuedBuildMessageBuilder = {

      val context = stub[MessageBuilderContext]

      context.getQueuedBuildName _ when() returns (_ => queuedBuildName)
      context.getQueuedBuildBranch _ when() returns (_ => queuedBuildBranch)
      context.getQueuedBuildUrl _ when() returns (_ => queuedBuildUrl)
      context.getQueuedBuildParameter _ when() returns ((_, name) ⇒ params.get(name))

      new SQueuedBuildMessageBuilder(build, context)
    }
  }

  class TestBuildArtifact(name: String, relativePath: String, file: Boolean) extends BuildArtifact {
    override def isArchive = false

    override def isFile = file

    override def isContainer = ???

    override def getInputStream = ???

    override def getChildren = ???

    override def getSize = ???

    override def getTimestamp = ???

    override def isDirectory = !file

    override def getName = name

    override def getRelativePath = relativePath
  }
}
