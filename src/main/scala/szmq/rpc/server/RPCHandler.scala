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
  def unknownMethod(call: MethodCall) = Reply("", Some("Unknown method: "+call.name+" for this args"))
  def malformedCall = Reply("", Some("Malformed method call"))
}

abstract class RPCHandler { self: Serializer =>
  type DispatchPF = PartialFunction[MethodCall, Reply]
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

  def handleSocket(ctx: Context, socket: Socket) {
    val poller = ctx.poller()
    poller.register(socket)
    while(running) {
      val polled = poller.poll(0)
      if (polled > 0) {
        val data = socket.recv(0)
        stats foreach (_.incr("Messages recieved"))
        try {
          val call = deserialize[MethodCall](data)
          stats foreach (_.setLabel("#-"+id+" current method", call.toString))
          val response = _dispatch.find(_.isDefinedAt(call)).get.apply(call)
          val responseData = serialize(response)
          socket.send(responseData, 0)
        } catch {
          case e: Exception => {
            stats foreach (_.incr("Malformed calls"))
            log.error(e, "Malformed call")
            ErrorResponse.malformedCall
          }
        }
        stats foreach (_.clearLabel("Current method"))
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