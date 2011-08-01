package szmq.client

import org.zeromq.ZMQ
import szmq.{RPCError, Endpoint}
import szmq.rpc.{Serializer, Reply, BSONSerializer, MethodCall}
import org.zeromq.ZMQ.{Socket, Context}

/**
 * Author: Yuri Buyanov
 * Date: 8/1/11 11:43 AM
 */

abstract class Client(socket: Socket) extends Serializer {
  def callMethod(name: String, args: List[Any]): Any = {
    val mc = MethodCall(name, args)
    socket.send(serialize(mc), 0)
    val reply = deserialize[Reply](socket.recv(0))
    println("reply: "+reply)
    reply.error map {err: String => throw new RPCError(err)} getOrElse(reply.value)
  }

}