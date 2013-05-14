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

### [POST statuses/retweet/:id](https://dev.twitter.com/docs/api/1.1/post/statuses/retweet/%3Aid)

> Retweets a tweet.

```scala
scala> val rt = http(client(Status.retweet(res0.id)) OK as.repatch.twitter.response.Tweet)
rt: dispatch.Future[repatch.twitter.response.Tweet] = scala.concurrent.impl.Promise$DefaultPromise@6758cd8

scala> rt()
res7: repatch.twitter.response.Tweet = Tweet(333519011696484352,RT @PLT_Hulk: ...
```

### [GET statuses/retweets/:id](https://dev.twitter.com/docs/api/1.1/get/statuses/retweets/%3Aid)

> Returns up to 100 of the first retweets of a given tweet.

```scala
scala> val rts = http(client(Status.retweets(BigInt("317744323254943744"))) OK
         as.repatch.twitter.response.Tweets)
rts: dispatch.Future[List[repatch.twitter.response.Tweet]] = scala.concurrent.impl.Promise$DefaultPromise@98fd639

scala> rts()
res4: List[repatch.twitter.response.Tweet] = 
List(Tweet(317776021933916160,RT @eed3si9n: scala> :k Monad // Finds locally imported types.
```

### [GET statuses/show/:id](https://dev.twitter.com/docs/api/1.1/get/statuses/show/%3Aid)

> Returns a single Tweet, specified by the id parameter.

```scala
scala> val x = http(client(Status.show(BigInt("317744323254943744"))) OK
         as.repatch.twitter.response.Tweet)
x: dispatch.Future[repatch.twitter.response.Tweet] = scala.concurrent.impl.Promise$DefaultPromise@4c603a77

scala> x()
res2: repatch.twitter.response.Tweet = 
Tweet(317744323254943744,scala> :k Monad // Finds locally imported types
```

### [POST statuses/destroy/:id](https://dev.twitter.com/docs/api/1.1/post/statuses/destroy/%3Aid)

> Destroys the status specified by the required ID parameter.

```scala
scala> x()
res0: repatch.twitter.response.Tweet = Tweet(333505115287846913, ...

scala> val deleted = http(client(Status.destroy(res0.id)) OK as.repatch.twitter.response.Tweet)
deleted: dispatch.Future[repatch.twitter.response.Tweet] = scala.concurrent.impl.Promise$DefaultPromise@a852084

scala> deleted()
res3: repatch.twitter.response.Tweet = Tweet(333505115287846913, ...
```

favorites
---------

### [GET favorites/list](https://dev.twitter.com/docs/api/1.1/get/favorites/list)

> Returns the 20 most recent Tweets favorited by the authenticating or specified user.

```scala
scala> val myfavs = http(client(Favorite.list) OK as.repatch.twitter.response.Tweets)
favs: dispatch.Future[List[repatch.twitter.response.Tweet]] = scala.concurrent.impl.Promise$DefaultPromise@4482d3a3

scala> myfavs()
res0: List[repatch.twitter.response.Tweet] = List(Tweet(331773677295239169

scala> val favs = http(client(Favorite.list("PLT_HULK")) OK as.repatch.twitter.response.Tweets)
favs: dispatch.Future[List[repatch.twitter.response.Tweet]] = scala.concurrent.impl.Promise$DefaultPromise@366ec9d4

scala> favs()
res1: List[repatch.twitter.response.Tweet] = List(Tweet(246253900770983937,@PLT_Hulk is angry lately.,...
```

### [POST favorites/create](https://dev.twitter.com/docs/api/1.1/post/favorites/create)

> Favorites the status specified in the ID parameter as the authenticating user.

```scala
scala> val fav = http(client(Favorite(res0.id)) OK as.repatch.twitter.response.Tweet)
fav: dispatch.Future[repatch.twitter.response.Tweet] = scala.concurrent.impl.Promise$DefaultPromise@12810908

scala> fav()
res1: repatch.twitter.response.Tweet = Tweet(333297803155611649,
```

### [POST favorites/destroy](https://dev.twitter.com/docs/api/1.1/post/favorites/destroy)

