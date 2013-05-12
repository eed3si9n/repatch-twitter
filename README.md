repatch-twitter
===============

repatch-twitter is a Dispatch 0.10 plugin for Twitter API.

setup
-----

```scala
resolvers += "sonatype-public" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies += "com.eed3si9n" %% "repatch-twitter-core" % "dispatch0.10.0_0.1.0-SNAPSHOT"
```

authentication
--------------

`repatch.twitter.request.AbstractClient` abstracts the OAuth authentication to use Twitter API. `OAuthClient` implements it.

```scala
scala> import dispatch._, Defaults._
import dispatch._
import Defaults._

scala> import com.ning.http.client.oauth._
import com.ning.http.client.oauth._

scala> import repatch.twitter.request._
import repatch.twitter.request._

scala> val consumer = new ConsumerKey("abcd", "secret")
consumer: com.ning.http.client.oauth.ConsumerKey = {Consumer key, key="abcd", secret="secret"}

scala> val accessToken = new RequestToken("xyz", "secret")
accessToken: com.ning.http.client.oauth.RequestToken = { key="xyz", secret="secret"}

scala> val client = OAuthClient(consumer, accessToken)
client: repatch.twitter.request.OAuthClient = <function1>
```

There's `ProperitesClient` that can load the consumer key and the access token from the given file name:

```scala
scala> val prop = new java.io.File(System.getProperty("user.home"), ".foo.properties")
prop: java.io.File = /Users/you/.foo.properties

scala> val client = PropertiesClient(prop)
client: repatch.twitter.request.OAuthClient = <function1>
```

search
------

### [GET search/tweets](https://dev.twitter.com/docs/api/1.1/get/search/tweets)

Here's how you can search for tweets:

```scala
scala> val x = http(client(Search("#scala").geocode_mi(40.7142, -74.0064, 10).count(2)) OK
         as.repatch.twitter.response.Search)
x2: dispatch.Future[repatch.twitter.response.Search] = scala.concurrent.impl.Promise$DefaultPromise@6bc9806d

scala> val search = x()
search: repatch.twitter.response.Search = Search(List(Tweet(330931826879234049,Rocking the contravariance. Hard. #nerd...
```

In the above, `repatch.twitter.request.Search` is a request builder, supporting methods such as `geocode_mi` and `count`, which return `Search`. Using `as.repatch.twitter.response.Search` converter returns `repatch.twitter.response.Search`. Alternatively, you can return json, and parse individual fields as follows:

```scala
scala> val x = http(client(Search("#scala").geocode_mi(40.7142, -74.0064, 10).count(2)) OK as.json4s.Json)
x: dispatch.Future[org.json4s.JValue] = scala.concurrent.impl.Promise$DefaultPromise@3252d2de

scala> val json = x()
json: org.json4s.JValue = 
JObject(List((statuses,JArray(List(JObject(List((metadata,JObject(List((result_...

scala> {
         import repatch.twitter.response.Search._
         import repatch.twitter.response.Tweet._
         for {
           t <- statuses(json)
         } yield(id_str(t), text(t))
       }
res0: List[(String, String)] = List((330931826879234049,Rocking the contravariance. Hard. #nerd #scala), (330877539461500928,RT @mhamrah: Excellent article on structuring distributed systems with #rabbitmq. Thanks @heroku Scaling Out with #Scala and #Akka http://t…))
```

timelines
---------

### [GET statuses/home_timeline](https://dev.twitter.com/docs/api/1.1/get/statuses/home_timeline)

Here's how to get your timeline.

```scala
scala> val x = http(client(Status.home_timeline) OK as.repatch.twitter.response.Tweets)
x: dispatch.Future[repatch.twitter.response.Statuses] = scala.concurrent.impl.Promise$DefaultPromise@41ad625a

scala> x()
res0: List[repatch.twitter.response.Tweet] = 
List(Tweet(331691122629951489,Partially applying a function that has an implicit parameter http://t.co/CwWQAkkBAN,....
```

### [GET statuses/mentions_timeline](https://dev.twitter.com/docs/api/1.1/get/statuses/mentions_timeline)

> Returns the 20 most recent mentions.

```scala
scala> val timeline = http(client(Status.mentions_timeline) OK as.repatch.twitter.response.Tweets)
timeline: dispatch.Future[List[repatch.twitter.response.Tweet]] = scala.concurrent.impl.Promise$DefaultPromise@53a5af37

scala> timeline()
res0: List[repatch.twitter.response.Tweet] = 
List(Tweet(333386163618455553,@eed3si9n I keep seeing pros and cons...
```

### [GET statuses/user_timeline](https://dev.twitter.com/docs/api/1.1/get/statuses/user_timeline)

> Returns a collection of the most recent Tweets posted by the user indicated by the `screen_name` or `user_id` parameters.

```scala
scala> val timeline = http(client(Status.user_timeline("twitterapi")) OK as.repatch.twitter.response.Tweets)
timeline: dispatch.Future[List[repatch.twitter.response.Tweet]] = scala.concurrent.impl.Promise$DefaultPromise@40878d74

scala> timeline()
res0: List[repatch.twitter.response.Tweet] = 
List(Tweet(330369772619452416,We're extending API v1 Retirement...
```

### [GET statuses/retweets_of_me](https://dev.twitter.com/docs/api/1.1/get/statuses/retweets_of_me)

> Returns the most recent tweets authored by the authenticating user that have been retweeted by others.

```scala
scala> val timeline = http(client(Status.retweets_of_me) OK as.repatch.twitter.response.Tweets)
timeline: dispatch.Future[List[repatch.twitter.response.Tweet]] = scala.concurrent.impl.Promise$DefaultPromise@14128779
```

tweets
------

### [POST statuses/update](https://dev.twitter.com/docs/api/1.1/post/statuses/update)

Send a tweet.

```scala
scala> val x = http(client(Status.update("testing from REPL"))
         OK as.repatch.twitter.response.Tweet)
x: dispatch.Future[repatch.twitter.response.Tweet = scala.concurrent.impl.Promise$DefaultPromise@65056d18
```

To reply to a tweet, call `in_reply_to_status_id(id)` method on `Status.Update` class.
