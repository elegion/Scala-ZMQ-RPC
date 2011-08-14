package szmq.rpc.client

import szmq._
import   rpc._
import org.zeromq.ZMQ.Socket

/**
 * Author: Yuri Buyanov
 * Date: 8/1/11 11:43 AM
 */

abstract class Client(socket: Socket) extends Serializer {
  def callMethod(name: String, args: Any*): Any = {
    val mc = MethodCall(name, args.toList)
    socket.send(serialize(mc), 0)
    val reply = deserialize[Reply](socket.recv(0))
    reply match {
      case ValueReply(value) => value
      case ErrorReply(errs) => throw new RPCError(errs: _*)
    }
  }

}