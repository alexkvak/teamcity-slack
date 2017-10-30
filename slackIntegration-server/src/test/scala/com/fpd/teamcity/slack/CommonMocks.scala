package com.fpd.teamcity.slack

import com.fpd.teamcity.slack.ConfigManager.Config
import jetbrains.buildServer.serverSide.ServerPaths
import org.scalamock.scalatest.MockFactory

trait CommonMocks extends MockFactory {
  val manager = new ConfigManagerStub

  class ConfigManagerStub extends ConfigManager(new ServerPaths("")) {
    override protected def backup(): Boolean = true
    override protected def persist(): Boolean = true

    override protected def readConfig: Option[Config] = None

    def setConfig(newConfig: Config): Unit = config = Some(newConfig)
  }
}
