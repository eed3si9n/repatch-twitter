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

case class Tweet(
  id: BigInt,
  text: String,
  created_at: Calendar,
  user: JObject,
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
object Tweet extends Parse {
  val contributors   = 'contributors[List[JValue]]
  val coordinates    = 'coordinates[JObject]
  val created_at     = 'created_at.![Calendar]
  val current_user_retweet = 'current_user_retweet[JObject]
  val entities       = 'entities.![JObject]
  val favorite_count = 'favorite_count[Int]
  val favorited      = 'favorited[Boolean]
  val filtere_level  = 'filtere_level[String]
  val id             = 'id.![BigInt]
  val id_str         = 'id_str.![String]
  val in_reply_to_screen_name   = 'in_reply_to_screen_name[String]
  val in_reply_to_status_id     = 'in_reply_to_status_id[BigInt]
  val in_reply_to_status_id_str = 'in_reply_to_status_id_str[String]
  val in_reply_to_user_id       = 'in_reply_to_user_id[BigInt]
  val in_reply_to_user_id_str   = 'in_reply_to_user_id_str[String]
  val lang           = 'lang[String]
  val place          = 'place[JObject]
  val possibly_sensitive = 'possibly_sensitive[Boolean]
  val scopes         = 'scopes[JObject]
  val source         = 'source.![String]
  val retweet_count  = 'retweet_count.![Int]
  val retweeted      = 'retweeted.![Boolean]
  val text           = 'text.![String]
  val truncated      = 'truncated.![Boolean]
  val user           = 'user.![JObject]
  val withheld_copyright    = 'withheld_copyright[Boolean]
  val withheld_in_countries = 'withheld_in_countries[List[JValue]]
  val withheld_scope        = 'withheld_scope[String]

  def apply(js: JValue): Tweet = Tweet(
    id = id(js),
    text = text(js),
    created_at = created_at(js),
    user = user(js),
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

trait Parse {
  def parse[A: ReadJs](key: String)(js: JValue): Option[A] =
    implicitly[ReadJs[A]].readJs.lift(js \ key)
  def parse_![A: ReadJs](key: String)(js: JValue): A = parse(key)(js).get
  implicit class SymOp(sym: Symbol) {
    def apply[A: ReadJs]: JValue => Option[A] = parse[A](sym.name)_
    def ![A: ReadJs]: JValue => A = parse_![A](sym.name)_
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
