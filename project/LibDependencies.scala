import sbt._

object LibDependencies {

  val cryptoCompile = Seq(
    "com.typesafe"     %  "config"         % "1.4.3",
    "javax.inject"     %  "javax.inject"   % "1"
  )

  val cryptoTest = Seq(
    "org.scalatest"        %% "scalatest"       % "3.2.17"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"    % "0.64.8"   % Test,
    "org.scalatestplus"    %% "scalacheck-1-17" % "3.2.17.0" % Test,
    "org.scalatestplus"    %% "mockito-3-4"     % "3.2.10.0" % Test
  )

  val cryptoJsonPlay28Compile = Seq(
    "com.typesafe.play" %% "play-json" % "2.8.1" // version provided by Play 2.8
  )

  val cryptoJsonPlay29Compile = Seq(
    "com.typesafe.play" %% "play-json" % "2.10.2" // version provided by Play 2.9
  )

  val cryptoJsonPlay30Compile = Seq(
    "org.playframework" %% "play-json" % "3.0.2" // version provided by Play 3.0
  )
}
