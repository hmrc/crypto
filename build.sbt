
val scala2_12 = "2.12.16"
val scala2_13 = "2.13.8"

lazy val library = Project("crypto", file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    scalaVersion := scala2_12,
    crossScalaVersions := Seq(scala2_12, scala2_13),
    majorVersion := 7,
    isPublicArtefact := true,
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test,
    resolvers += Resolver.typesafeRepo("releases")
  )
