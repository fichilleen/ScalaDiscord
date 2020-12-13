import Functions.FilmCommand
import ackcord._
import ackcord.data.{GuildChannel, TextChannelId}
import ackcord.requests.{CreateMessage, CreateMessageData}
import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  //final val token = "Nzg3Mjk5NTUyNTA2OTM3MzQ0.X9S72A.fep-oj_lT4meB-j_F6SQRymfTOE"
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
  //The client settings contains an excecution context that you can use before you have access to the client
  //import clientSettings.executionContext

  val client = Await.result(clientSettings.createClient(), Duration.Inf)
  import client.executionContext
  client.login()

  val fetchFilmInfo = new FetchFilmInfo(config.omdbapiToken)

  // This doesn't work, not sure why
  val cmd = new FilmCommand(client.requests)
  client.commands.runNewNamedCommand(cmd.hello)

  client.onEventAsync { implicit cacheSnapshot => {
    case APIMessage.MessageCreate(guild, message, _) =>

      println(s"On guild $guild")
      println(s" channel.to_str = '${message.channelId}''")


      if (message.content.toLowerCase.startsWith("fuck you") && message.authorUsername != clientName) {
        val msg = CreateMessage(message.channelId, CreateMessageData(content = s"Fuck you ${message.authorUsername}"))
        client.requestsHelper.run(msg).map(_ => ())
      }

      else if (guild.isDefined) {
          val chan = textChannelToGuildChannel(message.channelId)
          chan.foreach{ c =>
            println("got a defined channel")
            println(s"called ${c.name}")
          }
          if ((chan.isDefined) && (chan.get.name == "saturday-film-votes") && (message.authorUsername != clientName)) {
            println("in saturday film votes")
            OptFuture.fromFuture(
              fetchFilmInfo.fetchByTitle(message.content).map { f =>
                client.requestsHelper.run(CreateMessage(message.channelId, CreateMessageData(content = f.asMessage))).map(
                  println
                )
              }
            )
          }
          else OptFuture.fromOption(None)
        }
      else {
        OptFuture.fromOption(None)
      }
  }}

  client.onEventSideEffectsIgnore {
    case APIMessage.Ready(_) =>
      println("Connected")
    case x =>
      println(s"other - $x")
  }

}