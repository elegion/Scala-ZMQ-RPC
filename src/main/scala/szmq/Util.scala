package szmq

import org.zeromq._
import org.zeromq.ZMQ._
import szmq.DefaultConfig

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

  private def plugSocket(sockType: Int)(context: Context, endpoint: Endpoint)(handler: Socket => Any) = {
    val socket = context.socket(sockType)
    endpoint plug socket
    val handlerResult = handler(socket)
    socket.close()
    result
  }

  def rep(context: Context, endpoint: Endpoint)(handler: Socket => Any) = plugSocket(REP)(context, endpoint)(handler)
  def req(context: Context, endpoint: Endpoint)(handler: Socket => Any) = plugSocket(REQ)(context, endpoint)(handler)


  def repLoop(context: Context, endpoint: Endpoint)(handler: Socket => Any) {
    val loopHandler = { s: Socket => while (true) { handler(s) }}
    rep(context,endpoint)(loopHandler)
  }


}