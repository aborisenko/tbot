import Dependencies._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.0.2"

lazy val root = (project in file("."))
  .settings(
    name := "treasureidentitybot",
    libraryDependencies ++= Dependencies.zio,
    libraryDependencies ++= Dependencies.zioConfig,
    libraryDependencies ++= Seq(
      // https://mvnrepository.com/artifact/io.d11/zhttp
      "io.d11" %% "zhttp" % "2.0.0-RC11",
      "io.d11" %% "zhttp" % "2.0.0-RC11" % Test,
    ),
    libraryDependencies ++= Dependencies.circe,
    libraryDependencies ++= Seq(
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
      "io.github.kitlangton" %% "zio-magic"      % "0.3.11",
      "org.mariadb.jdbc" % "mariadb-java-client" % "3.0.4"// https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
    ),
    libraryDependencies ++= Seq(liquibase),

//    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
)