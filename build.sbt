import com.typesafe.sbt.SbtGit.{GitKeys => git}

def baseVersion = "0.1.0"
def dispatchVersion = "0.11.0"
def specsVersion = "2.3.11"

lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  version := s"dispatch${dispatchVersion}_${baseVersion}",
  organization := "com.eed3si9n",
  scalaVersion := "2.11.0",
  libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % dispatchVersion,
    "net.databinder.dispatch" %% "dispatch-json4s-native" % dispatchVersion
  ),
  libraryDependencies <+= (scalaVersion) {
    case "2.9.3" =>  "org.specs2" %% "specs2" % "1.12.4.1" % "test"
    case _ => "org.specs2" %% "specs2" % specsVersion % "test"
  },
  crossScalaVersions := Seq("2.11.0", "2.10.4"),
  resolvers += Resolver.sonatypeRepo("public"),
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT")),
  description := """repatch-twitter is a plugin for Dispatch to use Twitter API.""",
  homepage := Some(url("https://github.com/eed3si9n/repatch-twitter")),
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
  publishMavenStyle := true,
  pomIncludeRepository := { x => false },
  parallelExecution in Test := false,
  crossVersion <<= scalaVersion { sv =>
    ("-(M|RC)".r findFirstIn sv) map {_ => CrossVersion.full} getOrElse CrossVersion.binary
  }
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(unidocSettings: _*).
  settings(site.settings: _*).
  settings(ghpages.settings: _*).
  settings(
    name := "repatch-twitter",
    publishArtifact := false,
    git.gitRemoteRepo := "git@github.com:eed3si9n/repatch-twitter.git",
    site.addMappingsToSiteDir(mappings in packageDoc in ScalaUnidoc, "latest/api")
  )

lazy val core = (project in file("core")).
  settings(commonSettings: _*).
  settings(
    name := "repatch-twitter-core",
    initialCommands in console := """import dispatch._, Defaults._
                                    |import repatch.twitter.request._
                                    |val prop = new java.io.File(System.getProperty("user.home"), ".foo.properties")
                                    |val client = PropertiesClient(prop)
                                    |val http = new Http""".stripMargin
  )
