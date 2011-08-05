package szmq.rpc.server

import szmq._
import  Util._
import org.zeromq._
import  ZMQ._
import   Context._
import com.twitter.ostrich.stats.Stats
import com.twitter.ostrich.admin.{ServiceTracker, Service}
import com.twitter.logging.Logger


/**
 * Author: Yuri Buyanov
 * Date: 8/2/11 12:08 PM
 */


//TODO: more safe start/shutdown
//sockets open/close on start/shutdown
class RPCQueue(
                val frontendEP: Endpoint,
                val id: String = getClass.getName,
                val ctx: Context = context(DefaultConfig.ioThreads)) extends Service {

  val log = Logger.get(id)
  def backendEPUri = "inproc://"+id
  val backendEP = BindTo(backendEPUri)
  val workerEP = ConnectTo(backendEPUri)
  var workerSet = scala.collection.mutable.Set.empty[RPCHandler]
  var queueThread: Option[Thread] = None
  lazy val stats = Stats
  ServiceTracker.register(this)

  def start() {
    log.debug("Starting")

    withContext(ctx) { context: Context =>
      queueThread = Some(thread {
        router(context, frontendEP) { frontend =>
          stats.setLabel("frontend", frontendEP.toString)
          dealer(context, backendEP) { backend =>
            new ZMQQueue(context, frontend, backend).run()
          }
        }
      })

      workerSet.synchronized {
        workerSet foreach (startWorker _)
      }

    }
  }


  def shutdown() {
    log.debug("Shutting down")

    workerSet.synchronized{
      workerSet foreach (_.stop())
    }
    queueThread foreach (_.interrupt())
    queueThread = None
  }

  def addWorker(handler: RPCHandler) {
    log.debug("Adding worker: %s", handler.toString)

    workerSet.synchronized{
      workerSet += handler
      startWorker(handler)
    }
    stats.incr("workers")
  }

  def startWorker(handler: RPCHandler) {
    log.debug("Starting worker: %s", handler.toString)
    thread {
      rep(ctx, workerEP) { s: Socket =>
        handler.handleSocket(ctx, s)
      }
    }
  }

}