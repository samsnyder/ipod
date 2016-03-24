lazy val root = (project in file(".")).
  settings(
    name := "MyPod",
    version := "1.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-pickling" % "0.10.1"
    // scalacOptions += "-Xlog-implicits"
  )


