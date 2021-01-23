import Functions.FilmCommand
import ackcord._
import ackcord.data.{GuildChannel, TextChannelId}
import ackcord.requests.{CreateMessage, CreateMessageData}
import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  final val clientName = "bb3tbbbb"

  implicit val as: ActorSystem = ActorSystem("tests")

  def textChannelToGuildChannel(textChannelId: TextChannelId)(implicit c: CacheSnapshot): Option[GuildChannel] = {
    textChannelId.asChannelId[GuildChannel].resolve
  }

  val config = Config.config match {
    case Right(value) => value
    case Left(value) => sys.error(s"Failed to parse config - $value")
  }

  val clientSettings = ClientSettings(config.discordToken)

  import client.executionContext
  val client = Await.result(clientSettings.createClient(), Duration.Inf)

  client.login()

  val fetchFilmInfo = new FetchFilmInfo(config.omdbapiToken)

  val cmd = new FilmCommand(client.requests)
  client.commands.runNewNamedCommand(cmd.hello)

  client.onEventAsync { implicit cacheSnapshot => {
    case APIMessage.Ready(_) =>
      println("Connected")
      OptFuture.fromOption(None)

    case APIMessage.MessageCreate(guild, message, _) =>
      println(s" channel.to_str = '${message.channelId}''")

      if (message.content.toLowerCase.startsWith("fuck you") && message.authorUsername != clientName) {
        val msg = CreateMessage(message.channelId, CreateMessageData(content = s"Fuck you ${message.authorUsername}"))
        client.requestsHelper.run(msg).map(_ => ())
      }

      else if (guild.isDefined) {
        textChannelToGuildChannel(message.channelId) match {
          case Some(c: GuildChannel) if c.name == "saturday-film-votes" && (message.authorUsername != clientName) =>
            OptFuture.fromFuture(
              fetchFilmInfo.fetchByTitle(message.content).map { f =>
                client.requestsHelper.run(CreateMessage(message.channelId, CreateMessageData(content = f.asMessage))).map(
                  println
                )
              }
            )
          case _ => OptFuture.fromOption(None)
        }
      }
      else {
        OptFuture.fromOption(None)
      }
    }
  }
}