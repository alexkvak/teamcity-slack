package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.Helpers.Implicits._
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide._
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

class HelpersTest extends AnyFlatSpec with MockFactory with Matchers {
  "RichBuild.branchMask" should "return correct value" in {
    forAll(data) {
      (branchNameOpt: Option[String], branchMask: String, matches: Boolean) =>
        val build = stub[SBuild]
        branchNameOpt.foreach { branchName =>
          val branch = stub[Branch]
          (branch.getDisplayName _).when().returns(branchName)
          (build.getBranch _).when().returns(branch)
        }
        build.matchBranch(branchMask) shouldEqual matches
    }

    def data =
      Table(
        (
          "branchName",
          "branchMask",
          "matches"
        ), // First tuple defines column names
        // Subsequent tuples define the data
        (Some(""), ".*", true),
        (Some("default"), "default", true),
        (Some("release-1099"), "release-\\d+", true),
        (Some("release-branch"), "release-\\d+", false),
        (None, "default", false)
      )
  }

  "RichBuild.branchMask" should "return correct value for empty branch" in {
    val build = stub[SBuild]
    (build.getBranch _).when().returns(null)
    build.matchBranch(".*") shouldEqual true
  }

  "RichBuild.formattedDuration" should "format duration" in {
    forAll(data) { (duration: Long, formatted: String) =>
      val build = stub[SBuild]
      (build.getDuration _).when().returns(duration)

      build.formattedDuration shouldEqual formatted
    }

    def data =
      Table(
        ("duration", "formatted"), // First tuple defines column names
        (1L, "1s"),
        (10L, "10s"),
        (60L, "1m"),
        (670L, "11m:10s"),
        (3600L, "1h"),
        (3780L, "1h:3m"),
        (3782L, "1h:3m:2s"),
        (3602L, "1h:2s"),
        (86402L, "1d:2s"),
        (8640000L, "100d")
      )
  }

  "RichBuildServer.findPreviousStatus" should "work properly" in {
    def stubBranch(branchName: Option[String]) = branchName.map { b =>
      val branch = stub[Branch]
      (branch.getDisplayName _).when().returns(b)
      branch
    }.orNull

    forAll(data) {
      (
          previousBuilds: List[PreviousBuild],
          currentBranch: Option[String],
          expectedStatus: Status
      ) =>
        val buildHistory = stub[BuildHistory]
        val buildServer = stub[SBuildServer]

        val currentBuild = stub[SFinishedBuild]
        (currentBuild.getBranch _).when().returns(stubBranch(currentBranch))

        val buildList = previousBuilds.map { previousBuild =>
          val build = stub[SFinishedBuild]
          (build.isPersonal _).when().returns(false)
          (build.getBuildStatus _).when().returns(previousBuild.status)

          val branch = stubBranch(previousBuild.branchName)
          (build.getBranch _).when().returns(branch)
          build
        }

        buildHistory.getEntriesBefore _ when (currentBuild, false) returns buildList.reverse.asJava
        (buildServer.getHistory _).when().returns(buildHistory)

        new RichBuildServer(buildServer).findPreviousStatus(
          currentBuild
        ) shouldEqual expectedStatus
    }

    case class PreviousBuild(branchName: Option[String], status: Status)
    def data =
      Table(
        (
          "previousBuilds",
          "currentBranch",
          "expectedStatus"
        ), // First tuple defines column names
        // Subsequent tuples define the data
        (
          List(
            PreviousBuild(Some("default"), Status.NORMAL),
            PreviousBuild(Some("master"), Status.FAILURE)
          ),
          Some("default"),
          Status.NORMAL
        ),
        (
          List(
            PreviousBuild(Some("default"), Status.NORMAL),
            PreviousBuild(Some("default"), Status.FAILURE)
          ),
          Some("default"),
          Status.FAILURE
        ),
        (
          List(
            PreviousBuild(None, Status.NORMAL),
            PreviousBuild(Some("default"), Status.FAILURE)
          ),
          None,
          Status.NORMAL
        ),
        (
          List(
            PreviousBuild(None, Status.NORMAL),
            PreviousBuild(None, Status.FAILURE)
          ),
          None,
          Status.FAILURE
        ),
        (
          List(
            PreviousBuild(Some("default"), Status.NORMAL),
            PreviousBuild(Some("master"), Status.FAILURE)
          ),
          None,
          Status.NORMAL
        ),
        (
          List(
            PreviousBuild(Some("default"), Status.NORMAL),
            PreviousBuild(Some("master"), Status.FAILURE)
          ),
          Some("awesome"),
          Status.NORMAL
        ),
        (Nil, Some("awesome"), Status.NORMAL),
        (
          List(
            PreviousBuild(Some("default"), Status.UNKNOWN),
            PreviousBuild(Some("default"), Status.NORMAL),
            PreviousBuild(Some("master"), Status.FAILURE)
          ),
          Some("awesome"),
          Status.NORMAL
        )
      )
  }
}
