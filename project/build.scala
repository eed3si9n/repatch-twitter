import sbt._

object Builds extends Build {
  import Keys._
  lazy val dispatchVersion = SettingKey[String]("x-dispatch-version")

  lazy val root = Project("repatch-twitter", file(".")) settings(
    scalaVersion := "2.10.1",
    dispatchVersion := "0.10.0",
    organization := "com.eed3si9n",
    libraryDependencies <++= (dispatchVersion) { (dv) => Seq(
      "net.databinder.dispatch" %% "dispatch-core" % dv,
      "net.databinder.dispatch" %% "dispatch-json4s-native" % dv
    )},
    libraryDependencies <+= (scalaVersion) {
      case "2.9.3" =>  "org.specs2" %% "specs2" % "1.12.4.1" % "test"
      case _ => "org.specs2" %% "specs2" % "1.15-SNAPSHOT" % "test"
    },
    crossScalaVersions := Seq("2.10.1", "2.9.3"),
    resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public"
  )
}
