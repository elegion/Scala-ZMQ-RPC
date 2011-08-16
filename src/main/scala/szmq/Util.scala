package szmq

import org.zeromq._
import org.zeromq.ZMQ._
import java.lang.Thread

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 3:48 PM
 */

object Util {
  def withContext(c: Context)(block: Context => Any) = {
    block(c)
  }

  def inContext(config: Config = DefaultConfig)(block: Context => Any) = {
    val zmqcontext = context(config.ioThreads)
    withContext(zmqcontext)(block)
  }

  private def inSocket(sockType: Int)(context: Context, endpoint: Endpoint)(handler: Socket => Any) = {
    val socket = context.socket(sockType)
    try {
      endpoint plug socket
      val handlerResult = handler(socket)
      handlerResult
    } finally {
      try {
        socket.close()
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  def rep = inSocket(REP) _
  def req = inSocket(REQ) _
  def router = inSocket(XREP) _
  def dealer = inSocket(XREQ) _

  def repLoop(context: Context, endpoint: Endpoint)(handler: Socket => Any) {
    val loopHandler = { s: Socket => while (true) { handler(s) }}
    rep(context,endpoint)(loopHandler)
  }

  def thread(block: => Any) = {
    val thread = new Thread() {
      override def run() { block }
    }
    thread.start()
    thread
  }
}