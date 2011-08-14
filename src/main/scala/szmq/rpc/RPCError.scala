package szmq.rpc

/**
 * Author: Yuri Buyanov
 * Date: 8/1/11 11:55 AM
 */

class RPCError(errors: Error*) extends Exception(errors.map(_.code).mkString(", "))