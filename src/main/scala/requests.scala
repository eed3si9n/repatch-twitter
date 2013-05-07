package repatch.twitter.request

import dispatch._
import org.json4s._
import oauth._
import com.ning.http.client.oauth._
import java.util.Calendar
import java.text.SimpleDateFormat

trait Show[A] {
  def shows(a: A): String
}
object Show {
  def showA[A]: Show[A] = new Show[A] {
    def shows(a: A): String = a.toString 
  }
  implicit val stringShow  = showA[String]
  implicit val intShow     = showA[Int]
  implicit val bigIntShow  = showA[BigInt]
  implicit val booleanShow = showA[Boolean]
  private val yyyyMmDd = new SimpleDateFormat("yyyy-MM-dd")
  implicit val calendarShow: Show[Calendar] = new Show[Calendar] {
    def shows(a: Calendar): String = yyyyMmDd.format(a.getTime)
  }
}

// https://api.twitter.com/1.1/search/tweets.json
case class Search(params: Map[String, String]) extends Method with Param[Search] {
  def complete = _ / "search" / "tweets.json" <<? params

  def param[A: Show](key: String)(value: A): Search =
    copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  private def geocode0(unit: String) = (lat: Double, lon: Double, r: Double) =>
    param[String]("geocode")(List(lat, lon, r).mkString(",") + unit)
  val geocode_mi = geocode0("mi")
  val geocode  = geocode0("km")
  val lang     = 'lang[String]
  val locale   = 'locale[String]
  /**  mixed, recent, popular */
  val result_type = 'result_type[String]
  val count    = 'count[Int]
  val until    = 'until[Calendar]
  val since_id = 'since_id[BigInt]
  val max_id   = 'max_id[BigInt]
  val include_entities = 'include_entities[Boolean]
  val callback = 'callback[String]
}
case object Search {
  def apply(q: String): Search = Search(Map("q" -> q))
}

trait Param[R] {
  val params: Map[String, String]
  def param[A: Show](key: String)(value: A): R
  implicit class SymOp(sym: Symbol) {
    def apply[A: Show]: A => R = param(sym.name)_
  }
}
trait Method extends (Req => Req) {
  def complete: Req => Req
  def apply(req: Req): Req = complete(req)
}

/** AbstractClient is a function to wrap API operations */
trait AbstractClient extends (Method => Req) {
  def hostName = "api.twitter.com"
  def host = :/(hostName).secure / "1.1"
  def apply(method: Method): Req = method(host)  
}

// ConsumerKey(key: String, secret: String) 
// RequestToken(key: String, token: String) 
case class OAuthClient(consumer: ConsumerKey, token: RequestToken) extends AbstractClient {
  override def apply(method: Method): Req = method(host) sign(consumer, token)
}

object OAuthClient {
  def apply(consumer: ConsumerKey): OAuthClient = OAuthClient(consumer, emptyToken)
  val emptyToken = new RequestToken("", "")
}

trait TwitterEndpoints extends SomeEndpoints {
  def requestToken: String = "https://api.twitter.com/oauth/request_token"
  def accessToken: String = "https://api.twitter.com/oauth/access_token"
  def authorize: String = "https://api.twitter.com/oauth/authorize"
}

case class OAuthExchange(http: HttpExecutor, consumer: ConsumerKey, callback: String) extends
  SomeHttp with SomeConsumer with TwitterEndpoints with SomeCallback with Exchange {
}
