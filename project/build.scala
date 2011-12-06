import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "Ride with Bikes",
    version := "0.1",
    versionCode := 1,
    scalaVersion := "2.9.1",
    platformName in Android := "android-7"
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++
    AndroidManifestGenerator.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "com.github.jbrechtel" %% "robospecs" % "0.2-SNAPSHOT" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "Ride with Bikes",
    file("."),
    settings = General.fullAndroidSettings
  )

//  lazy val tests = Project (
//    "tests",
//    file("tests"),
//    settings = General.settings ++ AndroidTest.androidSettings
//  ) dependsOn main
}
