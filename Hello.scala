import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor._
import akka.actor.Actor._
import akka.transactor._
import scala.concurrent.stm._

class HelloActor(myName: String) extends Actor {
  def receive = {
    case "hello" => println("hello from %s".format(myName))
    case _ => println("'huh?', said %s".format(myName))
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val helloActor = system.actorOf(Props(new HelloActor("Fred")), name = "helloactor")
  helloActor ! "hello"
  helloActor ! "buenos dias"
}

case object Ping
case object Pong

class PongActor extends Actor {
  def receive = {
    case Ping ⇒ {
      Thread.sleep(2000)
      println(self.path + ": Received Ping!")
      sender ! Pong
    }
    case _ ⇒ ()
  }
}

class PingActor extends Actor {
  context.actorSelection("../Pong*") ! Ping // starts things off
  def receive = {
    case Pong ⇒ {
      Thread.sleep(2000)
      println(self.path + ": Received Pong!")
      sender ! Ping
    }
    case _ ⇒ ()
  }
}

object PingPong extends App {
  val system = ActorSystem()
  system.actorOf(Props[PongActor], name = "Pong")
  system.actorOf(Props[PingActor], name = "Ping")
}

object PingPongPong extends App {
  val system = ActorSystem()
  system.actorOf(Props[PongActor], name = "Pong1")
  system.actorOf(Props[PongActor], name = "Pong2")
  system.actorOf(Props[PingActor], name = "Ping")
}

import akka.actor._
import akka.transactor._
import scala.concurrent.stm._

case object Increment

class FriendlyCounter(friend: ActorRef) extends Transactor {
  val count = Ref(0)

  override def coordinate = {
    case Increment ⇒ include(friend)
  }

  def atomically = implicit txn ⇒ {
    case Increment ⇒ count transform (_ + 1)
  }
}

