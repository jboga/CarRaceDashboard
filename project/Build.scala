import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "CarRaceDashboard"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.mongodb" %% "casbah" % "2.4.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
