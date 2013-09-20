package com.intel.bigdata.prototype.frontend.server

import akka.actor.Actor
import akka.actor.ActorRef
import com.intel.bigdata.prototype.api.ResultWithTime

case class GetStatusesRequest(replyTo: ActorRef)
case class GetStatusesResponse(replyTo: ActorRef, statuses : ResultWithTime[List[StatusUpdate]])
case class StatusUpdate(node: String, timestamp: Long, status: String)

class StatusAggregator extends Actor {
	var statuses : Map[String, StatusUpdate] = Map() // node -> xml status
	
	def receive = {
	  	case request: GetStatusesRequest => {
	  	  val oldestTimestamp: Long = {
	  	    if (statuses.values.isEmpty) {
	  	      System.currentTimeMillis()
	  	    } else {
	  	      statuses.values.min(Ordering.by((su:StatusUpdate) => su.timestamp)).timestamp	  	      
	  	    }
	  	  }
	  	  val time = System.currentTimeMillis() - oldestTimestamp
	  	  sender ! GetStatusesResponse(request.replyTo, ResultWithTime(time, statuses.values.toList))
	  	}
	  	case update: StatusUpdate => statuses += (update.node -> update)	  	
	}
}