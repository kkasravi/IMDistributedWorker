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
import spray.json._
import DefaultJsonProtocol._
import akka.util.Timeout.durationToTimeout
import spray.http.ContentType.apply
import spray.http.HttpEntity.apply
import spray.http.StatusCode.int2StatusCode
import akka.pattern.ask
import scala.concurrent.Future
import spray.httpx.marshalling._
import com.intel.bigdata.prototype.backend.worker.{Work,Service,ServiceInfo,ServiceTimes}
import com.intel.bigdata.prototype.backend.master.{Router}
import com.intel.bigdata.prototype.api.CommandRequest
import com.intel.bigdata.prototype.api.ResultWithTime
import com.intel.bigdata.prototype.api.Result
import com.intel.bigdata.prototype.api.State


class IMService(router: ActorRef) extends Actor with SprayActorLogging {
  implicit val timeout: Timeout = 600.second // for the actor 'asks'
  private implicit val system = context.system
  import context.dispatcher // ExecutionContext for the futures and scheduler

  import net.liftweb.json._
  import net.liftweb.json.ext._

  import spray.httpx.SprayJsonSupport._
  implicit val formats = net.liftweb.json.DefaultFormats + new EnumNameSerializer(Result) + new EnumNameSerializer(State) 


  var serviceId2requestSender: Map[String, ActorRef] = Map()

  def nextWorkId(): String = UUID.randomUUID().toString

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! index

    case httpRequest: HttpRequest =>
      httpRequest.uri.path.toString match {
        case "/stopIM" =>
          sender ! HttpResponse(entity = "Shutting down in 1 second ...")
          context.system.scheduler.scheduleOnce(1.second) { context.system.shutdown() }
          
        case path : String if path startsWith "/start" =>
          processAgentServiceRequest(httpRequest.uri.path.toString, "start")
        case path : String if path startsWith "/stop" =>
          processAgentServiceRequest(httpRequest.uri.path.toString, "stop")
        case path : String if path startsWith "/status" =>
          processAgentServiceRequest(httpRequest.uri.path.toString, "status")
        case path : String if path startsWith "/config" =>
          processAgentServiceRequest(httpRequest.uri.path.toString, "config")
        case path : String if path startsWith "/info" =>
          httpRequest.uri.query.get("service").map(
            processAgentServiceInfo(_)
          )
        case "/instantStatus"  =>
          val statusAggregator = context.actorSelection("/user/StatusAggregator")
          statusAggregator ! GetStatusesRequest(sender)
        case _ => sender ! HttpResponse(status = 404, entity = "Unknown resource!")
    }
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

  def processAgentServiceRequest(path: String, command: String) = {     
      val imRequestSender = sender	  
      val service = Service(nextWorkId(), CommandRequest(path.stripPrefix("/" + command + "/"), command, null))
      val future = router ? service
      future onSuccess {
	     case Router.Ok => {
	       serviceId2requestSender += (service.id -> imRequestSender)
	        val msg = """{ "service": { "id": """"+service.id+"""", "status": "success"}}"""
                imRequestSender ! HttpResponse(entity = msg.toJson.prettyPrint)
	     } 
	     case Router.NotOk => {
	        val msg = """{ "service": { "id": """"+service.id+"""", "status": "failure"}}"""
	        imRequestSender ! HttpResponse(entity = msg)
	     }
      }
    }
 
  def processAgentServiceInfo(serviceId: String) = {     
      val imRequestSender = sender	  
      val service = Service(serviceId, "info")
      val info = router ? ServiceInfo(service)
      info onSuccess {
        case serviceTimes: ServiceTimes =>
	  val msg = """{ "service": { "id": """"+serviceTimes.service.id+"""", "timesPerWorker": """+serviceTimes.timesPerWorker.toJson.prettyPrint+""", "completionTime": """+serviceTimes.completionTime.toJson.prettyPrint+"""}}"""
	  imRequestSender ! HttpResponse(entity = msg)
        }
    }
  ////////////// helpers //////////////

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
    	  <script type="text/javascript">
            <!--
            var serviceId;
            function getServiceId(command) {
               var xmlhttp = new XMLHttpRequest();
               xmlhttp.onreadystatechange=function() {
                 if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                   serviceId=JSON.parse(eval(xmlhttp.responseText)).service.id
                   setTimeout(getServiceTimes,30000)
    				alert(xmlhttp.responseText);
                 }
               }
               xmlhttp.open("GET",command,true);
               xmlhttp.send();
            }
            function getServiceTimes() {
               var xmlhttp = new XMLHttpRequest();
               xmlhttp.onreadystatechange=function() {
                 if (xmlhttp.readyState==4 && xmlhttp.status==200) {
                   //var completionTime=JSON.parse(xmlhttp.responseText).service.completionTime;
    				alert(xmlhttp.responseText);
                 }
               }
               xmlhttp.open("GET","/info?service="+serviceId,true);
               xmlhttp.send();
            }
            -->
    	  </script>
          <h1><i>IMDistributedWorkers prototype</i>!</h1>
    	  <p>Service name: <input id="serviceName" text="serviceName"></input></p>
          <p>Actions:</p>
          <ul>
            <li><a id="startLink" href="#" onclick="getServiceId(startLink.firstChild.data)">/start</a></li>
            <li><a id="stopLink" href="#" onclick="getServiceId(stopLink.firstChild.data)">/stop</a></li>
            <li><a id="getLink" href="#" onclick="getServiceId(getLink.firstChild.data)">/status</a></li>
            <li><a id="configLink" href="#" onclick="getServiceId('/config')">/config</a></li>
            <li><a id="instantStatusLink" href="#" onclick="getServiceId('/instantStatus')">/instantStatus</a></li>
            <li><a href="/stopIM">/stopIM</a></li>
          </ul>
    	  <script type="text/javascript">
       		var startLink= document.getElementById('startLink');
       		var stopLink= document.getElementById('stopLink');
       		var getLink= document.getElementById('getLink');
    		var input= document.getElementById('serviceName');
    		input.onchange=input.onkeyup= function() {{
        		startLink.firstChild.data='/start/'+encodeURIComponent(input.value);
        		stopLink.firstChild.data='/stop/'+encodeURIComponent(input.value);
        		getLink.firstChild.data='/status/'+encodeURIComponent(input.value);
    		}};
    	  </script>

        </body>
      </html>.toString()
    )
  )
}
