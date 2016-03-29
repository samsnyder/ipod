lazy val root = (project in file(".")).
  settings(
    name := "MyPod",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies += "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
    libraryDependencies += "com.mpatric" % "mp3agic" % "0.8.4",
    libraryDependencies += "org.imgscalr" % "imgscalr-lib" % "4.2",
    // libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4",
    libraryDependencies += "commons-cli" % "commons-cli" % "1.2",
    libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2",
    // libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2",
    libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0",
    // libraryDependencies += "me.tongfei" % "progressbar" % "0.4.0",
    // libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2",
    // libraryDependencies += "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2",
    scalacOptions ++= Seq("-Xmax-classfile-name", "254")
    // ,scalacOptions += "-Xlog-implicits"
  )


