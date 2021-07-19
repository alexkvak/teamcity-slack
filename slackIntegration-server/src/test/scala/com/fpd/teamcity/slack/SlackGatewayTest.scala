package com.fpd.teamcity.slack

import jetbrains.buildServer.serverSide.ServerPaths
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SlackGatewayTest
    extends AnyFlatSpec
    with MockFactory
    with Matchers
    with BeforeAndAfterEach {

  override def beforeEach() {
    System.clearProperty("slack.proxyHost")
    System.clearProperty("slack.proxyPort")
    System.clearProperty("http.proxyHost")
    System.clearProperty("http.proxyPort")
    System.clearProperty("https.proxyHost")
    System.clearProperty("https.proxyPort")
    super.beforeEach() // To be stackable, must call super.beforeEach
  }

  "SlackGateway.prepareConfig" should "return config without proxy" in {
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual null
  }

  "SlackGateway.prepareConfig" should "return config with slack proxy host" in {
    System.setProperty("slack.proxyHost", "proxy.com")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:80"
  }

  "SlackGateway.prepareConfig" should "return config with slack proxy host and port" in {
    System.setProperty("slack.proxyHost", "proxy.com")
    System.setProperty("slack.proxyPort", "1234")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:1234"
  }

  "SlackGateway.prepareConfig" should "return config with slack proxy host and ignore non-number port" in {
    System.setProperty("slack.proxyHost", "proxy.com")
    System.setProperty("slack.proxyPort", "wrong port")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:80"
  }

  "SlackGateway.prepareConfig" should "return config with teamcity proxy host" in {
    System.setProperty("http.proxyHost", "proxy.com")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:80"
  }

  "SlackGateway.prepareConfig" should "return config with teamcity proxy host and port" in {
    System.setProperty("http.proxyHost", "proxy.com")
    System.setProperty("http.proxyPort", "1234")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:1234"
  }

  "SlackGateway.prepareConfig" should "return config with teamcity proxy host (https)" in {
    System.setProperty("https.proxyHost", "proxy.com")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:80"
  }

  "SlackGateway.prepareConfig" should "return config with teamcity proxy host and port (https)" in {
    System.setProperty("https.proxyHost", "proxy.com")
    System.setProperty("https.proxyPort", "1234")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://proxy.com:1234"
  }

  "SlackGateway.prepareConfig" should "return correct config if both http and https proxy settings is set" in {
    System.setProperty("http.proxyHost", "http-proxy.com")
    System.setProperty("http.proxyPort", "1234")
    System.setProperty("http.proxyLogin", "http-user")
    System.setProperty("http.proxyPassword", "http-pass")

    System.setProperty("https.proxyHost", "https-proxy.com")
    System.setProperty("https.proxyPort", "5678")
    System.setProperty("https.proxyLogin", "https-user")
    System.setProperty("https.proxyPassword", "https-pass")
    val config = SlackGateway.prepareConfig

    config.getProxyUrl shouldEqual "http://http-user:http-pass@http-proxy.com:1234"
  }

  "SlackGateway" should "be instantiated with proxy" in {
    System.setProperty("slack.proxyHost", "proxy.com")
    System.setProperty("slack.proxyPort", "wrong port")

    val serverPaths = new ServerPaths("", "", "", "")
    val configManager = new ConfigManager(serverPaths)
    val logger = stub[Logger]

    new SlackGateway(configManager, logger)
  }
}
