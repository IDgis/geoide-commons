// import play.PlayImport.PlayKeys.playPackageAssets

name := """geoide-viewer"""

Common.settings

lazy val viewer = (project in file("."))
	.enablePlugins(PlayJava)
	.aggregate(mapView, toc, geoideConfig, geoideCore)
	.dependsOn(mapView)
	.dependsOn(toc)
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
	.dependsOn(geoideCore)
	
lazy val geoideCore = (project in file("./modules/core"))
	.enablePlugins(PlayJava)

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  Common.Dependencies.geoideDomain,
  Common.Dependencies.geoideDomainTest,
  Common.Dependencies.geoideOL3,
  Common.Dependencies.geoideUtil,
  Common.Dependencies.geoideServiceCommon,
  Common.Dependencies.geoideServiceTms,
  Common.Dependencies.geoideServiceWms,
  Common.Dependencies.geoideServiceWfs,
  Common.Dependencies.geoideLayerCommon,
  Common.Dependencies.geoideLayerDefault,
  Common.Dependencies.webjarsOpenLayers
)

// Also deploy the assets:
// packagedArtifacts += ((artifact in playPackageAssets).value -> playPackageAssets.value)

// Use IDgis repositories:
resolvers ++= Common.resolvers		
