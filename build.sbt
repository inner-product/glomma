name := "glomma"

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / useSuperShell := false

// ScalaFix configuration
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

val breezeVersion = "1.1"
val catsVersion = "2.6.1"
val catsEffectVersion = "3.1.1"
val circeVersion = "0.14.0"
val fs2Version = "3.0.0"
val http4sVersion = "0.23.0-RC1"
val logbackVersion = "1.2.3"
val munitVersion = "0.7.22"

val build = taskKey[Unit]("Format, compile, and test")

val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalanlp"  %% "breeze"              % breezeVersion,
    "org.typelevel" %% "cats-free"           % catsVersion,
    "org.typelevel" %% "cats-effect"         % catsEffectVersion,
    "io.circe"      %% "circe-core"          % circeVersion,
    "io.circe"      %% "circe-generic"       % circeVersion,
    "io.circe"      %% "circe-parser"        % circeVersion,
    "co.fs2"        %% "fs2-core"            % fs2Version,
    "ch.qos.logback" % "logback-classic"     % logbackVersion,
    "org.http4s"    %% "http4s-dsl"          % http4sVersion,
    "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
    "org.http4s"    %% "http4s-blaze-client" % http4sVersion,
    "org.http4s"    %% "http4s-circe"        % http4sVersion,
    "org.scalameta" %% "munit"               % munitVersion % Test
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-Yrangepos",
    "-Ymacro-annotations",
    "-Wunused:imports"
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  addCompilerPlugin(scalafixSemanticdb),
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

lazy val event = project
  .in(file("event"))
  .settings(
    sharedSettings,
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )

lazy val data = project
  .in(file("data"))
  .settings(
    sharedSettings,
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )
  .dependsOn(event)

lazy val ingest = project
  .in(file("ingest"))
  .settings(
    sharedSettings,
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )
  .dependsOn(event, data % Test)
