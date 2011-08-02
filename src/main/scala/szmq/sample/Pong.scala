package szmq.sample

import szmq.Util._
import org.zeromq.ZMQ._
import szmq.rpc.{BSONSerializer, Reply, MethodCall, RPCHandler}
import szmq.{ConnectTo, BindTo}
import org.zeromq.{ZMQQueue, ZMQ}
import java.util.Queue

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 8:06 PM
 */

object Pong extends Application {
  val workerNum = 50

  class PongServer(val n: Int) extends  RPCHandler with BSONSerializer {
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

  inContext() { context: Context =>
    new Thread() {
      override def run() {
        router(context, BindTo("tcp://*:9999")) { frontend =>
          dealer(context, BindTo("tcp://*:9998")) { backend =>
            println("starting queue")
            new ZMQQueue(context, frontend, backend).run()
          }
        }
      }
    }.start()

    1 to workerNum foreach {n =>
      new Thread() {
        override def run() {
          rep(context, ConnectTo("tcp://localhost:9998")) { s: Socket =>
            new PongServer(n).handleSocket(s)
          }
        }
      }.start()
    }



  }
}