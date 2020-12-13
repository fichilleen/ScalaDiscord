import akka.actor.ActorSystem
import org.scalatest.flatspec._
import org.scalatest.matchers._

class FetchFilmInfoTest extends AnyFlatSpec with must.Matchers {

  implicit val as = ActorSystem("tests")
  val klass = new FetchFilmInfo("xxx")

  "urlParamsFromTitle" must "return the base name without a year" in {
    klass.urlParamsFromTitle("jaws") must be("t=jaws")
  }

  "urlParamsFromTitle" must "convert to lower case" in {
    klass.urlParamsFromTitle("Jaws") must be("t=jaws")
  }

  "urlParamsFromTitle" must "replace spaces with underscores" in {
    klass.urlParamsFromTitle("natural born killers") must be("t=natural_born_killers")
  }

  "urlParamsFromTitle" must "separate year into separate parameter if it exists" in {
    klass.urlParamsFromTitle("jaws (1975)") must be("t=jaws&year=1975")
  }

  "urlParamsFromTitle" must "strip any trailing space" in {
    klass.urlParamsFromTitle("jaws ") must be("t=jaws")
  }
}
