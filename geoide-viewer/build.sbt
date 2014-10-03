import play.PlayImport.PlayKeys.playPackageAssets

name := """geoide-viewer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "nl.idgis.geoide" % "geoide-domain" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-domain-test" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-ol3" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-util" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-service-common" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-service-tms" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-service-wms" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-service-wfs" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-layer-common" % "0.0.1-SNAPSHOT" changing (),
  "nl.idgis.geoide" % "geoide-layer-default" % "0.0.1-SNAPSHOT" changing (),
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "dojo-base" % "1.10.0-SNAPSHOT",
  "org.webjars" % "openlayers" % "2.13.1",
  "org.webjars" % "put-selector" % "0.3.5",
  "org.springframework" % "spring-context" % "4.0.3.RELEASE",
  "org.springframework" % "spring-aop" % "4.0.3.RELEASE",
  "org.springframework" % "spring-test" % "4.0.3.RELEASE" % "test"
)

// Compile less assets:
includeFilter in (Assets, LessKeys.less) := "*.less"

// Perform RequireJS compilation:
pipelineStages := Seq(rjs)

// Also deploy the assets:
// packagedArtifacts += ((artifact in playPackageAssets).value -> playPackageAssets.value)

// Use IDgis repositories:
resolvers += "idgis-public" at "http://nexus.idgis.eu/content/groups/public/"

resolvers += "idgis-thirdparty" at "http://nexus.idgis.eu/content/repositories/thirdparty/"

resolvers += "idgis-restricted" at "http://nexus.idgis.eu/content/groups/restricted/"