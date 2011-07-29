package szmq.sample

import szmq.Util._
import szmq.BindTo._
import org.zeromq.ZMQ._
import szmq.ConnectTo

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 10:14 PM
 */

object Ping extends Application {
  inContext() { context: Context =>
    req(context, ConnectTo("tcp://localhost:9999")) { s: Socket =>
      val requestString = "Ping"
      println("Sending "+requestString)
      s.send(requestString.getBytes, 0)
      println("Getting response "+requestString)
      val response = new String(s.recv(0))
      println("Got response "+response)
    }
  }
}