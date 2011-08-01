package szmq.sample

import szmq.Util._
import szmq.BindTo._
import org.zeromq.ZMQ._
import szmq.ConnectTo
import szmq.rpc.{BSONSerializer, Serializer, Reply, MethodCall}
import szmq.client.Client

//import org.msgpack.ScalaMessagePack
/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 10:14 PM
 */

object Ping extends Application {
  inContext() { context: Context =>
    req(context, ConnectTo("tcp://localhost:9999")) { socket =>
      println("Creating client")
      val client = new Client(socket) with BSONSerializer

      println("Calling ping")
      val response = client.callMethod("Ping", Nil)
      println("Got response "+response)

      println("Calling args")
      val response2 = client.callMethod("Args", List(3, 2.8))
      println("Got response "+response2)

    }

  }
}