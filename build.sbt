import org.scalajs.jsenv.nodejs.NodeJSEnv

ThisBuild / scalaVersion := "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(name := "scala-slack-bot")
  .aggregate(lambda.js, lambda.jvm, `service`.js, `service`.jvm)

lazy val lambda = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("lambda"))
  .enablePlugins(UniversalPlugin)
  .settings(
    name              := "lambda",
    libraryDependencies ++= Seq(
      "org.typelevel"                 %%% "cats-effect" % "3.5.1",
      "com.softwaremill.sttp.client4" %%% "core"        % "4.0.0-M2",
      "com.outr"                      %%% "scribe"      % "3.11.8",
      "com.lihaoyi"                   %%% "upickle"     % "3.1.2",
    ),
    topLevelDirectory := None,// required for AWS to accept the zip
  )
  .jsSettings(
    webpackConfigFile := Some(baseDirectory.value / ".." / ".." / "webpack.config.js"),
    Universal / mappings ++= (Compile / fullOptJS / webpack).value.map { f =>
      f.data -> f.data.getName.replace("-opt-bundle", "") // remove the bundler suffix from the file names
    },
    libraryDependencies ++= Seq(
      "net.exoego" %%% "aws-lambda-scalajs-facade" % "0.12.1", // type definitions for aws lambda handlers
    ),
  )
  .jsConfigure(_.enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin))
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core"   % "1.2.2",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.2",
    )
  )
  .jvmConfigure(_.enablePlugins(JavaAppPackaging))

lazy val `service` = crossProject(JSPlatform, JVMPlatform)
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
        .withSourceMap(true) // debugger will be able to navigate to scalajs code
        .withArgs(List("--inspect")),// enables remote debugger
    ),
  )
  .jvmSettings(
    Revolver.enableDebugging(),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime,
    ),
    dockerBaseImage := "openjdk:17",
  )
  .settings(
    Docker / packageName := "scala-slack-bot",
    dockerAliases        := Seq(
      DockerAlias(Some("registry.fly.io"), None, "scala-slack-bot", Some("latest")),
    ),
    dockerBuildOptions ++= Seq("--platform", "linux/amd64"),
  )
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
