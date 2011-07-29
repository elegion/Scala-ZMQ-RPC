package szmq.sample

import szmq.Util._
import szmq.BindTo
import org.zeromq.ZMQ._

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 8:06 PM
 */

object Pong extends Application {
  inContext() { context: Context =>
    repLoop(context, BindTo("tcp://*:9999")) { s: Socket =>
      println("Waiting 4 req")
      val requestString = new String(s.recv(0))
      println("Recieved "+requestString)
      s.send(("Pong "+requestString).getBytes, 0)
    }
  }
}