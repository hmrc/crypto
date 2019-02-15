val name = "crypto"

lazy val crypto = Project(name, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 5,
    makePublicallyAvailableOnBintray := true,
    crossScalaVersions := List("2.11.12", "2.12.8"),
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.typesafeRepo("releases")
    )
  )
