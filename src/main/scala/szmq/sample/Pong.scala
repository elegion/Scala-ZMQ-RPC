package szmq.sample

import szmq._
import   rpc._
import   rpc.server._

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 8:06 PM
 */

object Pong extends Application {
  val workerNum = 50

  class PongHandler(val n: Int) extends RPCHandler with BSONSerializer {
    serve {
      case MethodCall("Ping", _) => { Reply("Pong") }
      case MethodCall("Args", List(a, b)) => {
        println("# %s Got args: %s, %s" format (n, a, b))
        Thread.sleep(1000)
        Reply("Args: %s, %s" format (a, b))
      }
    }
  }

  val queue = new RPCQueue().start(BindTo("tcp://*:9999"))

  1 to workerNum foreach {n =>
    queue.addWorker(new PongHandler(n))
  }
}