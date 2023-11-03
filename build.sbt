val scala2_12 = "2.12.18"
val scala2_13 = "2.13.12"

ThisBuild / majorVersion     := 7
ThisBuild / scalaVersion     := scala2_13
ThisBuild / isPublicArtefact := true
ThisBuild / scalacOptions    ++= Seq("-feature")

lazy val library = Project("library", file("."))
  .settings(publish / skip := true)
  .aggregate(
    crypto,
    cryptoJsonPlay28,
    cryptoJsonPlay29
  )

lazy val crypto = Project("crypto", file("crypto"))
  .settings(
    crossScalaVersions := Seq(scala2_12, scala2_13),
    libraryDependencies ++= LibDependencies.cryptoCompile ++ LibDependencies.cryptoTest
  )

def shareSources(location: String) = Seq(
  Compile / unmanagedSourceDirectories   += baseDirectory.value / s"../$location/src/main/scala",
  Compile / unmanagedResourceDirectories += baseDirectory.value / s"../$location/src/main/resources",
  Test    / unmanagedSourceDirectories   += baseDirectory.value / s"../$location/src/test/scala",
  Test    / unmanagedResourceDirectories += baseDirectory.value / s"../$location/src/test/resources"
)

lazy val cryptoJsonPlay28 = Project("crypto-json-play-28", file("crypto-json-play-28"))
  .settings(
    crossScalaVersions := Seq(scala2_12, scala2_13),
    shareSources("crypto-json"),
    libraryDependencies ++= LibDependencies.cryptoJsonPlay28Compile ++ LibDependencies.cryptoTest
  ).dependsOn(crypto)

lazy val cryptoJsonPlay29 = Project("crypto-json-play-29", file("crypto-json-play-29"))
  .settings(
    crossScalaVersions := Seq(scala2_13),
    shareSources("crypto-json"),
    libraryDependencies ++= LibDependencies.cryptoJsonPlay29Compile ++ LibDependencies.cryptoTest
  ).dependsOn(crypto)
