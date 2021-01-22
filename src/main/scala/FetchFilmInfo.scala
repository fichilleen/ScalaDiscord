import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import models.FilmResponse

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.matching.Regex

class FetchFilmInfo(apiKey: String)(implicit as: ActorSystem, mat: Materializer) {
  import models.FilmResponseImplicits._
  implicit val dispatcher: ExecutionContextExecutor = as.dispatcher

  final val filmWithYearRegex: Regex = "(.*) \\(([0-9]{4})\\)".r
  final val baseUrl: String = s"http://www.omdbapi.com/?apikey=$apiKey"

  def fetchByTitle(title: String): Future[FilmResponse] = {

    val requestUri = s"$baseUrl&${urlParamsFromTitle(title)}"
    val request: HttpRequest = Get(requestUri)
    val response = Http().singleRequest(request)
    println(s"Called the api with $requestUri")

    response.flatMap(r =>
      Unmarshal(r).to[FilmResponse]
    )
  }

  def urlParamsFromTitle(title: String): String = {
    def normaliseTitle(title: String): String = title.toLowerCase.trim.replaceAll(" ", "_")

    val hasYear = filmWithYearRegex.findAllIn(title)
    if (hasYear.nonEmpty) {
      s"t=${normaliseTitle(hasYear.group(1))}&y=${hasYear.group(2)}"
    } else s"t=${normaliseTitle(title)}"
  }

}
