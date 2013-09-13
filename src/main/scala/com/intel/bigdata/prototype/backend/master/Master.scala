package com.intel.bigdata.prototype.backend.master

import scala.collection.immutable.Queue
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator
import akka.contrib.pattern.DistributedPubSubMediator.Put
import akka.contrib.pattern.ClusterClient.SendToAll
import scala.concurrent.duration.Deadline
import scala.concurrent.duration.FiniteDuration
import akka.actor.Props
import com.intel.bigdata.prototype.backend.worker._

object Master {

  val ResultsTopic = "results"
  val ServiceTopic = "service"

  def props(workTimeout: FiniteDuration): Props =
    Props(classOf[Master], workTimeout)

  case class Ack(workId: String)

  private sealed trait WorkerStatus
  private case object Idle extends WorkerStatus
  private case class Busy(work: Work, deadline: Deadline) extends WorkerStatus
  private case class WorkerState(ref: ActorRef, status: WorkerStatus)

  private case object CleanupTick

}

class Master(workTimeout: FiniteDuration) extends Actor with ActorLogging {
  import Master._
  import MasterWorkerProtocol._
  val mediator = DistributedPubSubExtension(context.system).mediator

  mediator ! Put(self)

  private var workers = Map[String, WorkerState]()
  private var serviceProgress = Map[String, Integer]()
  private var pendingWork = Queue[Work]()
  private var workIds = Set[String]()

  import context.dispatcher
  val cleanupTask = context.system.scheduler.schedule(workTimeout / 2, workTimeout / 2,
    self, CleanupTick)

  override def postStop(): Unit = cleanupTask.cancel()

  def receive = {
    case RegisterWorker(workerId) =>
      if (workers.contains(workerId)) {
        workers += (workerId -> workers(workerId).copy(ref = sender))
      } else {
        log.info("Worker registered: {}", workerId)
        workers += (workerId -> WorkerState(sender, status = Idle))
        if (pendingWork.nonEmpty)
          sender ! WorkIsReady
      }

    case WorkerRequestsWork(workerId) =>
      if (pendingWork.nonEmpty) {
        workers.get(workerId) match {
          case Some(s @ WorkerState(_, Idle)) =>
            val (work, rest) = pendingWork.dequeue
            pendingWork = rest
            log.info("Giving worker {} some work {}", workerId, work.job)
            // TODO store in Eventsourced
            sender ! work
            workers += (workerId -> s.copy(status = Busy(work, Deadline.now + workTimeout)))
          case _ =>

        }
      }

    case WorkIsDone(workerId, workId, result) =>
      workers.get(workerId) match {
        case Some(s @ WorkerState(_, Busy(work, _))) if work.workId == workId =>
          log.info("Work is done: {} => {} by worker {}", work, result, workerId)
          // TODO store in Eventsourced
          workers += (workerId -> s.copy(status = Idle))
          mediator ! DistributedPubSubMediator.Publish(ResultsTopic, WorkResult(workId, result))
          sender ! MasterWorkerProtocol.Ack(workId)
        case _ =>
          if (workIds.contains(workId)) {
            // previous Ack was lost, confirm again that this is done
            sender ! MasterWorkerProtocol.Ack(workId)
          }
      }

    case ServiceIsComplete(workerId, workId, result) =>
      workers.get(workerId) match {
        case _ =>
          log.info(s"Master State:ServiceIsComplete workId=$workId workerId=$workerId");
          if (serviceProgress.contains(workId)) {
            log.info(s"MasterWorkerProtocol.Ack $workId!!!");
            sender ! MasterWorkerProtocol.Ack(workId)
            val count:Integer = serviceProgress.get(workId).get + 1
            serviceProgress += (workId -> count)
            if (serviceProgress.get(workId).get >= workers.size) {
              log.info(s"Master All Workers done!!! workId=$workId");
            }
          }
      }

    case WorkFailed(workerId, workId) =>
      workers.get(workerId) match {
        case Some(s @ WorkerState(_, Busy(work, _))) if work.workId == workId =>
          log.info("Work failed: {}", work)
          // TODO store in Eventsourced
          workers += (workerId -> s.copy(status = Idle))
          pendingWork = pendingWork enqueue work
          notifyWorkers()
        case _ =>
      }

    case work: Work =>
      // idempotent
      if (workIds.contains(work.workId)) {
        sender ! Master.Ack(work.workId)
      } else {
        log.info("Master Accepted work: {}", work)
        // TODO store in Eventsourced
        pendingWork = pendingWork enqueue work
        workIds += work.workId
        sender ! Master.Ack(work.workId)
        notifyWorkers()
      }

    case service: Service =>
      mediator ! DistributedPubSubMediator.Publish(ServiceTopic, Service(service.id,service.command))
      sender ! Master.Ack(service.id)
      serviceProgress += (service.id -> 0)
      workIds += service.id

    case CleanupTick =>
      for ((workerId, s @ WorkerState(_, Busy(work, timeout))) <- workers) {
        if (timeout.isOverdue) {
          log.info("Work timed out: {}", work)
          // TODO store in Eventsourced
          workers -= workerId
          pendingWork = pendingWork enqueue work
          notifyWorkers()
        }
      }
  }

  def notifyWorkers(): Unit =
    if (pendingWork.nonEmpty) {
      // could pick a few random instead of all
      log.info("Master notifyWorkers!!!");
      workers.foreach {
        case (_, WorkerState(ref, Idle)) => 
          log.info("Master notifyWorkers!!!");
          ref ! WorkIsReady
        case _                           => // busy
      }
    }

  // TODO cleanup old workers
  // TODO cleanup old workIds

}
