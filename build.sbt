val scala2_12 = "2.12.16"
val scala2_13 = "2.13.8"

val silencerVersion = "1.7.9"

lazy val commonSettings = Seq(
  organization := "uk.gov.hmrc",
  majorVersion := 7,
  scalaVersion := scala2_12,
  crossScalaVersions := Seq(scala2_12, scala2_13),
  isPublicArtefact := true,
  scalacOptions ++= Seq("-feature"),
  libraryDependencies ++= Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )
)

lazy val library = Project("library", file("."))
  .settings(
    commonSettings,
    publish / skip := true,
  )
  .aggregate(
    crypto,
    cryptoJsonPlay28
  )

lazy val crypto = Project("crypto", file("crypto"))
  .settings(
    commonSettings,
    libraryDependencies ++= LibDependencies.cryptoCompile ++ LibDependencies.cryptoTest
  )

lazy val cryptoJsonPlay28 = Project("crypto-json-play-28", file("crypto-json"))
  .settings(
    commonSettings,
    libraryDependencies ++= LibDependencies.cryptoJsonPlay28Compile ++ LibDependencies.cryptoTest
  ).dependsOn(crypto)
