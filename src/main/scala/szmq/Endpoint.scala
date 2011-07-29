package szmq

import org.zeromq.ZMQ.Socket

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 7:02 PM
 */

abstract sealed class Endpoint {
  def plug(s: Socket)
}

object BindTo {
  def apply(address: String): BindTo = BindTo(Seq(address))
}

case class BindTo(addresses: Traversable[String]) extends Endpoint {
  def plug(s: Socket) {
    addresses foreach (s bind _)
  }
}

object ConnectTo {
  def apply(address: String): ConnectTo = ConnectTo(Seq(address))
}

case class ConnectTo(addresses: Traversable[String]) extends Endpoint {
  def plug(s: Socket) {
    addresses foreach (s connect _)
  }
}