package szmq.rpc

import org.msgpack.annotation.MessagePackMessage

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 9:32 PM
 */

object Messages

/**
 * rpc method call
 */
@MessagePackMessage
case class MethodCall(
  name: String,
  args: List[Any]
)

@MessagePackMessage
case class Reply(
  value: Any,
  error: String
)