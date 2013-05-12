package repatch.twitter.response

import dispatch._
import org.json4s._
import java.util.{GregorianCalendar, Calendar, Locale}
import java.text.SimpleDateFormat

case class Search(
  statuses: List[Tweet],
  search_metadata: JObject
)

/** https://dev.twitter.com/docs/api/1.1/get/search/tweets
 */
object Search extends Parse {
  val statuses        = 'statuses.![List[JValue]]
  val search_metadata = 'search_metadata.![JObject]

  def apply(js: JValue): Search = Search(
    statuses = statuses(js) map { x => Tweet(x) },
    search_metadata = search_metadata(js)
  )
}

object MetaData extends Parse {
  val max_id          = 'max_id.![BigInt]
  val since_id        = 'since_id.![BigInt]
  val count           = 'count.![Int]
}

case class Tweet(
  id: BigInt,
  text: String,
  created_at: Calendar,
  user: Option[User],
  favorite_count: Option[Int],
  favorited: Option[Boolean],
  retweet_count: Int,
  retweeted: Boolean,
  truncated: Boolean,
  source: String,
  lang: Option[String],
  coordinates: Option[JObject],
  entities: JObject,
  in_reply_to_status_id: Option[BigInt],
  in_reply_to_user_id: Option[BigInt]
)

/** https://dev.twitter.com/docs/platform-objects/tweets 
 */
object Tweet extends Parse with CommonField {
  val rebracket = (s: String) => s replace ("&gt;", ">") replace ("&lt;", "<")
  val contributors   = 'contributors[List[JValue]]
  val coordinates    = 'coordinates[JObject]
  val current_user_retweet = 'current_user_retweet[JObject]
  val favorite_count = 'favorite_count[Int]
  val favorited      = 'favorited[Boolean]
  val filtere_level  = 'filtere_level[String]
  val in_reply_to_screen_name   = 'in_reply_to_screen_name[String]
  val in_reply_to_status_id     = 'in_reply_to_status_id[BigInt]
  val in_reply_to_status_id_str = 'in_reply_to_status_id_str[String]
  val in_reply_to_user_id       = 'in_reply_to_user_id[BigInt]
  val in_reply_to_user_id_str   = 'in_reply_to_user_id_str[String]
  val place          = 'place[JObject]
  val possibly_sensitive = 'possibly_sensitive[Boolean]
  val scopes         = 'scopes[JObject]
  val source         = 'source.![String]
  val retweet_count  = 'retweet_count.![Int]
  val retweeted      = 'retweeted.![Boolean]
  val text           = 'text.![String] andThen rebracket
  val truncated      = 'truncated.![Boolean]
  val user           = 'user[JObject]

  def apply(js: JValue): Tweet = Tweet(
    id = id(js),
    text = text(js),
    created_at = created_at(js),
    user = user(js) map { case x => User(x) },
    favorite_count = favorite_count(js),
    favorited = favorited(js),
    retweet_count = retweet_count(js),
    retweeted = retweeted(js),
    truncated = truncated(js),
    source = source(js),
    lang = lang(js),
    coordinates = coordinates(js),
    entities = entities(js),
    in_reply_to_status_id = in_reply_to_status_id(js),
    in_reply_to_user_id = in_reply_to_user_id(js)   
  )
}

object Tweets extends Parse {
  def apply(js: JValue): List[Tweet] =
    parse_![List[JValue]](js) map { x => Tweet(x) }
}

case class User(
  id: BigInt,
  screen_name: String,
  created_at: Calendar,
  name: String,
  `protected`: Boolean,
  description: Option[String],
  location: Option[String],
  time_zone: Option[String],
  url: Option[String],
  verified: Boolean,
  statuses_count: Int,
  favourites_count: Int,
  followers_count: Int,
  friends_count: Int,
  default_profile: Boolean,
  default_profile_image: Boolean,
  profile_image_url: String,
  profile_image_url_https: String,
  lang: Option[String],
  entities: JObject
)

/** https://dev.twitter.com/docs/platform-objects/users
 */
