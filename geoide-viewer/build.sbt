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
  cache,
  
  Common.Dependencies.akkaRemote,
  
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.webjarsFontAwesome,
  
  Common.Dependencies.springContext,
  Common.Dependencies.springAop,
  
  "it.innove" % "play2-pdf" % "1.1.1"
)

// Also deploy the assets:
// packagedArtifacts += ((artifact in playPackageAssets).value -> playPackageAssets.value)

// Use IDgis repositories:
resolvers := Common.resolvers ++ resolvers.value

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