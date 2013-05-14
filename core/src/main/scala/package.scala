package dispatch.as.repatch.twitter

package object response {
  import com.ning.http.client.Response    
  import repatch.twitter.{response => r}
  import dispatch.as.json4s.Json

  val Search: Response => r.Search = Json andThen r.Search.apply
  val Tweets: Response => List[r.Tweet] = Json andThen r.Tweets.apply
  val Statuses: Response => List[r.Tweet] = Tweets
  val Tweet: Response => r.Tweet = Json andThen r.Tweet.apply
  val Status: Response => r.Tweet = Tweet
  val User: Response => r.User = Json andThen r.User.apply
}
