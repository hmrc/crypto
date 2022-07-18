import sbt._

object LibDependencies {

  val compile = Seq(
    "org.bouncycastle" %  "bcprov-jdk15on" % "1.68",
    "com.typesafe"     %  "config"         % "1.4.1",
    "javax.inject"     %  "javax.inject"   % "1"
  )

  val test = Seq(
    "org.scalatest"        %% "scalatest"     % "3.2.3"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"  % "0.36.8"  % Test,
    "org.scalacheck"       %% "scalacheck"    % "1.15.2"  % Test,
    "org.mockito"          %% "mockito-scala" % "1.16.15" % Test
  )
}
