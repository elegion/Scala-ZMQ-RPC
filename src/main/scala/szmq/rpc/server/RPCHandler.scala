package szmq.rpc.server

import szmq.rpc._
import org.zeromq.ZMQ.{Context, Poller, Socket}

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 10:00 PM
 */

object ErrorResponse {
  def unknownMethod(call: MethodCall) = Reply("", Some("Unknown method: "+call.name+" for this args"))
  def malformedCall = Reply("", Some("Malformed method call"))
}

abstract class RPCHandler { self: Serializer =>
  var running = true
  type DispatchPF = PartialFunction[MethodCall, Reply]
  @volatile private var _dispatch: List[DispatchPF] = dispatchUnhandled::Nil
  def dispatchUnhandled: DispatchPF = {
    case call: MethodCall => { ErrorResponse.unknownMethod(call) }
  }
  def serve(handler: DispatchPF) {
    _dispatch ::= handler
  }

  def stop() {
    running = false
  }

  def handleSocket(ctx: Context, socket: Socket) {
    val poller = ctx.poller()
    poller.register(socket)
    while(running) {
      val polled = poller.poll(0)
      if (polled > 0) {
      val data = socket.recv(0)
        try {
          val call = deserialize[MethodCall](data)
          val response = _dispatch.find(_.isDefinedAt(call)).get.apply(call)
          val responseData = serialize(response)
          socket.send(responseData, 0)
        } catch {
          case e: Exception => ErrorResponse.malformedCall
        }
      }
    }
    poller.unregister(socket)
    socket.close()
  }

}