val scala2_13 = "2.13.16"
val scala3    = "3.3.5"

ThisBuild / majorVersion     := 8
ThisBuild / scalaVersion     := scala2_13
ThisBuild / isPublicArtefact := true
ThisBuild / scalacOptions    ++= Seq("-feature")

lazy val library = Project("library", file("."))
  .settings(publish / skip := true)
  .aggregate(
    crypto,
    cryptoJsonPlay29,
    cryptoJsonPlay30
  )

lazy val crypto = Project("crypto", file("crypto"))
  .settings(
    crossScalaVersions := Seq(scala2_13, scala3),
    libraryDependencies ++= LibDependencies.cryptoCompile ++ LibDependencies.cryptoTest
  )

def shareSources(location: String) = Seq(
  Compile / unmanagedSourceDirectories   += baseDirectory.value / s"../$location/src/main/scala",
  Compile / unmanagedResourceDirectories += baseDirectory.value / s"../$location/src/main/resources",
  Test    / unmanagedSourceDirectories   += baseDirectory.value / s"../$location/src/test/scala",
  Test    / unmanagedResourceDirectories += baseDirectory.value / s"../$location/src/test/resources"
)

lazy val cryptoJsonPlay29 = Project("crypto-json-play-29", file("crypto-json-play-29"))
  .settings(
    crossScalaVersions := Seq(scala2_13),
    shareSources("crypto-json"),
    libraryDependencies ++= LibDependencies.cryptoJsonPlay29Compile ++ LibDependencies.cryptoTest
  ).dependsOn(crypto)

lazy val cryptoJsonPlay30 = Project("crypto-json-play-30", file("crypto-json-play-30"))
  .settings(
    crossScalaVersions := Seq(scala2_13, scala3),
    shareSources("crypto-json"),
    libraryDependencies ++= LibDependencies.cryptoJsonPlay30Compile ++ LibDependencies.cryptoTest
  ).dependsOn(crypto)
