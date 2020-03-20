import sbt._

object LibDependencies {

  val compile = Seq(
    "uk.gov.hmrc"  %% "secure"       % "7.11.0",
    "com.typesafe" %  "config"       % "1.4.0",
    "javax.inject" %  "javax.inject" % "1"
  )

  val test = Seq(
    "org.scalatest"        %% "scalatest"     % "3.1.0"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"  % "0.35.10" % Test,
    "org.scalacheck"       %% "scalacheck"    % "1.14.3"  % Test,
    "org.mockito"          %% "mockito-scala" % "1.10.1"  % Test
  )
}
