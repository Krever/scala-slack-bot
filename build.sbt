import org.scalajs.jsenv.nodejs.NodeJSEnv

ThisBuild / scalaVersion := "3.3.0"

val root = (project in file("."))
  .settings(
    name              := "lambda",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-explaintypes", // Explain type errors in more detail.
      "-feature",      // Warn when we use advanced language features
      "-unchecked",    // Give more information on type erasure warning
    ),
    webpackConfigFile := Some(baseDirectory.value / "webpack.config.js"),
    libraryDependencies ++= Seq(
      // Include type definition for aws lambda handlers
      "net.exoego"   %%% "aws-lambda-scalajs-facade" % "0.12.1",
      "org.typelevel" %% "cats-effect"               % "3.5.1",
    ),
    // Package lambda as a zip. Use `universal:packageBin` to create the zip
    topLevelDirectory := None,
    Universal / mappings ++= (Compile / fullOptJS / webpack).value.map { f =>
      // remove the bundler suffix from the file names
      f.data -> f.data.getName.replace("-opt-bundle", "")
    },
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, UniversalPlugin)

val `local-run` = project
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % "0.23.22",
      "org.http4s" %%% "http4s-dsl"          % "0.23.22",
    ),
    scalaJSUseMainModuleInitializer := true,
    // ECMAScript
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    // CommonJS
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    jsEnv := new NodeJSEnv(NodeJSEnv.Config().withSourceMap(true).withArgs(List("--inspect")))
  )
  .dependsOn(root)
  .enablePlugins(ScalaJSPlugin)
