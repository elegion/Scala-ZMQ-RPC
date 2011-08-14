package szmq.rpc

import com.mongodb.util.JSONCallback

/**
 * Author: Yuri Buyanov
 * Date: 7/31/11 5:49 AM
 */

trait Serializer {
  type CaseClass = AnyRef with Product

  def serialize[T <: Message](obj: T)(implicit m: Manifest[T]): Array[Byte]
  def deserialize[T <: Message](data: Array[Byte])(implicit m: Manifest[T]): T
}

trait BSONSerializer extends Serializer {
  import com.novus.salat._
  import com.novus.salat.global._
  import com.mongodb.casbah.Imports._
  import org.bson.{BSONDecoder, BSONEncoder}

  val encoder = new BSONEncoder()
  val decoder = new BSONDecoder()

  def serialize[T <: Message](obj: T)(implicit m: Manifest[T]) = encoder.encode(grater[T].asDBObject(obj))
  def deserialize[T <: Message](data: Array[Byte])(implicit m: Manifest[T]) = {
    //http://groups.google.com/group/mongodb-user/browse_thread/thread/9e17b8765018309
    //WTF?
    val callback = new JSONCallback()
    val n = new BSONDecoder().decode(data, callback)
    val obj = callback.get.asInstanceOf[DBObject]
    //Ugly, but looks like salat isn't smart enough to detect proper subclass of T
    val gr =
      if (m.erasure == classOf[Reply])           //Reply
        if (obj.getAs[String]("errors").isDefined)  //ErrorReply
          grater[ErrorReply]
        else                                      //ValueReply
          grater[ValueReply]
      else                                        //MethodCall
        grater[MethodCall]

      gr.asObject(obj).asInstanceOf[T]            //boooooo! :(
  }
}

