package dispatch.as.repatch.twitter

package object response {
  import com.ning.http.client.Response    
  import repatch.twitter.response
  import dispatch.as.json4s.Json

  val Search: Response => response.Search = Json andThen response.Search.apply
  val Statuses: Response => List[response.Tweet] = Json andThen response.Statuses.apply
}
