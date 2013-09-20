package com.intel.bigdata.prototype.backend.worker

import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import com.typesafe.config.Config


class SettingsImpl(config: Config) extends Extension {
  val LRDelta: Int = config.getInt("longRunningDelta")
  val LRDelay: Int = config.getInt("longRunningDelay")
  val ConfigPayloadFile: String = config.getString("configPayloadFile")
  val StatusPayloadFile: String = config.getString("statusPayloadFile")
  val Host: String = config.getString("akka.remote.netty.tcp.hostname")
  val Port: String = config.getString("akka.remote.netty.tcp.port")
  val HeartbeatInterval: Int = config.getInt("heartbeatInterval")
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)
}

