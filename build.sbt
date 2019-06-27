name := "applicative-syntax"
organization := "com.monovore"

scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0")

libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
  "org.scalatest"  %%% "scalatest"  % "3.0.8" % "test",
  "org.typelevel" %% "cats-core" % "2.0.0-M4" % "test"
)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-Xlint",
  "-feature",
  "-language:higherKinds",
//  "-Xprint:packageobjects",
  "-deprecation",
  "-unchecked"
)

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 =>
      Seq("-Ypartial-unification")
    case _ =>
      Nil
  }
}

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