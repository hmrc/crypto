import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

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
      Test.scalaCheck,
      Test.mockito
    )
  }

  lazy val crypto = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      libraryDependencies ++= appDependencies,
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      )
    )

}

object Dependencies {

  import play.core.PlayVersion

  object Compile {
    val secure = "uk.gov.hmrc"       %% "secure" % "7.2.0"
    val play   = "com.typesafe.play" %% "play"   % PlayVersion.current % "provided"
  }

  sealed abstract class Test(scope: String) {
    val scalaTest  = "org.scalatest"     %% "scalatest"   % "3.0.1"             % scope
    val pegdown    = "org.pegdown"       % "pegdown"      % "1.6.0"             % scope
    val playTest   = "com.typesafe.play" %% "play-test"   % PlayVersion.current % scope
    val scalaCheck = "org.scalacheck"    %% "scalacheck"  % "1.13.5"            % scope
    val mockito    = "org.mockito"       % "mockito-core" % "2.10.0"            % scope
  }

  object Test extends Test("test")
}
