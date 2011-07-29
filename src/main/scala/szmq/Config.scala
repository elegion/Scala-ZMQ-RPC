package szmq

import org.zeromq.ZMQ.Socket

/**
 * Author: Yuri Buyanov
 * Date: 7/29/11 3:51 PM
 */

abstract class Config {
  def ioThreads: Int
}

object DefaultConfig extends Config {
  def ioThreads = 1
}
