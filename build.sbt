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
    jsonEncryptionPlay28,
    jsonEncryption
  )

lazy val crypto = Project("crypto", file("crypto"))
  .settings(
    commonSettings,
    libraryDependencies ++= LibDependencies.cryptoCompile ++ LibDependencies.cryptoTest
  )

// TODO rename as crypto-json?
lazy val jsonEncryptionPlay28 = Project("json-encryption-play-28", file("json-encryption"))
  .settings(
    commonSettings,
    libraryDependencies ++= LibDependencies.jsonEncryptionPlay28Compile ++ LibDependencies.cryptoTest
  ).dependsOn(crypto, jsonEncryption)


// empty artefact, exists to ensure eviction of previous json-encryption jar which has now moved into json-encryption-play-xx
lazy val jsonEncryption = Project("json-encryption", file("json-encryption-empty"))
  .settings(
    commonSettings
  )
