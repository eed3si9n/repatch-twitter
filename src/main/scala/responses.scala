package repatch.twitter.response

import dispatch._
import org.json4s._

object Search extends Parse {
  val statuses       = 'statuses.![List[JValue]]
}

/** https://dev.twitter.com/docs/platform-objects/tweets 
 */
object Tweet extends Parse {
  val contributors   = 'contributors[List[JValue]]
  val coordinates    = 'coordinates[JObject]
  val created_at     = 'created_at.![String]
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
  val scope          = 'scope.![JObject]
  val retweet_count  = 'retweet_count.![Int]
  val retweeted      = 'retweeted.![Boolean]
  val text           = 'text.![String]
  val truncated      = 'truncated.![Boolean]
  val user           = 'user.![JObject]
  val withheld_copyright    = 'withheld_copyright[Boolean]
  val withheld_in_countries = 'withheld_in_countries[List[JValue]]
  val withheld_scope        = 'withheld_scope[String]
}

trait Parse {
  def parse[A: Read](key: String)(js: JValue): Option[A] =
    implicitly[Read[A]].readJs.lift(js \ key)
  def parse_![A: Read](key: String)(js: JValue): A = parse(key)(js).get
  implicit class SymOp(sym: Symbol) {
    def apply[A: Read]: JValue => Option[A] = parse[A](sym.name)_
    def ![A: Read]: JValue => A = parse_![A](sym.name)_
  }
}
trait Read[A] {
  import Read.=>?
  val readJs: JValue =>? A
}
object Read {
  type =>?[-A, +B] = PartialFunction[A, B]
  def readJs[A](pf: JValue =>? A): Read[A] = new Read[A] {
    val readJs = pf
  }
  implicit val listRead: Read[List[JValue]] = readJs { case JArray(v) => v }
  implicit val objectRead: Read[JObject]    = readJs { case JObject(v) => JObject(v) }
  implicit val bigIntRead: Read[BigInt]     = readJs { case JInt(v) => v }
  implicit val intRead: Read[Int]           = readJs { case JInt(v) => v.toInt }
  implicit val stringRead: Read[String]     = readJs { case JString(v) => v }
  implicit val boolRead: Read[Boolean]      = readJs { case JBool(v) => v }
}
