package szmq.sample

import szmq.Util._
import szmq.BindTo._
import org.zeromq.ZMQ._
import szmq.ConnectTo
import szmq.rpc.{Serialization, Reply, MethodCall}

//import org.msgpack.ScalaMessagePack
/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 10:14 PM
 */

object Ping extends Application with Serialization {
  inContext() { context: Context =>
    req(context, ConnectTo("tcp://localhost:9999")) { s: Socket =>
      val request = MethodCall("Ping", "")
      s.send(serialize(request), 0)
      println("Getting response "+request)
      val response = deserialize[Reply](s.recv(0))
      println("Got response "+response)
    }
  }
}