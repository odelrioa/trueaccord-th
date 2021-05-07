import sbt._
import play.sbt.PlayImport.{ws, guice}

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

  val libraryDependencies = List(
    guice,
    ws,
    scalaTest % Test
  )
}
