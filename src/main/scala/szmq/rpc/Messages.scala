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
case class ErrorReply(errors: List[Error]) extends Reply

object ErrorReply {
  //I know about varargs, but salat deserialization doesn't seem to support them
  def apply(error: Error): ErrorReply = ErrorReply(List(error))

  def apply(
    code: String,
    description: String,
    argname: Option[String] = None,
    emotion: String = ":("
  ): ErrorReply =
    ErrorReply(Error(code, description, argname, emotion))
}


case class Error(
  code: String,
  description: String,
  argname: Option[String] = None,
  emotion: String = ":("
)