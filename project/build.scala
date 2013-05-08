import sbt._

object Builds extends Build {
  import Keys._
  lazy val dispatchVersion = SettingKey[String]("x-dispatch-version")

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    dispatchVersion := "0.10.0",
    version <<= dispatchVersion { dv => dv + "_0.1.0-SNAPSHOT" },
    organization := "com.eed3si9n",
    scalaVersion := "2.10.1",
    libraryDependencies <++= (dispatchVersion) { (dv) => Seq(
      "net.databinder.dispatch" %% "dispatch-core" % dv,
      "net.databinder.dispatch" %% "dispatch-json4s-native" % dv
    )},
    libraryDependencies <+= (scalaVersion) {
      case "2.9.3" =>  "org.specs2" %% "specs2" % "1.12.4.1" % "test"
      case _ => "org.specs2" %% "specs2" % "1.15-SNAPSHOT" % "test"
    },
    crossScalaVersions := Seq("2.10.1"),
    resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public"
  )
  lazy val coreSettings = buildSettings ++ Seq(
    name := "repatch-twitter-core"
  )

  lazy val root = Project("root", file("."),
    settings = buildSettings ++ Seq(name := "repatch-twitter")) aggregate(core)
  lazy val core = Project("core", file("core"), settings = coreSettings)
}
