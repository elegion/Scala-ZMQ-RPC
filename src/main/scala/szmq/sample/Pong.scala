package szmq.sample

import szmq.Util._
import org.zeromq.ZMQ._
import szmq.{ConnectTo, BindTo}
import org.zeromq.{ZMQQueue, ZMQ}
import java.util.Queue
import szmq.rpc._

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
        println("#"+n+" Got args: "+a+", "+b)
        Thread.sleep(1000)
        println("#"+n+" replying: "+a+", "+b)
        Reply("Args: %s, %s" format (a, b))
      }
    }
  }

  val queue = new RPCQueue().start(BindTo("tcp://*:9999"))

  1 to workerNum foreach {n =>
    queue.addWorker(new PongHandler(n))
  }
}