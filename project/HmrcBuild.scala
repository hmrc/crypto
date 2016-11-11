import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

  import uk.gov.hmrc.DefaultBuildSettings._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning

  val appDependencies = {

    import Dependencies._

    Seq(
      Compile.secure,
      Compile.play,

      Test.scalaTest,
      Test.pegdown,
      Test.playTest,
      Test.scalaCheck
    )
  }

  lazy val crypto = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      scalaVersion := "2.11.7",
      libraryDependencies ++= appDependencies,
      crossScalaVersions := Seq("2.11.7"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      )
    )

}

object Dependencies {

  import play.core.PlayVersion

  object Compile {
    val secure = "uk.gov.hmrc" %% "secure" % "7.1.0"
    val play = "com.typesafe.play" %% "play" % PlayVersion.current % "provided"
  }

  sealed abstract class Test(scope: String) {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % scope
    val playTest = "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
    val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.12.2" % scope
  }

  object Test extends Test("test")
}



