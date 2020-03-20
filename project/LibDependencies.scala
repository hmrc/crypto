import sbt._

object LibDependencies {

  val compile = Seq(
    "uk.gov.hmrc"  %% "secure"      % "7.9.0",
    "com.typesafe" % "config"       % "1.3.3",
    "javax.inject" % "javax.inject" % "1"
  )

  val test = Seq(
    "org.scalatest"  %% "scalatest"   % "3.0.5"  % Test,
    "org.pegdown"    % "pegdown"      % "1.6.0"  % Test,
    "org.scalacheck" %% "scalacheck"  % "1.13.5" % Test,
    "org.mockito"    % "mockito-core" % "2.10.0" % Test
  )
}
