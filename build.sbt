import ReleaseTransformations._

organization := "com.thesamet"

name := "sbt-protoc"

description := "SBT plugin for generating code from Protocol Buffer using protoc"

scalacOptions := Seq("-deprecation", "-unchecked", "-Xlint", "-Yno-adapted-args")

scalacOptions += {
  if (sbtVersion.value.startsWith("0.13")) "-target:jvm-1.7"
  else "-target:jvm-1.8"
}

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.4.0",
  "com.trueaccord.scalapb" %% "protoc-bridge" % "0.2.7"
)

sbtPlugin := true

enablePlugins(ScriptedPlugin)

scriptedBufferLog := false

scriptedLaunchOpts += s"-Dplugin.version=${version.value}"

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

publishMavenStyle := false

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ test"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publish"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
