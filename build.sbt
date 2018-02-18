import org.typelevel.Dependencies

name := "applicative-syntax"
organization := "com.monovore"

scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
  "org.scalatest"  %%% "scalatest"  % "3.0.0" % "test",
  "org.typelevel" %% "cats-core" % "1.0.1" % "test"
)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-Xlint",
  "-feature",
  "-language:higherKinds",
  "-Ypartial-unification",
//  "-Xprint:packageobjects",
  "-deprecation",
  "-unchecked"
)

List(Compile, Test) flatMap { config =>
  Seq(
    // Notice this is :=, not += - all the warning/lint options are simply
    // impediments in the repl.
    scalacOptions in console in config := Seq(
      "-language:_",
      "-Xplugin:" + (packageBin in Compile).value
    )
  )
}

scalacOptions in Test ++= {
  val jar = (packageBin in Compile).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}