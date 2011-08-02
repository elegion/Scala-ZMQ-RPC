package szmq.rpc

import szmq._
import  Util._
import org.zeromq._
import  ZMQ._
import   Context._


/**
 * Author: Yuri Buyanov
 * Date: 8/2/11 12:08 PM
 */

class RPCQueue(
                val id: String = getClass.getName,
                val ctx: Context = context(DefaultConfig.ioThreads)) {

  def backendEPUri = "inproc://"+id
  val backendEP = BindTo(backendEPUri)
  val workerEP = ConnectTo(backendEPUri)

  def start(frontendEP: Endpoint) = {
    withContext(ctx) { context: Context =>
      thread {
        router(context, frontendEP) { frontend =>
          dealer(context, backendEP) { backend =>
            println("starting queue")
            new ZMQQueue(context, frontend, backend).run()
          }
        }
      }
    }
    this
  }

  def addWorker(handler: RPCHandler) {
    thread {
      rep(ctx, workerEP) { s: Socket =>
        handler.handleSocket(s)
      }
    }
  }

}