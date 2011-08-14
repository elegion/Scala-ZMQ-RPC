package szmq.rpc.server

import szmq.rpc._
import org.zeromq.ZMQ.{Context, Poller, Socket}
import com.twitter.ostrich.stats.StatsCollection
import com.twitter.logging.Logger

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 10:00 PM
 */

object ErrorResponse {
  def unknownMethod(call: MethodCall) = ErrorReply(
    code = "METHOD_UNKNOWN",
    description = "Unknown method: "+call.name+" for args: "+call.args.mkString(", ")
  )

  def malformedCall = ErrorReply("MALFORMED_CALL", "Malformed method call")
}

abstract class RPCHandler { self: Serializer =>
  type DispatchPF = PartialFunction[MethodCall, () => Reply]
  implicit def reply2Callback(r: Reply): () => Reply = { () => r }

  @volatile private var _dispatch: List[DispatchPF] = dispatchUnhandled::Nil
  def dispatchUnhandled: DispatchPF = {
    case call: MethodCall => {
      stats foreach (_.incr("Unhandled call (total)"))
      stats foreach (_.incr("Unhandled calls ("+call.name)+")")
      log.warning("Unhandled method call: %s", call.toString)
      ErrorResponse.unknownMethod(call)
    }
  }
  var running = true

  def serve(handler: DispatchPF) {
    _dispatch ::= handler
  }

  def stop() {
    running = false
  }

  def handleSocket(ctx: Context, socket: Socket, pollTimeout = 0) {
    val poller = ctx.poller()
    poller.register(socket)
    while(running) {
      val polled = poller.poll(pollTimeout)
      if (polled > 0) {
        val data = socket.recv(0)
        stats foreach (_.incr("Messages recieved"))
        try {
          val call = deserialize[MethodCall](data)
          val response = _dispatch.find(_.isDefinedAt(call)).get.apply(call)()

          //cannot serialize child class as Reply
          val responseData = response match {
            case v: ValueReply => serialize(v)
            case e: ErrorReply => serialize(e)
          }
          socket.send(responseData, 0)
        } catch {
          case e: Exception => {
            stats foreach (_.incr("Malformed calls"))
            log.error(e, "Malformed call")
            ErrorResponse.malformedCall
          }
        }
      }
    }
    poller.unregister(socket)
    socket.close()
  }

  /**
   * Override to get stats
   */
  def id = hashCode().toHexString
  def stats: Option[StatsCollection] = None
  def log = Logger.get(getClass.getName)
}