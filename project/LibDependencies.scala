import sbt._

object LibDependencies {

  val cryptoCompile = Seq(
    "com.typesafe"     %  "config"         % "1.4.5",
    "javax.inject"     %  "javax.inject"   % "1"
  )

  val cryptoTest = Seq(
    "org.scalatest"        %% "scalatest"       % "3.2.19"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"    % "0.64.8"   % Test,
    "org.scalatestplus"    %% "scalacheck-1-17" % "3.2.18.0" % Test,
    "org.scalatestplus"    %% "mockito-4-11"    % "3.2.18.0" % Test
  )

  val cryptoJsonPlay30Compile = Seq(
    "org.playframework" %% "play-json" % "3.0.5" // version provided by Play 3.0
  )
}
