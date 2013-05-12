package repatch.twitter.request

import dispatch._
import org.json4s._
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
  implicit val doubleShow  = showA[Double]
  private val yyyyMmDd = new SimpleDateFormat("yyyy-MM-dd")
  implicit val calendarShow: Show[Calendar] = new Show[Calendar] {
    def shows(a: Calendar): String = yyyyMmDd.format(a.getTime)
  }
}

// https://api.twitter.com/1.1/search/tweets.json
case class Search(params: Map[String, String]) extends Method
    with Param[Search] with CommonParam[Search] {
  def complete = _ / "search" / "tweets.json" <<? params

  def param[A: Show](key: String)(value: A): Search =
    copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  private def geocode0(unit: String) = (lat: Double, lon: Double, r: Double) =>
    param[String]("geocode")(List(lat, lon, r).mkString(",") + unit)
  val geocode_mi      = geocode0("mi")
  val geocode         = geocode0("km")
  val lang            = 'lang[String]
  val locale          = 'locale[String]
  /**  mixed, recent, popular */
  val result_type     = 'result_type[String]
  val until           = 'until[Calendar]
  val include_entities = 'include_entities[Boolean]
  val callback        = 'callback[String]
}
object Search {
  def apply(q: String): Search = Search(Map("q" -> q))
}

object Status {
  /** See https://dev.twitter.com/docs/api/1.1/get/statuses/mentions_timeline
   */
  def mentions_timeline: MentionsTimeline = MentionsTimeline()
  case class MentionsTimeline(params: Map[String, String] = Map()) extends Method
      with Param[MentionsTimeline] with TimelineParam[MentionsTimeline] {
    def complete = _ / "statuses" / "mentions_timeline.json" <<? params
    def param[A: Show](key: String)(value: A): MentionsTimeline =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }

  /** See https://dev.twitter.com/docs/api/1.1/get/statuses/home_timeline
   */ 
  def home_timeline: HomeTimeline = HomeTimeline()
  case class HomeTimeline(params: Map[String, String] = Map()) extends Method
      with Param[HomeTimeline] with TimelineParam[HomeTimeline] {
    def complete = _ / "statuses" / "home_timeline.json" <<? params
    def param[A: Show](key: String)(value: A): HomeTimeline =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }

  /** See https://dev.twitter.com/docs/api/1.1/get/statuses/user_timeline
   */
  def user_timeline(user_id: BigInt): UserTimeline = UserTimeline(Map("user_id" -> user_id.toString))
  def user_timeline(screen_name: String): UserTimeline = UserTimeline(Map("screen_name" -> screen_name))
  case class UserTimeline(params: Map[String, String]) extends Method
      with Param[UserTimeline] with TimelineParam[UserTimeline] {
    def complete = _ / "statuses" / "user_timeline.json" <<? params
    def param[A: Show](key: String)(value: A): UserTimeline =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
    val include_rts      = 'include_rts[Boolean]
  }

  /** See https://dev.twitter.com/docs/api/1.1/get/statuses/retweets_of_me
   */
  def retweets_of_me: RetweetsOfMeTimeline = RetweetsOfMeTimeline()
  case class RetweetsOfMeTimeline(params: Map[String, String] = Map()) extends Method
      with Param[RetweetsOfMeTimeline] with TimelineParam[RetweetsOfMeTimeline] {
    def complete = _ / "statuses" / "retweets_of_me.json" <<? params
    def param[A: Show](key: String)(value: A): RetweetsOfMeTimeline =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
    val include_user_entities = 'include_user_entities[Boolean]
  }  

  /** See https://dev.twitter.com/docs/api/1.1/get/statuses/retweets_of_me
   */
  def retweets(id: BigInt): Retweets = Retweets(id)
  case class Retweets(id: BigInt, params: Map[String, String] = Map()) extends Method
      with Param[Retweets] with TimelineParam[Retweets] {
    def complete = _ / "statuses" / "retweets" / ("%s.json" format id.toString) <<? params
    def param[A: Show](key: String)(value: A): Retweets =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }

  /** See https://dev.twitter.com/docs/api/1.1/get/statuses/show/%3Aid
   */
  def show(id: BigInt): ShowStatus = ShowStatus(id)
  case class ShowStatus(id: BigInt, params: Map[String, String] = Map()) extends Method
      with Param[ShowStatus] with TimelineParam[ShowStatus] {
    def complete = _ / "statuses" / "show" / ("%s.json" format id.toString) <<? params
    def param[A: Show](key: String)(value: A): ShowStatus =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
    val include_my_retweet = 'include_my_retweet[Boolean]
  }

  /** See https://dev.twitter.com/docs/api/1.1/post/statuses/destroy/%3Aid
   */
  def destroy(id: BigInt): DestroyStatus = DestroyStatus(id)
  case class DestroyStatus(id: BigInt, params: Map[String, String] = Map()) extends Method
      with Param[DestroyStatus] {
    def complete = _ / "statuses" / "destroy" / ("%s.json" format id.toString) << params
    def param[A: Show](key: String)(value: A): DestroyStatus =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
    val trim_user       = 'trim_user[Boolean]
  }
  
  /** See https://dev.twitter.com/docs/api/1.1/post/statuses/update
   */
  def update(status: String): Update = Update(Map("status" -> status))
  case class Update(params: Map[String, String]) extends Method with Param[Update] {
    def complete = _ / "statuses" / "update.json" << params

    def param[A: Show](key: String)(value: A): Update =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
    val in_reply_to_status_id = 'in_reply_to_status_id[BigInt]
    val lat             = 'lat[Double]
    val `long`          = 'long[Double]
    val place_id        = 'place_id[String]
    val display_coordinates = 'display_coordinates[Boolean]
    val trim_user       = 'trim_user[Boolean]
  }
}

trait TimelineParam[R] extends CommonParam[R] { self: Param[R] =>
  val trim_user       = 'trim_user[Boolean]
  val exclude_replies = 'exclude_replies[Boolean]
  val contributor_details = 'contributor_details[Boolean]
  val include_entities = 'include_entities[Boolean]
}

trait CommonParam[R] { self: Param[R] =>
  val count           = 'count[Int]
  val since_id        = 'since_id[BigInt]
  val max_id          = 'max_id[BigInt]
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
