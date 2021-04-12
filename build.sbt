name := "glomma"

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / useSuperShell := false

// ScalaFix configuration
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

val catsVersion = "2.4.2"
val catsEffectVersion = "3.0.0"
val circeVersion = "0.13.0"
val http4sVersion = "1.0.0-M6"
val logbackVersion = "1.2.3"
val munitVersion = "0.7.22"

val build = taskKey[Unit]("Format, compile, and test")

val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"     % catsVersion,
    "org.typelevel" %% "cats-free"     % catsVersion,
    "org.typelevel" %% "cats-effect"   % catsEffectVersion,
    "io.circe"      %% "circe-core"    % circeVersion,
    "io.circe"      %% "circe-generic" % circeVersion,
    "io.circe"      %% "circe-parser"  % circeVersion,
    "org.scalameta" %% "munit"         % munitVersion % Test
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-Yrangepos",
    "-Ymacro-annotations",
    "-Wunused:imports",
    "-Werror"
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  addCompilerPlugin(scalafixSemanticdb)
)

lazy val data = project
  .in(file("data"))
  .settings(
    sharedSettings,
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )
