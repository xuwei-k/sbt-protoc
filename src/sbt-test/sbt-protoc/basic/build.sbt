val protobufVersion = "3.3.1"

libraryDependencies += "com.google.protobuf" % "protobuf-java" % protobufVersion % "protobuf"

scalaVersion := "2.10.6"

crossScalaVersions += "2.11.8"

excludeFilter in PB.generate := "test1.proto"

unmanagedResourceDirectories in Compile ++= (PB.protoSources in Compile).value

TaskKey[Unit]("checkJar") := {
  val jar = (packageBin in Compile).value
  IO.withTemporaryDirectory{ dir =>
    val files = IO.unzip(jar, dir, "*.proto")
    val expect = Set("test1.proto", "test2.proto").map(dir / _)
    assert(files == expect, s"$files $expect")
  }
}

// https://github.com/sbt/sbt-protobuf/issues/37
mainClass in compile := Some("whatever")

PB.targets in Compile := Seq(PB.gens.java(protobufVersion) -> (sourceManaged in Compile).value)
