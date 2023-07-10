enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, UniversalPlugin)

name := "lambda"

scalaVersion := "3.3.0"
scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Warn when we use advanced language features
    "-unchecked", // Give more information on type erasure warning
) 
//version in webpack := "4.16.1"
//useYarn := true
webpackConfigFile := Some(baseDirectory.value / "webpack.config.js")
//version in startWebpackDevServer := "3.1.4"

// Optional: Disable source maps to speed up compile times
scalaJSLinkerConfig ~= { _.withSourceMap(false) }

// Incluce type defintion for aws lambda handlers
libraryDependencies += "net.exoego" %%% "aws-lambda-scalajs-facade" % "0.12.1"

// Optional: Include the AWS SDK as a dep
/*
val awsSdkVersion = "2.596.0"
val awsSdkScalajsFacadeVersion = s"0.28.0-v${awsSdkVersion}"
libraryDependencies += "net.exoego" %%% "aws-sdk-scalajs-facade-dynamodb" % awsSdkScalajsFacadeVersion
libraryDependencies += "net.exoego" %%% "aws-sdk-scalajs-facade-s3" % awsSdkScalajsFacadeVersion
npmDependencies in Compile += "aws-sdk" -> awsSdkVersion
*/
 
// Optional: Include some nodejs types (useful for, say, accessing the env)
//libraryDependencies += "net.exoego" %%% "scala-js-nodejs-v12" % "0.9.1"

// Include scalatest
//libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"

// Package lambda as a zip. Use `universal:packageBin` to create the zip
topLevelDirectory := None
mappings in Universal ++= (webpack in (Compile, fullOptJS)).value.map { f =>
  // remove the bundler suffix from the file names
  f.data -> f.data.getName().replace("-opt-bundle", "")
}
