import sbt._

object LibDependencies {

  val cryptoCompile = Seq(
    "com.typesafe"     %  "config"         % "1.4.1",
    "javax.inject"     %  "javax.inject"   % "1"
  )

  val cryptoTest = Seq(
    "org.scalatest"        %% "scalatest"     % "3.2.3"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"  % "0.36.8"  % Test,
    "org.scalacheck"       %% "scalacheck"    % "1.15.2"  % Test,
    "org.mockito"          %% "mockito-scala" % "1.16.15" % Test
  )

  val cryptoJsonPlay28Compile = Seq(
    "com.typesafe.play" %% "play-json" % "2.8.1"
  )
}
