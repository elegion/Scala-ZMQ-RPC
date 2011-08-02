package szmq.sample

import szmq.Util._
import szmq.BindTo._
import org.zeromq.ZMQ._
import szmq.ConnectTo
import szmq.rpc.{BSONSerializer, Serializer, Reply, MethodCall}
import szmq.client.Client
import java.lang.Thread



//import org.msgpack.ScalaMessagePack
/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 10:14 PM
 */

object Ping {
  def main(args: Array[String]) {
    inContext() { context: Context =>
      val clientsNum = 200
      val count = args.headOption map (_.toInt) getOrElse (10)

      1 to clientsNum foreach { clientNum =>
        new Thread() {
          override def run() {
            val start = System currentTimeMillis()
            req(context, ConnectTo("tcp://localhost:9999")) { socket =>
              println("Creating client")
              val client = new Client(socket) with BSONSerializer
              1 to count foreach { n =>
                println("Calling args "+n)
                val response2 = client.callMethod("Args", List("client #"+clientNum, n))
                println("Got response "+response2)
                Thread sleep 1000
              }
            }
            println("done in "+(System.currentTimeMillis() - start) + "ms")

          }
        }.start()
        Thread sleep 1000
      }
    }
  }
}