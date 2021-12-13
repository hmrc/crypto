
val scala2_12 = "2.12.15"
val scala2_13 = "2.13.7"

lazy val library = Project("crypto", file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    scalaVersion := scala2_12,
    crossScalaVersions := Seq(scala2_12, scala2_13),
    majorVersion := 6,
    isPublicArtefact := true,
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test,
    resolvers += Resolver.typesafeRepo("releases")
  )
