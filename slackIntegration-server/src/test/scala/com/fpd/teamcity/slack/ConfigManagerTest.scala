package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import jetbrains.buildServer.serverSide.ServerPaths
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ConfigManagerTest extends FlatSpec with MockFactory with Matchers {
  "updateBuildSetting" should "preserve build settings when changing API key" in {
    val manager = new ConfigManagerStub
    val key = "SomeKey"
    manager.updateAuthKey(key)
    manager.oauthKey shouldEqual Some(key)

    val buildSetting = BuildSetting("", "", "", "{name}", Set.empty)
    manager.updateBuildSetting(buildSetting, None)
    manager.oauthKey shouldEqual Some(key)
    manager.allBuildSettingList.values.toSet shouldEqual Set(buildSetting)
  }

  class ConfigManagerStub extends ConfigManager(new ServerPaths("")) {
    override def persist(newConfig: ConfigManager.Config): Boolean = true

    override protected def readConfig: Option[ConfigManager.Config] = None
  }

}
