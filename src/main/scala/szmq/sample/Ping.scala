package szmq.sample

import szmq.Util._
import szmq.BindTo._
import org.zeromq.ZMQ._
import szmq.ConnectTo
import org.msgpack.ScalaMessagePack
import szmq.rpc.{Reply, MethodCall}

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 10:14 PM
 */

object Ping extends Application {
  inContext() { context: Context =>
    req(context, ConnectTo("tcp://localhost:9999")) { s: Socket =>
      val request = MethodCall("Ping", "")
      s.send(ScalaMessagePack.pack(request), 0)
      println("Getting response "+request)
      val response = ScalaMessagePack.unpack[Reply](s.recv(0))
      println("Got response "+response)
    }
  }
}