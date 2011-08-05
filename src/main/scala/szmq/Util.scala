package szmq

import org.zeromq._
import org.zeromq.ZMQ._
import szmq.DefaultConfig
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
    endpoint plug socket
    val handlerResult = handler(socket)
    socket.close()
    handlerResult
  }

  def rep(context: Context, endpoint: Endpoint)(handler: Socket => Any) = inSocket(REP)(context, endpoint)(handler)
  def req(context: Context, endpoint: Endpoint)(handler: Socket => Any) = inSocket(REQ)(context, endpoint)(handler)
  def router(context: Context, endpoint: Endpoint)(handler: Socket => Any) = inSocket(XREP)(context, endpoint)(handler)
  def dealer(context: Context, endpoint: Endpoint)(handler: Socket => Any) = inSocket(XREQ)(context, endpoint)(handler)

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