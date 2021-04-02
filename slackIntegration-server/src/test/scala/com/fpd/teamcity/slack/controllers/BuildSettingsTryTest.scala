package com.fpd.teamcity.slack.controllers

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import com.fpd.teamcity.slack.SlackGateway.{
  Destination,
  SlackChannel,
  SlackUser
}
import jetbrains.buildServer.serverSide.{Branch, SFinishedBuild}
import jetbrains.buildServer.users.SUser
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class BuildSettingsTryTest extends AnyFlatSpec with MockFactory with Matchers {
  "BuildSettingsTry.findPreviousBuild" should "work" in {
    val buildTypeId = "MyBuildTypeId"

    // Branches
    val branchMaster = stub[Branch]
    (branchMaster.getDisplayName _).when().returns("master")

    val branchDefault = stub[Branch]
    (branchDefault.getDisplayName _).when().returns("default")

    // Builds
    val buildWithoutBranch = stub[SFinishedBuild]
    (buildWithoutBranch.getBranch _).when().returns(null)
    (buildWithoutBranch.getBuildTypeId _).when().returns(buildTypeId)

    val buildDefault = stub[SFinishedBuild]
    (buildDefault.getBranch _).when().returns(branchDefault)
    (buildDefault.getBuildTypeId _).when().returns(buildTypeId)

    val buildMaster = stub[SFinishedBuild]
    (buildMaster.getBranch _).when().returns(branchMaster)
    (buildMaster.getBuildTypeId _).when().returns(buildTypeId)

    val buildPersonal = stub[SFinishedBuild]
    (buildPersonal.isPersonal _).when().returns(true)

    val buildWithPrevious = stub[SFinishedBuild]
    (buildWithPrevious.isPersonal _).when().returns(true)
    (buildWithPrevious.getPreviousFinished _).when().returns(buildMaster)

    // settings
    val settingMatchAll = BuildSetting(buildTypeId, ".*", "", "")
    val settingMatchDefault = BuildSetting(buildTypeId, "default", "", "")

    // Assertion
    forAll(data) {
      (
          build: SFinishedBuild,
          buildSetting: BuildSetting,
          found: Option[SFinishedBuild]
      ) =>
        BuildSettingsTry.filterMatchBuild(buildSetting, build) shouldEqual found
    }

    def data =
      Table(
        ("build", "buildSetting", "found"), // First tuple defines column names
        // Subsequent tuples define the data
        (buildWithoutBranch, settingMatchAll, Some(buildWithoutBranch)),
        (buildDefault, settingMatchDefault, Some(buildDefault)),
        (buildMaster, settingMatchDefault, None),
        (buildPersonal, settingMatchAll, None),
        (buildWithPrevious, settingMatchAll, Some(buildMaster))
      )
  }

  "BuildSettingsTry.detectDestination" should "work" in {
    forAll(data) {
      (setting: BuildSetting, user: SUser, expected: Option[Destination]) =>
        BuildSettingsTry.detectDestination(setting, user) shouldEqual expected
    }

    def data = {
      val buildTypeId = "MyBuildTypeId"
      val email = "email@email.com"
      val channelName = "general"
      val branchName = "default"

      val user = stub[SUser]
      (user.getEmail _).when().returns(email)

      Table(
        ("setting", "user", "expected"), // First tuple defines column names
        // Subsequent tuples define the data
        (BuildSetting(buildTypeId, branchName, "", ""), user, None),
        (
          BuildSetting(buildTypeId, branchName, channelName, ""),
          user,
          Some(SlackChannel(channelName))
        ),
        (
          BuildSetting(buildTypeId, branchName, "", "", notifyCommitter = true),
          user,
          Some(SlackUser(email))
        )
      )
    }
  }
}
