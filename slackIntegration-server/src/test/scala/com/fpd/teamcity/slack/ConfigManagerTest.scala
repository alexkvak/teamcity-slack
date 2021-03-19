package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigManagerTest extends AnyFlatSpec with MockFactory with Matchers {
  "updateBuildSetting" should "preserve build settings when changing API key" in new CommonMocks {
    val key = "SomeKey"
    manager.update(
      key,
      "",
      personalEnabled = true,
      enabled = true,
      "",
      sendAsAttachment = true
    )
    manager.oauthKey shouldEqual Some(key)

    private val buildSetting = BuildSetting("", "", "", "{name}", Set.empty)
    manager.updateBuildSetting(buildSetting, None)
    manager.oauthKey shouldEqual Some(key)
    manager.allBuildSettingList.values.toSet shouldEqual Set(buildSetting)
  }
}
