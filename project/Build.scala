import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "carracedashboard"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "com.mongodb.casbah" %% "casbah" % "2.1.5-1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "Sonatype" at "https://oss.sonatype.org/content/repositories/releases/"
    )

}
