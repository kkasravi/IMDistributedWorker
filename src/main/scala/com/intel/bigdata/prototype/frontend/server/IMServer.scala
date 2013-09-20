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
import com.typesafe.config.Config

object IMServer extends App {
  var router:akka.actor.ActorRef = null
  def systemName = "im"
  def workTimeout = 30.seconds
  def startServer(system: akka.actor.ActorSystem, config: Config) = { 
    val statusAggregator = system.actorOf(Props[StatusAggregator], name = "StatusAggregator")

    val handler = system.actorOf(Props(classOf[IMService], router), name = "handler")
    val imPort:Int = config.getInt("imPort")
    IO(Http)(system) ! Http.Bind(handler, interface = "localhost", port = imPort)    
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
      val config = ConfigFactory.load.getConfig("imServer")
      val system = ActorSystem("im", config)
      
//      val conf = ConfigFactory.load.getConfig("imMaster")
//      val system = ActorSystem(systemName, conf)
      startBackend(system, "backend")
	  startRouter(system)
	  startServer(system, config)
  }

}
