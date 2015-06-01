// import play.PlayImport.PlayKeys.playPackageAssets
name := """geoide-viewer"""

// Include common settings:
Common.settings

// Submodules:
lazy val viewer = (project in file("."))
	.enablePlugins(PlayJava)
	.aggregate(mapView, toc, geoidePrintService, geoideConfig, geoideCore)
	.dependsOn(mapView)
	.dependsOn(toc)
	.dependsOn(geoidePrintService)
	.dependsOn(geoideConfig)
	.dependsOn(geoideCore)

lazy val mapView = (project in file("./modules/mapview"))
	.enablePlugins(PlayJava)
	.dependsOn(geoideCore)

lazy val toc = (project in file("./modules/toc"))
	.enablePlugins(PlayJava)
	.dependsOn(geoideCore)

lazy val geoideConfig = (project in file("./modules/config"))
	.enablePlugins(PlayJava)
	.dependsOn(mapView)
	.dependsOn(geoidePrintService)
	.dependsOn(geoideCore)
	
lazy val geoideCore = (project in file("./modules/core"))
	.enablePlugins(PlayJava)

lazy val geoidePrintService = (project in file("./modules/print-service"))
	.enablePlugins(PlayJava)
	.dependsOn(geoideCore)
	
libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  
  Common.Dependencies.akkaRemote,
  Common.Dependencies.geoideRemote,
  Common.Dependencies.geoideDomain,
  Common.Dependencies.geoideUtil,
  
  Common.Dependencies.geoideDomainTest,
  Common.Dependencies.geoideServiceCommon,
  Common.Dependencies.geoideServiceTms,
  Common.Dependencies.geoideServiceWms,
  Common.Dependencies.geoideServiceWfs,
  Common.Dependencies.geoideLayerCommon,
  Common.Dependencies.geoideLayerDefault,
  Common.Dependencies.webjarsOpenLayers,
  "it.innove" % "play2-pdf" % "1.1.1"
)

// Also deploy the assets:
// packagedArtifacts += ((artifact in playPackageAssets).value -> playPackageAssets.value)

// Use IDgis repositories:
resolvers ++= Common.resolvers

publishTo := {
	val nexus = "http://nexus.idgis.eu/content/repositories/"
	if (isSnapshot.value)
		Some ("idgis-restricted-snapshots" at nexus + "restricted-snapshots")
	else
		Some ("idgis-restricted-releases" at nexus + "restricted-releases")
}

// Configure eclipse plugin:
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

EclipseKeys.classpathTransformerFactories := EclipseKeys.classpathTransformerFactories.value.init

EclipseKeys.preTasks := Seq()