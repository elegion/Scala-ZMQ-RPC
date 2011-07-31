package szmq.rpc

import com.mongodb.util.JSONCallback

/**
 * Author: Yuri Buyanov
 * Date: 7/31/11 5:49 AM
 */

trait Serializer {
  type CaseClass = AnyRef with Product

  def serialize[T <: CaseClass](obj: T)(implicit m: Manifest[T]): Array[Byte]
  def deserialize[T <: CaseClass](data: Array[Byte])(implicit m: Manifest[T]): T
}

trait BSONSerializer extends Serializer {
  import com.novus.salat._
  import com.novus.salat.global._
  import com.mongodb.casbah.Imports._
  import org.bson.{BSONDecoder, BSONEncoder}

  val encoder = new BSONEncoder()
  val decoder = new BSONDecoder()

  def serialize[T <: CaseClass](obj: T)(implicit m: Manifest[T]) = encoder.encode(grater[T].asDBObject(obj))
  def deserialize[T <: CaseClass](data: Array[Byte])(implicit m: Manifest[T]) = {
    //http://groups.google.com/group/mongodb-user/browse_thread/thread/9e17b8765018309
    //WTF?
    val callback = new JSONCallback();
    val n = new BSONDecoder().decode(data, callback);
    val obj = callback.get.asInstanceOf[DBObject];
    grater[T].asObject(obj)
  }
}

