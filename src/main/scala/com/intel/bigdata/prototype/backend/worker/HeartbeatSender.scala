package com.intel.bigdata.prototype.backend.worker

import akka.actor.ActorSystem
import com.intel.bigdata.prototype.frontend.server.StatusUpdate
import scala.reflect.io.File
import com.typesafe.config.ConfigFactory
import akka.actor.Actor

case class Heartbeat
case class NodeRegistration

class HeartbeatSender extends Actor {
  // Read from the config IM server address
  val imCfg = ConfigFactory.load.getConfig("imServer")
  val imHost = imCfg.getString("akka.remote.netty.tcp.hostname")
  val imPort = imCfg.getString("akka.remote.netty.tcp.port")
    
  def heartbeatStatus(node: String, system: ActorSystem) : StatusUpdate = {
    val is = getClass.getResourceAsStream(Settings(system).StatusPayloadFile)
    val statusStr = scala.io.Source.fromInputStream(is).getLines().mkString("\n")
    return StatusUpdate(node, System.currentTimeMillis(), statusStr)
  }

  def sendStatus(system: ActorSystem) = {
    val statusAggregator = system.actorSelection("akka.tcp://im@" + imHost + ":" + imPort + "/user/StatusAggregator")
    statusAggregator ! heartbeatStatus(Settings(system).Host + ":" + Settings(system).Port, system)
  }
  
  def receive = {
    case Heartbeat => sendStatus(context.system)
  }
}