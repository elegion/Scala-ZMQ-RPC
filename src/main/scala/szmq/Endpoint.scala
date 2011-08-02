package szmq

import org.zeromq.ZMQ.Socket

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 7:02 PM
 */

abstract sealed class Endpoint(addresses: String*) {
  def plug(s: Socket) {
    addresses foreach (_plug(s, _))
  }

  def _plug(s: Socket, address: String)
}

case class BindTo(addresses: String*) extends Endpoint(addresses: _*) {
  def _plug(s: Socket, address: String) { s bind address }
}

case class ConnectTo(addresses: String*) extends Endpoint(addresses: _*) {
  def _plug(s: Socket, address: String) { s connect address  }
}