package repatch.twitter.request

import dispatch._
import oauth._
import com.ning.http.client.oauth._
import java.io.{File, FileInputStream}
import java.util.Properties

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

object PropertiesClient {
  def apply(props: Properties): OAuthClient = {
    val consumer = new ConsumerKey(props getProperty "repatch.twitter.consumerKey",
      props getProperty "repatch.twitter.consumerKeySecret")
    val token = new RequestToken(props getProperty "repatch.twitter.accessToken",
      props getProperty "repatch.twitter.accessTokenSecret")
    OAuthClient(consumer, token)
  }
  def apply(file: File): OAuthClient = {
    val props = new Properties()
    props load new FileInputStream(file)
    apply(props)
  }
}

trait TwitterEndpoints extends SomeEndpoints {
  def requestToken: String = "https://api.twitter.com/oauth/request_token"
  def accessToken: String = "https://api.twitter.com/oauth/access_token"
  def authorize: String = "https://api.twitter.com/oauth/authorize"
}

case class OAuthExchange(http: HttpExecutor, consumer: ConsumerKey, callback: String) extends
  SomeHttp with SomeConsumer with TwitterEndpoints with SomeCallback with Exchange {
}
