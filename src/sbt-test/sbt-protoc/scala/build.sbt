import com.trueaccord.scalapb.compiler.Version.protobufVersion

scalaVersion := "2.10.6"

crossScalaVersions += "2.11.8"

PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)

libraryDependencies += "com.google.protobuf" % "protobuf-java" % protobufVersion % "protobuf"

mainClass in compile := Some("whatever")
