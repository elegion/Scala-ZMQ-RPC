package szmq.rpc

import client.Client
import org.scalatest.{BeforeAndAfterEach, Suite}
import szmq.Util._
import org.zeromq.ZMQ.Context
import szmq.ConnectTo._
import szmq.{BindTo, ConnectTo}

/**
 * Author: Yuri Buyanov
 * Date: 8/16/11 11:37 PM
 */

class PingPongSuite extends Suite with BeforeAndAfterEach {

  var pong = new PongServer(BindTo("tcp://*:9999"))

  override protected def beforeEach() {
    //to prevent parallel test execution
    //(i know, i can configure that in buildfile :)
    pong.start()
  }

  override protected def afterEach() {
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

        //method invocation with incorrect arg
        //list should return METHOD_UNKNOWN
        val err1 = intercept[RPCError] {
          client.callMethod("ping", "foo")
        }

        assert(err1.errors.length === 1)
        assert(err1.errors.head.code === "METHOD_UNKNOWN")

        //method invocation with incorrect method name
        //list should return METHOD_UNKNOWN
        val err2 = intercept[RPCError] {
          client.callMethod("ping", "foo")
        }
        assert(err2.errors.length === 1)
        assert(err2.errors.head.code === "METHOD_UNKNOWN")
      }
    }
  }

  //todo: test with multiple parallel clients (like szmq.sample.Ping
}