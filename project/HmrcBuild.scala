import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning

  val appDependencies = {

    import Dependencies._

    Seq(
      Compile.secure,
      "com.typesafe" % "config"       % "1.3.3",
      "javax.inject" % "javax.inject" % "1",
      Test.scalaTest,
      Test.pegdown,
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

  object Compile {
    val secure = "uk.gov.hmrc" %% "secure" % "7.2.0"
  }

  sealed abstract class Test(scope: String) {
    val scalaTest  = "org.scalatest"  %% "scalatest"   % "3.0.5"  % scope
    val pegdown    = "org.pegdown"    % "pegdown"      % "1.6.0"  % scope
    val scalaCheck = "org.scalacheck" %% "scalacheck"  % "1.13.5" % scope
    val mockito    = "org.mockito"    % "mockito-core" % "2.10.0" % scope
  }

  object Test extends Test("test")
}
