import org.scalajs.jsenv.nodejs.NodeJSEnv

ThisBuild / scalaVersion := "3.3.0"

val lambda = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("lambda"))
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "lambda",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-explaintypes", // Explain type errors in more detail.
      "-feature",      // Warn when we use advanced language features
      "-unchecked",    // Give more information on type erasure warning
    ),
    libraryDependencies ++= Seq(
      "org.typelevel"                 %%% "cats-effect" % "3.5.1",
      "com.softwaremill.sttp.client4" %%% "core" % "4.0.0-M2",
      "com.outr"                      %%% "scribe"      % "3.11.8",
      "com.lihaoyi"                   %%% "upickle"     % "3.1.2",
    ),
  )
  .jsSettings(
    webpackConfigFile := Some(baseDirectory.value / ".." / ".." / "webpack.config.js"),
    // Package lambda as a zip. Use `universal:packageBin` to create the zip
    topLevelDirectory := None,
    Universal / mappings ++= (Compile / fullOptJS / webpack).value.map { f =>
      // remove the bundler suffix from the file names
      f.data -> f.data.getName.replace("-opt-bundle", "")
    },
    libraryDependencies ++= Seq(
      // Include type definition for aws lambda handlers
      "net.exoego" %%% "aws-lambda-scalajs-facade" % "0.12.1",
    ),
  )
  .jsConfigure(_.enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin))

val `local-run` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(lambda)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % "0.23.22",
      "org.http4s" %%% "http4s-dsl"          % "0.23.22",
    ),
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    jsEnv                           := new NodeJSEnv(
      NodeJSEnv
        .Config()
        .withSourceMap(true)         // debugger will be able to navigate to scalajs code
        .withArgs(List("--inspect")),// enables remote debugged
    ),
  )
  .jvmSettings(
    Revolver.enableDebugging(),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime,
    ),
  )