> Un-favorites the status specified in the ID parameter as the authenticating user.

```scala
scala> val unfav = http(client(Favorite.destroy(res0.id)) OK as.repatch.twitter.response.Tweet)
unfav: dispatch.Future[repatch.twitter.response.Tweet] = scala.concurrent.impl.Promise$DefaultPromise@7f3e93cb

scala> unfav()
res1: repatch.twitter.response.Tweet = Tweet(333297803155611649
```

streaming
---------

### [POST statuses/filter](https://dev.twitter.com/docs/api/1.1/post/statuses/filter)

> Returns public statuses that match one or more filter predicates.

```scala
scala> http(client(PublicStream.track("scala")) > 
         as.repatch.twitter.response.stream.TweetOrJson {
         case Right(tweet) => println(tweet) 
         case Left(json)   => println(json) 
       })
res0: dispatch.Future[Unit] = scala.concurrent.impl.Promise$DefaultPromise@3f77f4ea

scala> Tweet(333804083792191488,Looking for a Hoogle like to Scala? ....
```

### [GET user](https://dev.twitter.com/docs/api/1.1/get/user)

> Streams messages for a single user, as described in User streams.

```scala
scala> http(client(UserStream()) > as.repatch.twitter.response.stream.TweetOrJson {
         case Right(tweet) => println(tweet) 
         case Left(json)   => println(json) 
       })
res0: dispatch.Future[Unit] = scala.concurrent.impl.Promise$DefaultPromise@451094e7
```

friends & followers
-------------------

### [GET friends/ids](https://dev.twitter.com/docs/api/1.1/get/friends/ids)

> Returns a cursored collection of user IDs for every user the specified user is following.

```scala
scala> val x = http(client(Friend.ids) OK as.json4s.Json)
x: dispatch.Future[org.json4s.JValue] = scala.concurrent.impl.Promise$DefaultPromise@72d53ac7

scala> {
         import repatch.twitter.response.Friend._
         val json = x()
         ids(json)
       }
res0: List[BigInt] = List(812340000, ...
```

### [GET followers/ids](https://dev.twitter.com/docs/api/1.1/get/followers/ids)

> Returns a cursored collection of user IDs for every user following the specified user.

```scala
scala> val x = http(client(Follower.ids) OK as.json4s.Json)
x: dispatch.Future[org.json4s.JValue] = scala.concurrent.impl.Promise$DefaultPromise@7449d884

scala> {
         import repatch.twitter.response.Follower._
         val json = x()
         ids(json)
       }
res0: List[BigInt] = List(1234567, ...
```

### [POST friendships/create](https://dev.twitter.com/docs/api/1.1/post/friendships/create)

> Allows the authenticating users to follow the user specified in the ID parameter.

```scala
scala> val u = http(client(Friendship("twitterapi")) OK as.repatch.twitter.response.User)
u: dispatch.Future[repatch.twitter.response.User] = scala.concurrent.impl.Promise$DefaultPromise@50e3fa1d

scala> u()
res0: repatch.twitter.response.User = User(6253282,twitterapi,...
```

### [POST friendships/destroy](https://dev.twitter.com/docs/api/1.1/post/friendships/destroy)

> Allows the authenticating user to unfollow the user specified in the ID parameter.

```scala
scala> val u = http(client(Friendship.destroy("twitterapi")) OK as.repatch.twitter.response.User)
u: dispatch.Future[repatch.twitter.response.User] = scala.concurrent.impl.Promise$DefaultPromise@487793db

scala> u()
res0: repatch.twitter.response.User = User(6253282,twitterapi,...
```

users
-----

### [GET users/show](https://dev.twitter.com/docs/api/1.1/get/users/show)

> Returns a variety of information about the user specified by the required user_id or screen_name parameter. 

```scala
scala> val u = http(client(User.show("twitterapi")) OK as.repatch.twitter.response.User)
u: dispatch.Future[repatch.twitter.response.User] = scala.concurrent.impl.Promise$DefaultPromise@3aff776

scala> u().status map {_.text}
res0: Option[String] = Some(We have deprecated HTTP 1.0 support for the Streaming API: https://t.co/JfieFem8Kf)
```
