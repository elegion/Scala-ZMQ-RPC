package szmq.rpc

import szmq._
import   rpc._
import   rpc.server._

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 8:06 PM
 */

//TODO: convert Ping-Ping sample to unit test, add malformed call, unknown method cases
class PongServer(bindTo: BindTo) {
  val workerNum = 5
  val queue = new RPCQueue(bindTo)

  class PongHandler(val n: Int) extends RPCHandler with BSONSerializer {
    serve {
      case MethodCall("ping", a::b::Nil) => Reply("Pong (%s, %s)" format (a,b))
    }
  }

  def start() {
    queue.start()
    1 to workerNum foreach {n =>
      val worker = new PongHandler(n)
      queue.addWorker(worker)
    }
  }

  def stop() {
    queue.shutdown()
  }
}