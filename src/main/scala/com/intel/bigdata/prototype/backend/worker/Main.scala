package com.intel.bigdata.prototype.backend.worker

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Address
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterClient
import akka.contrib.pattern.ClusterSingletonManager

object Main extends Startup {

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load.getConfig("imAgent")
    val system = ActorSystem(systemName, conf)
    val protocol = Cluster(system).selfAddress.protocol
    val sys = Cluster(system).selfAddress.system
    
    val masterConf = ConfigFactory.load.getConfig("imServer")
    val host:String = masterConf.getString("akka.remote.netty.tcp.hostname")
    val port:Int = masterConf.getInt("akka.remote.netty.tcp.port")
    
    val address = akka.actor.Address(protocol,sys,host,port)
    Cluster(system).join(address)
    startWorker(address, system)
  }

}

trait Startup {

  def systemName = "im"
  def workTimeout = 30.seconds

  def startWorker(contactAddress: akka.actor.Address, system: akka.actor.ActorSystem): Unit = {
    val initialContacts = Set(system.actorSelection(RootActorPath(contactAddress) / "user" / "receptionist"))
    val clusterClient = system.actorOf(ClusterClient.props(initialContacts), "clusterClient")
    system.actorOf(Worker.props(clusterClient, Props[WorkExecutor]), "worker")
    
    val heartbeatSender = system.actorOf(Props[HeartbeatSender], name="HeartbeatSender")

    //Use system's dispatcher as ExecutionContext
	import system.dispatcher

  	//This will schedule to send Heartbeat-message every HeartbeatInterval ms
	val interval = Settings(system).HeartbeatInterval
	if (interval > 0) {
		val cancellable = system.scheduler.schedule(0 milliseconds, interval milliseconds, heartbeatSender, Heartbeat)	  
	}
  }
}
