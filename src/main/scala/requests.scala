package repatch.twitter.request

import dispatch._
import org.json4s._
import oauth._
import com.ning.http.client.oauth._

// https://api.twitter.com/1.1/search/tweets.json
case class Search(params: Map[String, String]) extends Method {
  def param(key: String, value: String): Search = copy(params = params + (key -> value))
  def complete = _ / "search" / "tweets.json" <<? params
}
case object Search {
  def apply(q: String): Search = Search(Map("q" -> q))
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
