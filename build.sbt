name := "glomma"

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / useSuperShell := false

// ScalaFix configuration
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

val breezeVersion = "1.1"
val catsVersion = "2.4.2"
val catsEffectVersion = "2.2.0"
val circeVersion = "0.13.0"
val finatraVersion = "21.3.0"
val http4sVersion = "1.0.0-M6"
val logbackVersion = "1.2.3"
val munitVersion = "0.7.22"

val build = taskKey[Unit]("Format, compile, and test")

val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalanlp"  %% "breeze"        % breezeVersion,
    "org.typelevel" %% "cats-core"     % catsVersion,
    "org.typelevel" %% "cats-free"     % catsVersion,
    "org.typelevel" %% "cats-effect"   % catsEffectVersion,
    "io.circe"      %% "circe-core"    % circeVersion,
    "io.circe"      %% "circe-generic" % circeVersion,
    "io.circe"      %% "circe-parser"  % circeVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "org.scalameta" %% "munit"         % munitVersion % Test
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-Yrangepos",
    "-Ymacro-annotations",
    "-Wunused:imports"
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  addCompilerPlugin(scalafixSemanticdb)
)

lazy val data = project
  .in(file("data"))
  .settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ),
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )
  .dependsOn(event)

lazy val event = project
  .in(file("event"))
  .settings(
    sharedSettings,
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )

lazy val ingest = project
  .in(file("ingest"))
  .settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "com.twitter" %% "finatra-http-server" % finatraVersion
    ),
    build := { Def.sequential(scalafixAll.toTask(""), scalafmtAll, Test / test).value }
  )
  .dependsOn(event)
