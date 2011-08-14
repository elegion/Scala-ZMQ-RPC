package szmq.sample

import szmq._
import   rpc._
import   rpc.server._

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 8:06 PM
 */

//TODO: convert Ping-Ping sample to unit test, add malformed call, unknown method cases
object Pong extends Application {
  val workerNum = 5

  class PongHandler(val n: Int) extends RPCHandler with BSONSerializer {
    serve {
      case MethodCall("ping", Nil) => Reply("Pong")
      case MethodCall("args", a::b::Nil) => {
        println("# %s Got args: %s, %s" format (n, a, b))
        Thread.sleep(1000)
        Reply("args: %s, %s" format (a, b))
      }
    }
  }

  val queue = new RPCQueue(BindTo("tcp://*:9999"))
  queue.start()

  1 to workerNum foreach {n =>
    queue.addWorker(new PongHandler(n))
  }

  readLine()
  print("Stopping...")
  queue.shutdown()
}