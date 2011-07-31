package szmq.rpc

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 9:32 PM
 */

object Messages

/**
 * rpc method call
 */
case class MethodCall(
  name: String,
  args: List[Any]
)

case class Reply(
  value: Any,
  error: String
)