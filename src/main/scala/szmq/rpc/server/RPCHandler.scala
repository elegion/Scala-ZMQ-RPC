package szmq.rpc.server

import szmq.rpc._
import org.zeromq.ZMQ.{Context, Poller, Socket}
import com.twitter.logging.Logger
import szmq.Loggable
import com.twitter.ostrich.stats.{Stats, StatsCollection}

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 10:00 PM
 */

object ErrorResponse {
  def unknownMethod(call: MethodCall) = ErrorReply(
    code = "METHOD_UNKNOWN",
    description = "Unknown method: "+call.name+" for args: "+call.args.mkString(", "),
    emotion = """¯\(°_o)/¯""" //I DUNNO
  )

  def malformedCall = ErrorReply(
    code = "MALFORMED_CALL",
    description = "Malformed method call",
    emotion = ":S"
  )

  def exception(e: Throwable) = ErrorReply(
    code = "EXCEPTION",
    description = e.getMessage,
    emotion = "=:-E"
  )
}

abstract class RPCHandler extends Loggable { self: Serializer =>
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

  def safeCall(block: => Reply) = {
    try
      block()
    catch {
      case e: Throwable => {
        log.error(e, "Exception has occurred during request processing.")
        stats.foreach(_.incr("EXCEPTION responses by %s" format e.getClass.getSimpleName))
        stats.foreach(_.incr("EXCEPTION responses total"))
        ErrorResponse.exception(e)
      }
    }
  }


  def handleSocket(ctx: Context, socket: Socket, pollTimeout: Int = 0) {
    val poller = ctx.poller()
    poller.register(socket)
    while(running) {
      val polled = poller.poll(pollTimeout)
      if (polled > 0) {
        val data = socket.recv(0)
        stats foreach (_.incr("Messages recieved"))
        val response = try {
          val call = deserialize[MethodCall](data)
          safeCall {
            _dispatch.find(_.isDefinedAt(call)).get.apply(call)()
          }
          //cannot serialize child class as Reply :(
        } catch {
          case e: Exception => {
            stats foreach (_.incr("Malformed calls"))
            log.error(e, "Malformed call")
            ErrorResponse.malformedCall
          }
        }

        val responseData = response match {
          case v: ValueReply => serialize(v)
          case e: ErrorReply => serialize(e)
        }

        socket.send(responseData, 0)

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
}
