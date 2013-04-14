import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "gcalexchsync"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
  //  "com.google.oauth-client" % "google-oauth-client" % "1.14.1-beta",
    "com.google.oauth-client" % "google-oauth-client-java6" % "1.14.1-beta",
    "com.google.oauth-client" % "google-oauth-client-jetty" % "1.14.1-beta",
    "com.google.http-client" % "google-http-client-jackson2" % "1.14.1-beta",
    "com.google.api.client" % "google-api-client" % "1.4.1-beta",
    "com.google.apis" % "google-api-services-calendar" % "v3-rev37-1.14.1-beta"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
