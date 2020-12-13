package Functions

import ackcord.Requests
import ackcord.commands._
import ackcord.syntax._
import akka.NotUsed


class FilmCommand(requests: Requests) extends CommandController(requests) {
  val hello: NamedCommand[NotUsed] = Command
    .named(Seq("suggest!"), Seq("!suggest"))
    .withRequest(m => m.textChannel.sendMessage(s"I got ur suggestion ${m.user.username}"))
}