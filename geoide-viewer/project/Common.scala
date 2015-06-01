import sbt._
import Keys._
import play.PlayImport.PlayKeys._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.less.Import._

object Common {
	val geoideCommonsVersion = "0.1.8-SNAPSHOT"
	
	val resolvers = Seq[Resolver] (
		Resolver.mavenLocal,
		"idgis-public" at "http://nexus.idgis.eu/content/groups/public/",
		"idgis-thirdparty" at "http://nexus.idgis.eu/content/repositories/thirdparty/",
		"idgis-restricted" at "http://nexus.idgis.eu/content/groups/restricted/"	
	)
	
	val settings: Seq[Setting[_]] = Seq (
		organization := "nl.idgis.geoide",
		version := geoideCommonsVersion,
		scalaVersion := "2.11.1",
		
		// Set the Java version:
		javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8"),
		
		// Perform RequireJS compilation:
		// pipelineStages := Seq(rjs),

		// Compile less assets:
		includeFilter in (Assets, LessKeys.less) := "*.less"
	)
	
	object Dependencies {
		val akkaRemote = "com.typesafe.akka" %% "akka-remote" % "2.3.4"
		
		val geoideDomain = "nl.idgis.geoide" % "geoide-domain" % "0.1.8-SNAPSHOT"
		val geoideDomainTest = "nl.idgis.geoide" % "geoide-domain-test" % "0.1.8-SNAPSHOT"
		val geoideUtil = "nl.idgis.geoide" % "geoide-util" % "0.1.8-SNAPSHOT"
		val geoideServiceCommon = "nl.idgis.geoide" % "geoide-service-common" % "0.1.8-SNAPSHOT"
		val geoideServiceTms = "nl.idgis.geoide" % "geoide-service-tms" % "0.1.8-SNAPSHOT"
		val geoideServiceWms = "nl.idgis.geoide" % "geoide-service-wms" % "0.1.8-SNAPSHOT"
		val geoideServiceWfs = "nl.idgis.geoide" % "geoide-service-wfs" % "0.1.8-SNAPSHOT"
		val geoideLayerCommon = "nl.idgis.geoide" % "geoide-layer-common" % "0.1.8-SNAPSHOT"
		val geoideLayerDefault = "nl.idgis.geoide" % "geoide-layer-default" % "0.1.8-SNAPSHOT"
		val geoidePrint = "nl.idgis.geoide" % "geoide-print" % "0.1.8-SNAPSHOT"
		val geoideHttpClient = "nl.idgis.geoide" % "geoide-http-client" % "0.1.8-SNAPSHOT"
		val geoideMap = "nl.idgis.geoide" % "geoide-map" % "0.1.8-SNAPSHOT"
		val geoideRemote = "nl.idgis.geoide" % "geoide-remote" % "0.1.8-SNAPSHOT"
		
		val webjarsPlay = "org.webjars" %% "webjars-play" % "2.3.0"
  		val webjarsBootstrap = "org.webjars" % "bootstrap" % "3.2.0"
  		val webjarsDojoBase = "org.webjars" % "dojo-base" % "1.10.0-SNAPSHOT"
  		val webjarsOpenLayers = "org.webjars" % "openlayers" % "3.4.0"
  		val webjarsPutSelector = "org.webjars" % "put-selector" % "0.3.5"
  		val webjarsFontAwesome = "org.webjars" % "font-awesome" % "4.2.0"
  		
		val springContext = "org.springframework" % "spring-context" % "4.0.3.RELEASE"
		val springAop = "org.springframework" % "spring-aop" % "4.0.3.RELEASE"
		val springTest = "org.springframework" % "spring-test" % "4.0.3.RELEASE" % "test"
	}
}
