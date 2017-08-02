package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.Helpers.Implicits._
import jetbrains.buildServer.serverSide.{Branch, SBuild}
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{FlatSpec, Matchers}

class HelpersTest extends FlatSpec with MockFactory with Matchers {
  "RichBuild.branchMask" should "return correct value" in {
    forAll(data) { (branchNameOpt: Option[String], branchMask: String, matches: Boolean) ⇒
      val build = stub[SBuild]
      branchNameOpt.foreach { branchName ⇒
        val branch = stub[Branch]
        branch.getDisplayName _ when() returns branchName
        build.getBranch _ when() returns branch
      }
      build.matchBranch(branchMask) shouldEqual matches
    }

    def data =
      Table(
        ("branchName", "branchMask", "matches"), // First tuple defines column names
        // Subsequent tuples define the data
        (Some("default"), "default", true),
        (Some("release-1099"), "release-\\d+", true),
        (Some("release-branch"), "release-\\d+", false),
        (None, "default", false)
      )
  }
}
