package models
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.reflect.ClassTag


trait PascalCaseJsonProtocol extends DefaultJsonProtocol {
  override protected def extractFieldNames(classTag: ClassTag[_]): Array[String] =
    super.extractFieldNames(classTag).map(pascalCase)

  def pascalCase(name: String): String = {
    val x = name.head
    x.toString.toUpperCase ++ name.tail
  }
}

object PascalCaseJsonProtocol extends PascalCaseJsonProtocol

case class FilmResponse(title: String, year: String, runtime: String, genre: String, poster: String, plot: String, ratings: List[Rating]) {
  def asMessage: String = {
    val critics = ratings.map(s => s"${s.source} - ${s.value}").mkString("\n")
    s"""|**$title ($year)**
       |**Run time**  : $runtime
       |**Genre**     : $genre
       |**Plot**      : $plot
       |**Ratings**   :
       |$critics
       |$poster""".stripMargin
  }
}

case class Rating(source: String, value: String)
case class AllRatings(ratings: List[Rating])

object FilmResponseImplicits extends PascalCaseJsonProtocol {
  implicit val ratingFormat: RootJsonFormat[Rating] = jsonFormat2(Rating)
  implicit val allRatingsFormat: RootJsonFormat[AllRatings] = jsonFormat1(AllRatings)
  implicit val filmResponseFormat: RootJsonFormat[FilmResponse] = jsonFormat7(FilmResponse)
}


