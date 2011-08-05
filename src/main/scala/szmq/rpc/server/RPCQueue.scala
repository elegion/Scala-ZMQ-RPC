package szmq.rpc.server

import szmq._
import  Util._
import org.zeromq._
import  ZMQ._
import   Context._
import com.twitter.ostrich.stats.Stats
import com.twitter.ostrich.admin.{ServiceTracker, Service}


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

  def backendEPUri = "inproc://"+id
  val backendEP = BindTo(backendEPUri)
  val workerEP = ConnectTo(backendEPUri)
  var workerSet = scala.collection.mutable.Set.empty[RPCHandler]
  var queueThread: Option[Thread] = None
  lazy val stats = Stats.make(id)
  ServiceTracker.register(this)

  def start() {
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
    workerSet.synchronized{
      workerSet foreach (_.stop())
    }
    queueThread foreach (_.interrupt())
    queueThread = None
  }

  def addWorker(handler: RPCHandler) {
    workerSet.synchronized{
      workerSet += handler
      startWorker(handler)
    }
    stats.incr("workers")
  }

  def startWorker(handler: RPCHandler) {
    thread {
      rep(ctx, workerEP) { s: Socket =>
        handler.handleSocket(ctx, s)
      }
    }
  }

}