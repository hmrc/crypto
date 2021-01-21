val name = "crypto"

lazy val crypto = Project(name, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 6,
    makePublicallyAvailableOnBintray := true,
    scalaVersion := "2.12.12",
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.typesafeRepo("releases")
    )
  )
