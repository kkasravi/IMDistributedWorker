package com.intel.bigdata.prototype.frontend.server

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.actor.Address
import akka.actor.PoisonPill
import akka.io.IO
import spray.can.Http
import akka.actor.actorRef2Scala
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterSingletonManager
import com.intel.bigdata.prototype.backend.master._
import com.typesafe.config.ConfigFactory

object IMServer extends App {
  var router:akka.actor.ActorRef = null
  def systemName = "im"
  def workTimeout = 10.seconds
  def startServer() = { 
    val config: com.typesafe.config.Config = ConfigFactory.load.getConfig("imServer")
    implicit val system = ActorSystem("imWeb", config)
    val handler = system.actorOf(Props(classOf[IMService], router), name = "handler")
    val imPort:Int = config.getInt("imPort")
    IO(Http) ! Http.Bind(handler, interface = "localhost", port = imPort)
  }
  def startBackend(system: akka.actor.ActorSystem, role: String) = {
    println(Cluster(system).selfAddress)
    Cluster(system).join(Cluster(system).selfAddress)
    system.actorOf(ClusterSingletonManager.props(_ => Master.props(workTimeout), "active", PoisonPill, Some(role)), "master")
  }
  
  def startRouter(system: akka.actor.ActorSystem): Unit = {
    router = system.actorOf(Props[Router], "router")
    system.actorOf(Props[WorkResultConsumer], "consumer")
  }
  
  override def main(args: Array[String]) {
      val conf = ConfigFactory.load.getConfig("imMaster")
      val system = ActorSystem(systemName, conf)
      startBackend(system, "backend")
	  startRouter(system)
	  startServer()
  }

}
