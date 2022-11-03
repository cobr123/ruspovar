ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(ruspovar_scala)
  .settings(
    name := "ruspovar_scala_js",
    // This is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.1.0",
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.7.4" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )

lazy val ruspovar_scala = (project in file("./ruspovar_scala"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "ruspovar_scala",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"
  )
