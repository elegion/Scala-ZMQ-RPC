package szmq.rpc

/**
 * Author: Yuri Buyanov
 * Date: 7/31/11 5:49 AM
 */

trait Serialization {
  def serialize(obj: Any): Array[Byte]
  def deserialize[T](data: Array[Byte]): T
}

