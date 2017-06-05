scalaVersion := "2.10.6"

PB.targets in Compile := Seq(PB.gens.java -> (sourceManaged in Compile).value)
