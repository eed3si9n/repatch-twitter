import sbt._
import com.typesafe.sbt.SbtGhPages._
import com.typesafe.sbt.SbtGit.{GitKeys => git}
import com.typesafe.sbt.SbtSite._
import sbtunidoc.Plugin._

object Builds extends Build {
  import Keys._
  lazy val dispatchVersion = SettingKey[String]("x-dispatch-version")

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    dispatchVersion := "0.10.0",
    version <<= dispatchVersion { dv => "dispatch" + dv + "_0.1.0-SNAPSHOT" },
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
    resolvers += "sonatype-public" at "https://oss.sonatype.org/content/repositories/public",
    licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT")),
    description := """repatch-twitter is a Dispatch plugin for Twitter API.""",
    pomExtra := (<scm>
        <url>git@github.com:eed3si9n/repatch-twitter.git</url>
        <connection>scm:git:git@github.com:eed3si9n/repatch-twitter.git</connection>
      </scm>
      <developers>
        <developer>
          <id>eed3si9n</id>
          <name>Eugene Yokota</name>
          <url>http://eed3si9n.com</url>
        </developer>
      </developers>),
    publishArtifact in Test := false,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots") 
      else Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    pomIncludeRepository := { x => false },
    parallelExecution in Test := false,
    crossVersion <<= scalaVersion { sv =>
      ("-(M|RC)".r findFirstIn sv) map {_ => CrossVersion.full} getOrElse CrossVersion.binary
    }
  )
  lazy val rootSettings = buildSettings ++ unidocSettings ++ 
      site.settings ++ ghpages.settings ++ Seq(
    name := "repatch-twitter",
    git.gitRemoteRepo := "git@github.com:eed3si9n/repatch-twitter.git",
    site.addMappingsToSiteDir(mappings in packageDoc in ScalaUnidoc, "latest/api")
  )
  lazy val coreSettings = buildSettings ++ Seq(
    name := "repatch-twitter-core",
    initialCommands in console := """import dispatch._, Defaults._
                                    |import repatch.twitter.request._
                                    |val prop = new java.io.File(System.getProperty("user.home"), ".foo.properties")
                                    |val client = PropertiesClient(prop)
                                    |val http = new Http""".stripMargin
  )

  lazy val root = Project("root", file("."), settings = rootSettings) aggregate(core)
  lazy val core = Project("core", file("core"), settings = coreSettings)
}
