package dispatch.as.repatch.twitter.response

package object stream {
  import com.ning.http.client.Response    
  import repatch.twitter.{response => r}
  import dispatch.as.json4s.stream.Json
  import org.json4s._

  def TweetOrJson[A](f: Either[JValue, r.Tweet] => A) = Json[A] {
    case r.Tweet(tweet) => f(Right(tweet))
    case json           => f(Left(json))
  } 
}
