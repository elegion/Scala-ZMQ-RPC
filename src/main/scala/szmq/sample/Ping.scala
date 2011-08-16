package szmq.sample

import org.zeromq.ZMQ._
import szmq._
import   rpc._
import   Util._
import  szmq.rpc.client.Client

import java.lang.Thread

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 10:14 PM
 */

object Ping {
  def main(args: Array[String]) {
    inContext() { context: Context =>
      val clientsNum = 10
      val count = args.headOption map (_.toInt) getOrElse (10)

      1 to clientsNum map { clientNum =>
        Thread sleep 100
        thread {
          val start = System currentTimeMillis()
          req(context, ConnectTo("tcp://localhost:9999")) { socket =>
            println("Creating client")
            val client = new Client(socket) with BSONSerializer
            1 to count foreach { n =>
              println("Calling args "+n)
              val response = client.callMethod("args", "client #"+clientNum, n)
              println("Got response "+response)
              Thread sleep 1000
            }
          }
          println("done in "+(System.currentTimeMillis() - start) + "ms")
        }
      } foreach (_.join())
    }
  }
}