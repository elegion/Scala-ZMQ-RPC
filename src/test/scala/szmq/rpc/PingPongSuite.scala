package szmq.rpc

import client.Client
import server.RPCQueue
import szmq.Util._
import org.zeromq.ZMQ.Context
import szmq.ConnectTo._
import szmq.{BindTo, ConnectTo}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

/**
 * Author: Yuri Buyanov
 * Date: 8/16/11 11:37 PM
 */

class PingPongSuite extends Suite with BeforeAndAfterAll {

  var pong = new PongServer(BindTo("tcp://*:9999"))

  override protected def beforeAll() {
    //todo: deal with parallel test execution in scalatest
    pong.start()
  }

  override protected def afterAll() {
    pong.stop()
  }

  def testPingPong() {
      inContext() { context: Context =>
      req(context, ConnectTo("tcp://localhost:9999")) { socket =>
        val client = new Client(socket) with BSONSerializer
        1 to 10 foreach { n =>
          val response = client.callMethod("ping", "foo", n)
          assert(response === "Pong (foo, %s)".format(n))
        }
      }
    }
  }

  //todo: test with multiple parallel clients (like szmq.sample.Ping
}