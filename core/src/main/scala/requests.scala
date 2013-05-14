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

// See https://api.twitter.com/1.1/search/tweets.json
case class Search(params: Map[String, String]) extends Method
    with Param[Search] with CountParam[Search] {
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
      with Param[DestroyStatus] with TrimUserParam[DestroyStatus] {
    def complete = _ / "statuses" / "destroy" / ("%s.json" format id.toString) << params
    def param[A: Show](key: String)(value: A): DestroyStatus =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }

  /** See https://dev.twitter.com/docs/api/1.1/post/statuses/update
   */
  def update(status: String): Update = Update(Map("status" -> status))
  case class Update(params: Map[String, String]) extends Method
      with Param[Update] with TrimUserParam[Update] {
    def complete = _ / "statuses" / "update.json" << params

    def param[A: Show](key: String)(value: A): Update =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
    val in_reply_to_status_id = 'in_reply_to_status_id[BigInt]
    val lat             = 'lat[Double]
    val `long`          = 'long[Double]
    val place_id        = 'place_id[String]
    val display_coordinates = 'display_coordinates[Boolean]
  }

  /** See https://dev.twitter.com/docs/api/1.1/post/statuses/retweet/%3Aid
   */
  def retweet(id: BigInt): Retweet = Retweet(id)
  case class Retweet(id: BigInt, params: Map[String, String] = Map()) extends Method
      with Param[Retweet] with TrimUserParam[Retweet] {
    def complete = _ / "statuses" / "retweet" / ("%s.json" format id.toString) << params
    def param[A: Show](key: String)(value: A): Retweet =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }
}

/** See https://dev.twitter.com/docs/api/1.1/post/favorites/create
 */
case class Favorite(params: Map[String, String]) extends Method
    with Param[Favorite] with IncludeEntitiesParam[Favorite] {
  def complete = _ / "favorites" / "create.json" << params
  def param[A: Show](key: String)(value: A): Favorite =
    copy(params = params + (key -> implicitly[Show[A]].shows(value)))
}

object Favorite {
  def apply(id: BigInt): Favorite = Favorite(Map("id" -> id.toString))
  def create(id: BigInt): Favorite = apply(id)

  /** See https://dev.twitter.com/docs/api/1.1/post/favorites/destroy
   */
  def destroy(id: BigInt): DestroyFavorite = DestroyFavorite(Map("id" -> id.toString))
  case class DestroyFavorite(params: Map[String, String]) extends Method
      with Param[DestroyFavorite] with IncludeEntitiesParam[DestroyFavorite] {
    def complete = _ / "favorites" / "destroy.json" << params
    def param[A: Show](key: String)(value: A): DestroyFavorite =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }

  /** See https://dev.twitter.com/docs/api/1.1/get/favorites/list
   */
  def list: ListFavorite = ListFavorite()
  def list(user_id: BigInt): ListFavorite = ListFavorite(Map("user_id" -> user_id.toString))
  def list(screen_name: String): ListFavorite = ListFavorite(Map("screen_name" -> screen_name))
  case class ListFavorite(params: Map[String, String] = Map()) extends Method
      with Param[ListFavorite] with CountParam[ListFavorite] with IncludeEntitiesParam[ListFavorite] {
    def complete = _ / "favorites" / "list.json" <<? params
    def param[A: Show](key: String)(value: A): ListFavorite =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }
}

/** https://dev.twitter.com/docs/api/1.1/post/statuses/filter
 */
case class PublicStream(params: Map[String, String]) extends Method
    with Param[PublicStream] with StreamParam[PublicStream] {
  def complete = (_: Req) => url("https://stream.twitter.com/1.1/statuses/filter.json") << params
  def param[A: Show](key: String)(value: A): PublicStream =
    copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  val follow          = 'follow[String]
}

object PublicStream {
  private def empty: PublicStream = PublicStream(Map())
  def follow(a: String): PublicStream = empty.follow(a)
  /** Keywords to track. Phrases of keywords are specified by a comma-separated list. */
  def track(a: String): PublicStream = empty.track(a)
  def locations(a: String): PublicStream = empty.locations(a)
}

/** See https://dev.twitter.com/docs/api/1.1/get/user
 */
