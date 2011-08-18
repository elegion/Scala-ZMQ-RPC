package szmq.rpc

import client.Client
import server.{RPCQueue, RPCHandler}
import szmq.Util._
import org.zeromq.ZMQ.Context
import szmq.ConnectTo._
import szmq.{BindTo, ConnectTo}
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.scalatest.Assertions._

/**
 * Author: Yuri Buyanov
 * Date: 8/18/11 10:58 PM
 */

class ErrorsSuite extends Suite with BeforeAndAfterAll {
  val queue = new RPCQueue(BindTo("tcp://*:9998"))

  override protected def beforeAll() {
    //todo: deal with parallel test execution in scalatest
    queue.start()
    queue.addWorker(new RPCHandler with BSONSerializer {
      serve {
        case MethodCall("fail", Nil) => throw new Exception("I've failed")
      }
    })
  }

  override protected def afterAll() {
    queue.shutdown()
  }


  def testFails() {
    inContext() { context: Context =>
      req(context, ConnectTo("tcp://localhost:9998")) { socket =>
        val client = new Client(socket) with BSONSerializer

        //sending anything except MethodCall to socket
        //list should return MALFORMED_CALL
        val errException = intercept[RPCError] {
          client.callMethod("fail")
        }

        assert(errException.errors.length === 1)
        assert(errException.errors.head.code === "EXCEPTION")
        assert(errException.errors.head.description === "I've failed")

        //method invocation with incorrect arg
        //list should return METHOD_UNKNOWN
        val err1 = intercept[RPCError] {
          client.callMethod("fail", "arg")
        }

        assert(err1.errors.length === 1)
        assert(err1.errors.head.code === "METHOD_UNKNOWN")

        //method invocation with incorrect method name
        //list should return METHOD_UNKNOWN
        val err2 = intercept[RPCError] {
          client.callMethod("foo", "bar")
        }
        assert(err2.errors.length === 1)
        assert(err2.errors.head.code === "METHOD_UNKNOWN")

        //sending anything except MethodCall to socket
        //list should return MALFORMED_CALL
        socket.send("LOL".getBytes, 0)
        val errRepl = client.deserialize[Reply](socket.recv(0))
        assert(errRepl.isInstanceOf[ErrorReply])
        val err3 = errRepl.asInstanceOf[ErrorReply]

        assert(err3.errors.length === 1)
        assert(err3.errors.head.code === "MALFORMED_CALL")
      }
    }
  }




}