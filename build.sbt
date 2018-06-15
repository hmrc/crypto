val name = "crypto"

lazy val crypto = Project(name, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.typesafeRepo("releases")
    )
  )
