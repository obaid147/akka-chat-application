import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Terminated}

abstract class Msg

case class Info(msg: String) extends Msg

case class NewMessage(from: String, msg: String) extends Msg

case class Send(msg: String) extends Msg

case class Connect(username: String) extends Msg

case class BroadCast(msg: String) extends Msg

case object Disconnect

class Server extends Actor {
  override def receive: Receive = handler(List[(String, ActorRef)]())

  def handler(clients: List[(String, ActorRef)]): Receive = {
    case Connect(username) =>
      /*if (!clients.contains(username)) {*/
        broadcast(Info(s"$username has joined the chat"), /*(username, sender) ::*/ clients)
        context.watch(sender)
        context.become(handler((username, sender) :: clients))
      /*}*/
    case BroadCast(message) =>
      val clientOption = clients.find(_._2 == sender)
      if (clientOption.isDefined) {
        val username = clientOption.get._1
        broadcast(NewMessage(username, message), clients)
      }
    case Terminated(client) =>
      val clientOption = clients.find(_._2 == sender)
      val newClients = clients.filterNot(_._2 == client)
      if (clientOption.isDefined) {
        val username = clientOption.get._1
        broadcast(Info(s"$username has left chat"), newClients)
        context.become(handler(newClients))
      }
  }

  def broadcast(info: Msg, clients: List[(String, ActorRef)]): Unit = {
    clients.foreach(_._2 ! info)
  }

}

class Client(username: String, server: ActorRef) extends Actor {
  server ! Connect(username)

  override def receive: Receive = {
    case info: Info =>
      println(s"[$username's client]- ${info.msg}")
    case Send(msg) =>
      server ! BroadCast(msg)
    case newMsg: NewMessage =>
      if (username != newMsg.from)
        println(s"[$username's client]- from = ${newMsg.from},message = ${newMsg.msg}")
    case Disconnect =>
      println(s"KILL .............${self.path.name}")
      self ! PoisonPill
  }
}

object Client {
  def props(username: String, server: ActorRef): Props =
    Props(
      new Client(username, server)
    )
}


object BroadcastChat extends App {

  val system = ActorSystem("Broadcaster")

  val server: ActorRef = system.actorOf(Props[Server], "Server")

  val client1 = system.actorOf(Client.props("amr", server), "Client1")
  Thread.sleep(500)

  val client2: ActorRef = system.actorOf(Client.props("oby", server), "Client2")

  Thread.sleep(300)

  client2 ! Send("Hi all")
  Thread.sleep(300)
  val client3 = system.actorOf(Client.props("faw", server), "Client3")
  val client4 = system.actorOf(Client.props("raf", server), "Client4")
  Thread.sleep(1000)

  client1 ! Send("lechmav sarneee")
  Thread.sleep(1000)

  client4 ! Disconnect

  Thread.sleep(2000)
  val client5 = system.actorOf(Client.props("tene", server), "Client5")
  Thread.sleep(1000)
  client5 ! Send("TIN SOOB")

  Thread.sleep(300)
  client3 ! Disconnect
}