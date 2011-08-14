package szmq.rpc

/**
 * Author: Yuri Buyanov
 * Date: 7/30/11 9:32 PM
 */

object Messages
abstract sealed class Message extends Product //children classes should be case classes

/**
 * rpc method call
 */
case class MethodCall(
  name: String,
  args: List[Any] = Nil
) extends Message

abstract sealed class Reply extends Message
object Reply {
  def apply(value: Any) = ValueReply(value)
}

case class ValueReply(value: Any) extends Reply
case class ErrorReply(errors: Error*) extends Reply

object ErrorReply {
  def apply(
    code: String,
    description: String,
    argname: Option[String] = None
  ): ErrorReply =
    ErrorReply(Error(code, description, argname))
}


case class Error(
  code: String,
  description: String,
  argname: Option[String] = None
)