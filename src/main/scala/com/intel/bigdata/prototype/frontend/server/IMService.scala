package com.intel.bigdata.prototype.frontend.server

import java.util.UUID
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.util._
import spray.http._
import spray.http.HttpMethods._
import spray.http.MediaTypes._
import akka.util.Timeout.durationToTimeout
import spray.http.ContentType.apply
import spray.http.HttpEntity.apply
import spray.http.StatusCode.int2StatusCode
import akka.pattern.ask
import scala.concurrent.Future
import spray.httpx.marshalling._
import com.intel.bigdata.prototype.backend.worker.{Work,Service,ServiceInfo,ServiceTimes}
import com.intel.bigdata.prototype.backend.master.{Router}



class IMService(router: ActorRef) extends Actor with SprayActorLogging {
  implicit val timeout: Timeout = 600.second // for the actor 'asks'
  private implicit val system = context.system
  import context.dispatcher // ExecutionContext for the futures and scheduler

  import spray.httpx.SprayJsonSupport._

  def nextWorkId(): String = UUID.randomUUID().toString

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! index

    case HttpRequest(GET, Uri.Path("/stopIM"), _, _, _) =>
      sender ! HttpResponse(entity = "Shutting down in 1 second ...")
      context.system.scheduler.scheduleOnce(1.second) { context.system.shutdown() }
      
    case HttpRequest(GET, Uri.Path(path), _, _, _) if path startsWith "/start"  => 
      processAgentServiceRequest(path, "start")

    case HttpRequest(GET, Uri.Path(path), _, _, _) if path startsWith "/stop"  => 
      processAgentRequest(path, "stop")

    case HttpRequest(GET, Uri.Path(path), _, _, _) if path startsWith "/status"  => 
      processAgentRequest(path, "status")

    case HttpRequest(GET, Uri.Path(path), _, _, _) if path startsWith "/config"  => 
      processAgentRequest(path, "config")

      
    case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!")
  }

  def processAgentRequest(path: String, action: String) = {     
      val imRequestSender = sender	  
      val work = Work(nextWorkId(), action)
      val future = router ? work
      future onSuccess {
	     case Router.Ok => {
	        val msg = "work completed " + work.workId
	        imRequestSender ! HttpResponse(entity = msg)
	     } 
	     case Router.NotOk => {
	        val msg = "work failed " + work.workId
	        imRequestSender ! HttpResponse(entity = msg)
	     }
      }
    }

  def processAgentServiceRequest(path: String, action: String) = {     
      val imRequestSender = sender	  
      val service = Service(nextWorkId(), action)
      val future = router ? service
      future onSuccess {
	     case Router.Ok => {
	        val msg = "service launched " + service.id
                val info = router ? ServiceInfo(service)
                info onSuccess {
                  case serviceTimes: ServiceTimes =>
                    log.info("IMService got ServiceTimes!!!!")
	            imRequestSender ! HttpResponse(entity = msg)
                  case _ =>
                    log.info("IMService got response!!!!")
                }
	     } 
	     case Router.NotOk => {
	        val msg = "service failed " + service.id
	        imRequestSender ! HttpResponse(entity = msg)
	     }
      }
    }
 
  ////////////// helpers //////////////

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1><i>Akka-spray prototype</i>!</h1>
    	  <p>Service name: <input id="serviceName" text="serviceName"></input></p>
          <p>Actions:</p>
          <ul>
            <li><a id="startLink" href="/start">/start</a></li>
            <li><a id="stopLink" href="/stop">/stop</a></li>
            <li><a id="getLink" href="/status">/status</a></li>
            <li><a id="configLink" href="/config">/config</a></li>
    		<li><a href="/instantStatus">/instantStatus</a></li>
            <li><a href="/stopIM">/stopIM</a></li>
          </ul>
    	  <script type="text/javascript">
       		var startLink= document.getElementById('startLink');
       		var stopLink= document.getElementById('stopLink');
       		var getLink= document.getElementById('getLink');
    		var input= document.getElementById('serviceName');
    		input.onchange=input.onkeyup= function() {{
        		startLink.href= startLink.firstChild.data='/start/'+encodeURIComponent(input.value);
        		stopLink.href= stopLink.firstChild.data='/stop/'+encodeURIComponent(input.value);
        		getLink.href= getLink.firstChild.data='/status/'+encodeURIComponent(input.value);
    		}};
    	  </script>
        </body>
      </html>.toString()
    )
  )
}
