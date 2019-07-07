organization := "im.mange"

version := "0.0.1-SNAPSHOT"

lazy val baseSettings: Seq[Setting[_]] = Seq(
  scalaVersion := "2.12.3",
  scalacOptions     ++= Seq(
    "-Ypartial-unification",
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions", "-language:existentials",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    //"-Ywarn-numeric-widen",
    //"-Ywarn-value-discard",
    "-Xfuture"
  ),
  addCompilerPlugin("org.spire-math"  % "kind-projector" % "0.9.3" cross CrossVersion.binary),
  resolvers += Resolver.sonatypeRepo("releases")
)

lazy val sewsCounterExample = project.in(file("."))
  .settings(moduleName := "sews-counter-example")
  .settings(baseSettings: _*)

libraryDependencies ++= Seq(
  "im.mange" %% "little" % "0.0.60", //TODO: inline what's needed in sews-db
  "im.mange" %% "sews" % "0.0.27",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

val genElmCodec = taskKey[File]("generate elm codec for common types")

(genElmCodec) := {
  val codec = (baseDirectory).value / "src" / "main" / "elm" / "Codec.elm"
  (runner in (run)).value.run("app.Types", Attributed.data((fullClasspath in Compile).value), Seq(codec.toString), streams.value.log)
  codec
}

//genElmCodec := genElmCodec.triggeredBy(compile in Compile).value