object User extends Parse with CommonField {
  val contributors_enabled  = 'contributors_enabled.![Boolean]
  val default_profile       = 'default_profile.![Boolean]
  val default_profile_image = 'default_profile_image.![Boolean]
  val description           = 'description[String]
  val favourites_count      = 'favourites_count.![Int]
  val follow_request_sent   = 'follow_request_sent[Boolean]
  val followers_count       = 'followers_count.![Int]
  val friends_count         = 'friends_count.![Int]
  val geo_enabled           = 'geo_enabled.![Boolean]
  val is_translator         = 'is_translator.![Boolean]
  val listed_count          = 'listed_count.![Int]
  val location              = 'location[String]
  val name                  = 'name.![String]
  val notifications         = 'notifications[Boolean]
  val profile_background_color = 'profile_background_color.![String]
  val profile_background_image_url = 'profile_background_image_url.![String]
  val profile_background_image_url_https = 'profile_background_image_url_https.![String]
  val profile_background_tile  = 'profile_background_tile.![Boolean]
  val profile_banner_url    = 'profile_banner_url.![String]
  val profile_image_url     = 'profile_image_url.![String]
  val profile_image_url_https = 'profile_image_url_https.![String]
  val profile_link_color    = 'profile_link_color.![String]
  val profile_sidebar_border_color = 'profile_sidebar_border_color.![String]
  val profile_sidebar_fill_color   = 'profile_sidebar_fill_color.![String]
  val profile_text_color    = 'profile_text_color.![String]
  val profile_use_background_image = 'profile_use_background_image.![Boolean]
  val `protected`           = 'protected.![Boolean]
  val screen_name           = 'screen_name.![String]
  val show_all_inline_media = 'show_all_inline_media.![Boolean]
  val status                = 'status[JObject]
  val statuses_count        = 'statuses_count.![Int]
  val time_zone             = 'time_zone[String]
  val url                   = 'url[String]
  val utc_offset            = 'utc_offset[String]
  val verified              = 'verified.![Boolean]

  def apply(js: JValue): User = User(
    id = id(js),
    screen_name = screen_name(js),
    created_at = created_at(js),
    name = name(js),
    `protected` = `protected`(js),
    description = description(js),
    location = location(js),
    time_zone = time_zone(js),
    url = url(js),
    verified = verified(js),
    statuses_count = statuses_count(js),
    favourites_count = favourites_count(js),
    followers_count = followers_count(js),
    friends_count = friends_count(js),
    default_profile = default_profile(js),
    default_profile_image = default_profile_image(js),
    profile_image_url = profile_image_url(js),
    profile_image_url_https = profile_image_url_https(js),
    lang = lang(js),
    entities = entities(js)
  )
}

trait CommonField { self: Parse =>
  val id                    = 'id.![BigInt]
  val id_str                = 'id_str.![String]
  val created_at            = 'created_at.![Calendar]
  val entities              = 'entities.![JObject]
  val lang                  = 'lang[String]
  val withheld_copyright    = 'withheld_copyright[Boolean]
  val withheld_in_countries = 'withheld_in_countries[List[JValue]]
  val withheld_scope        = 'withheld_scope[String]
}

trait Parse {
  def parse[A: ReadJs](js: JValue): Option[A] =
    implicitly[ReadJs[A]].readJs.lift(js)
  def parse_![A: ReadJs](js: JValue): A = parse(js).get
  def parseField[A: ReadJs](key: String)(js: JValue): Option[A] = parse[A](js \ key)
  def parseField_![A: ReadJs](key: String)(js: JValue): A = parseField(key)(js).get
  implicit class SymOp(sym: Symbol) {
    def apply[A: ReadJs]: JValue => Option[A] = parseField[A](sym.name)_
    def ![A: ReadJs]: JValue => A = parseField_![A](sym.name)_
  }
}
trait ReadJs[A] {
  import ReadJs.=>?
  val readJs: JValue =>? A
}
object ReadJs {
  type =>?[-A, +B] = PartialFunction[A, B]
  def readJs[A](pf: JValue =>? A): ReadJs[A] = new ReadJs[A] {
    val readJs = pf
  }
  implicit val listRead: ReadJs[List[JValue]] = readJs { case JArray(v) => v }
  implicit val objectRead: ReadJs[JObject]    = readJs { case JObject(v) => JObject(v) }
  implicit val bigIntRead: ReadJs[BigInt]     = readJs { case JInt(v) => v }
  implicit val intRead: ReadJs[Int]           = readJs { case JInt(v) => v.toInt }
  implicit val stringRead: ReadJs[String]     = readJs { case JString(v) => v }
  implicit val boolRead: ReadJs[Boolean]      = readJs { case JBool(v) => v }
  private val twitterFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH)
  twitterFormat.setLenient(true)
  implicit val calendarRead: ReadJs[Calendar] =
    readJs { case JString(v) =>
      val date = twitterFormat.parse(v)
      val c = new GregorianCalendar
      c.setTime(date)
      c
    }
}