case class UserStream(params: Map[String, String] = Map()) extends Method
    with Param[UserStream] with StreamParam[UserStream] {
  def complete = (_: Req) => url("https://userstream.twitter.com/1.1/user.json") << params
  def param[A: Show](key: String)(value: A): UserStream =
    copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  /** user, followings */
  val `with`          = 'with[String]
  /** all */
  val replies         = 'replies[String]
}

object Friend {
  /** See https://dev.twitter.com/docs/api/1.1/get/friends/ids
   */
  def ids: FriendIds = FriendIds()
  case class FriendIds(params: Map[String, String] = Map()) extends Method
      with Param[FriendIds] with FriendParam[FriendIds] {
    def complete = _ / "friends" / "ids.json" <<? params
    def param[A: Show](key: String)(value: A): FriendIds =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }
}

object Follower {
  /** See https://dev.twitter.com/docs/api/1.1/get/followers/ids
   */
  def ids: FollowerIds = FollowerIds()
  case class FollowerIds(params: Map[String, String] = Map()) extends Method
      with Param[FollowerIds] with FriendParam[FollowerIds] {
    def complete = _ / "followers" / "ids.json" <<? params
    def param[A: Show](key: String)(value: A): FollowerIds =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }
}

/** See https://dev.twitter.com/docs/api/1.1/post/friendships/create
 */
case class Friendship(params: Map[String, String]) extends Method
    with Param[Friendship] {
  def complete = _ / "friendships" / "create.json" << params
  def param[A: Show](key: String)(value: A): Friendship =
    copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  val follow          = 'follow[Boolean]
}

object Friendship {
  def apply(user_id: BigInt): Friendship = Friendship(Map("user_id" -> user_id.toString))
  def apply(screen_name: String): Friendship = Friendship(Map("screen_name" -> screen_name))

  /** See https://dev.twitter.com/docs/api/1.1/post/friendships/destroy
   */
  def destroy(user_id: BigInt): DestroyFriendship = DestroyFriendship(Map("user_id" -> user_id.toString))
  def destroy(screen_name: String): DestroyFriendship = DestroyFriendship(Map("screen_name" -> screen_name))
  case class DestroyFriendship(params: Map[String, String]) extends Method
      with Param[DestroyFriendship] {
    def complete = _ / "friendships" / "destroy.json" << params
    def param[A: Show](key: String)(value: A): DestroyFriendship =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }
}

object User {
  /** See https://dev.twitter.com/docs/api/1.1/get/users/show
   */
  def show(user_id: BigInt): ShowUser = ShowUser(Map("user_id" -> user_id.toString))
  def show(screen_name: String): ShowUser = ShowUser(Map("screen_name" -> screen_name))
  case class ShowUser(params: Map[String, String]) extends Method
      with Param[ShowUser] with IncludeEntitiesParam[ShowUser] {
    def complete = _ / "users" / "show.json" <<? params
    def param[A: Show](key: String)(value: A): ShowUser =
      copy(params = params + (key -> implicitly[Show[A]].shows(value)))
  }
}

trait TimelineParam[R] extends CountParam[R]
    with TrimUserParam[R] with IncludeEntitiesParam[R] { self: Param[R] =>
  val exclude_replies = 'exclude_replies[Boolean]
  val contributor_details = 'contributor_details[Boolean]
}

trait IncludeEntitiesParam[R] { self: Param[R] =>
  val include_entities = 'include_entities[Boolean]
}

trait TrimUserParam[R] { self: Param[R] =>
  val trim_user       = 'trim_user[Boolean]
}

trait CountParam[R] { self: Param[R] =>
  val count           = 'count[Int]
  val since_id        = 'since_id[BigInt]
  val max_id          = 'max_id[BigInt]
}

trait StreamParam[R] { self: Param[R] =>
  /** length */
  val delimited       = 'delimited[String]
  val stall_warnings  = 'stall_warnings[Boolean]
  val locations       = 'locations[String]
  val track           = 'track[String]
}

trait UserIdParam[R] { self: Param[R] =>
  val user_id         = 'user_id[BigInt]
  val screen_name     = 'screen_name[String]
}

trait FriendParam[R] extends UserIdParam[R] { self: Param[R] =>
  val cursor          = 'cursor[BigInt]
  val stringify_ids   = 'stringify_ids[Boolean]
  val count           = 'count[Int]
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
