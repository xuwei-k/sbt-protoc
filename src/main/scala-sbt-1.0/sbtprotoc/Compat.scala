package sbtprotoc

import sbt.librarymanagement.{CrossVersion, ModuleID}
import sbt.util.CacheImplicits
import sjsonnew.JsonFormat
import java.io.File
import java.net.URLClassLoader

private[sbtprotoc] trait Compat extends CacheImplicits { self: ProtocPlugin.type =>
  private val CrossDisabled = sbt.librarymanagement.Disabled()
  protected def makeArtifact(f: protocbridge.Artifact): ModuleID = {
    ModuleID(f.groupId, f.artifactId, f.version)
      .cross(if (f.crossVersion) CrossVersion.binary else CrossDisabled)
  }

  protected object CacheArguments {
    implicit val instance: JsonFormat[Arguments] =
      caseClassArray(Arguments.apply _, Arguments.unapply _)
  }

  def jarFiles(classes: Seq[Class[_]]): Seq[File] = {
    import sbt.librarymanagement._, syntax._

    val log = sbt.util.LogExchange.logger("sbt-protoc")

    val lm = {
      import sbt.librarymanagement.ivy._
      val ivyConfig = InlineIvyConfiguration().withLog(log)
      IvyDependencyResolution(ivyConfig)
    }

    classes.map{
      clazz => new File(clazz.getProtectionDomain.getCodeSource.getLocation.getPath)
    } ++ Vector(
      "com.github.os72" % "protoc-jar" % "3.4.0",
      "com.trueaccord.scalapb" % "protoc-bridge_2.12" % "0.2.7",
      "com.trueaccord.scalapb" % "compilerplugin_2.12" % "0.6.2"
    ).flatMap { module =>
      lm.retrieve(
        dependencyId = module,
        scalaModuleInfo = None,
        retrieveDirectory = new File("target"),
        log = log
      ) match {
        case Right(jars) =>
          jars
        case Left(unresolved) =>
          throw unresolved.resolveException
      }
    }
  }

  def forkRun(args: Array[String], classes: Seq[Class[_]]) = {
    val jars = jarFiles(classes)
    println(jars)
    val jarsToClassload = sbt.io.Path.toURLs(jars)
    val cachedLoader = new URLClassLoader(jarsToClassload, rootLoader)
    val clazz = Class.forName("com.github.os72.protocjar.Protoc", false, cachedLoader)
    val method = clazz.getMethod("runProtoc", classOf[Array[String]])
    method.invoke(null, args).asInstanceOf[Int]
  }

  lazy val rootLoader: ClassLoader = {
    @annotation.tailrec
    def parent(loader: ClassLoader): ClassLoader = {
      val p = loader.getParent
      if (p eq null) loader else parent(p)
    }
    val systemLoader = ClassLoader.getSystemClassLoader
    if (systemLoader ne null) parent(systemLoader)
    else parent(getClass.getClassLoader)
  }

}
