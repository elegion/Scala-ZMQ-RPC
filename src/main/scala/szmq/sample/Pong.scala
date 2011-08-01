package szmq.sample

import szmq.Util._
import szmq.BindTo
import org.zeromq.ZMQ._
import szmq.rpc.{BSONSerializer, Reply, MethodCall, RPCHandler}

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 8:06 PM
 */

object Pong extends Application {
  object PongServer extends RPCHandler with BSONSerializer {
    serve {
      case MethodCall("Ping", _) => { Reply("Pong") }
      case MethodCall("Args", List(a, b)) => { Reply("Args: %s, %s" format (a, b)) }
    }
  }

  inContext() { context: Context =>
    rep(context, BindTo("tcp://*:9999")) { s: Socket =>
      PongServer.handleSocket(s)
    }
  }
}