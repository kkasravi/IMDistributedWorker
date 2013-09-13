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
    val host = Cluster(system).selfAddress.host.get
    val address = akka.actor.Address(protocol,sys,host,6236)
    Cluster(system).join(address)
    startWorker(address, system)
  }

}

trait Startup {

  def systemName = "im"
  def workTimeout = 10.seconds

  def startWorker(contactAddress: akka.actor.Address, system: akka.actor.ActorSystem): Unit = {
    val initialContacts = Set(system.actorSelection(RootActorPath(contactAddress) / "user" / "receptionist"))
    val clusterClient = system.actorOf(ClusterClient.props(initialContacts), "clusterClient")
    println(system.actorOf(Worker.props(clusterClient, Props[WorkExecutor]), "worker"))
  }
}
