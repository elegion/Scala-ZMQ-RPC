Scala ZMQ (szmq) is a simple wrapper around jzmq and a basic implementation of ØMQ RPC server/client.

This is not a production-ready code (yet?), just a little ØMQ + scala example.

Server example:

    package szmq.sample

    import szmq._
    import   rpc._
    import   rpc.server._

    object Pong extends Application {
      val workerNum = 50

      class PongHandler(val n: Int) extends RPCHandler
      with BSONSerializer //Use BSON serialization to encode MethodCalls and replies,
                          //You can use another serialization mechanizm, just implement a Serializer trait
                          //(designed to work only with case classes for now to keep things simple
                          //and be compatible with salat)
      {
        //serve takes PartialFunction[MethodCall, Reply]
        serve {
          case MethodCall("ping", _) => Reply("Pong")
          case MethodCall("args", List(a, b)) => {
            println("# %s Got args: %s, %s" format (n, a, b))
            Thread.sleep(1000)
            Reply("Args: %s, %s" format (a, b))
          }
        }
      }

      //start frontend dispatcher
      val queue = new RPCQueue().start(BindTo("tcp://*:9999"))

      //add rpc workers
      1 to workerNum foreach {n =>
        queue.addWorker(new PongHandler(n))
      }
    }

Client example:

    package szmq.sample

    import org.zeromq.ZMQ._
    import szmq._
    import   rpc._
    import   Util._
    import  szmq.rpc.client.Client

    import java.lang.Thread

    object Ping {
      def main(args: Array[String]) {
        inContext() { context: Context =>
          val clientsNum = 200
          val count = args.headOption map (_.toInt) getOrElse (10)

          //create a bunch of client threads
          1 to clientsNum foreach { clientNum =>

            //shorthand for new Thread(){ override def run() { ... } }.start()
            thread {
              val start = System currentTimeMillis()

              //create req socket, execute block with it and close
              req(context, ConnectTo("tcp://localhost:9999")) { socket =>
                println("Creating client")
                val client = new Client(socket) with BSONSerializer
                1 to count foreach { n =>
                  println("Calling args "+n)
                  val response2 = client.callMethod("args", List("client #"+clientNum, n))
                  println("Got response "+response2)
                  Thread sleep 1000
                }
              }
              println("done in "+(System.currentTimeMillis() - start) + "ms")
            }
            Thread sleep 100
          }
        }
      }
    }

Current implementation of rpc uses the following message flow:

    [ Client | REQ ] <--> [ XREP | queue thread | XREQ ] <--> [ REP | worker threads ]

