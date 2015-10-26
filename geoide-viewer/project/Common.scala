import sbt._
import Keys._
import play.PlayImport.PlayKeys._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.less.Import._

object Common {
	val geoideCommonsVersion = "1.0.1-SNAPSHOT"
	
	val resolvers = Seq[Resolver] (
		Resolver.mavenLocal,
		"idgis-public" at "http://nexus.idgis.eu/content/groups/public/",
		"idgis-thirdparty" at "http://nexus.idgis.eu/content/repositories/thirdparty/",
		"idgis-restricted" at "http://nexus.idgis.eu/content/groups/restricted/"	
	)
	
	val settings: Seq[Setting[_]] = Seq (
		organization := "nl.idgis.geoide",
		version := geoideCommonsVersion,
		scalaVersion := "2.11.6",
		
		// Set the Java version:
		javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8"),
		
		// Perform RequireJS compilation:
		// pipelineStages := Seq(rjs),

		// Compile less assets:
		includeFilter in (Assets, LessKeys.less) := "*.less"
	)
	
	object Dependencies {
		val akkaRemote = "com.typesafe.akka" %% "akka-remote" % "2.3.4"
		
		val geoideDomain = "nl.idgis.geoide" % "geoide-domain" % "1.0.1-SNAPSHOT"
		val geoideUtil = "nl.idgis.geoide" % "geoide-util" % "1.0.1-SNAPSHOT"
		val geoideRemote = "nl.idgis.geoide" % "geoide-remote" % "1.0.1-SNAPSHOT"
		
		val webjarsPlay = "org.webjars" %% "webjars-play" % "2.3.0"
  		val webjarsBootstrap = "org.webjars" % "bootstrap" % "3.2.0"
  		val webjarsDojo =  "org.webjars" % "dojo" % "1.10.0"
  		val webjarsOpenLayers = "org.webjars" % "openlayers" % "3.8.2"
  		val webjarsPutSelector = "org.webjars" % "put-selector" % "0.3.5"
  		val webjarsFontAwesome = "org.webjars" % "font-awesome" % "4.2.0"
  		
  		val jts = "com.vividsolutions" % "jts" % "1.13"
	}
}
