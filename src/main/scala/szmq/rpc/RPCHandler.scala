package szmq.rpc

import org.zeromq.ZMQ.Socket

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 10:00 PM
 */

object ErrorResponse {
  def unknownMethod(call: MethodCall) = Reply("", Some("Unknown method: "+call.name+" for this args"))
  def malformedCall = Reply("", Some("Malformed method call"))
}

abstract class RPCHandler { self: Serializer =>
  type DispatchPF = PartialFunction[MethodCall, Reply]
  @volatile private var _dispatch: List[DispatchPF] = dispatchUnhandled::Nil
  def dispatchUnhandled: DispatchPF = {
    case call: MethodCall => { ErrorResponse.unknownMethod(call) }
  }
  def serve(handler: DispatchPF) {
    _dispatch ::= handler
  }


  def handleSocket(socket: Socket) {
    while(true) {
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

}