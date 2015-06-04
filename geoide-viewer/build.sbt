// import play.PlayImport.PlayKeys.playPackageAssets
name := """geoide-viewer"""

// Include common settings:
Common.settings

// Submodules:
lazy val viewer = (project in file("."))
	.enablePlugins(PlayJava)
	.aggregate(mapView, toc, geoidePrintService, geoideCore)
	.dependsOn(mapView)
	.dependsOn(toc)
	.dependsOn(geoidePrintService)
	.dependsOn(geoideCore)

lazy val mapView = (project in file("./modules/mapview"))
	.enablePlugins(PlayJava)
	.dependsOn(geoideCore)

lazy val toc = (project in file("./modules/toc"))
	.enablePlugins(PlayJava)
	.dependsOn(geoideCore)

lazy val geoideCore = (project in file("./modules/core"))
	.enablePlugins(PlayJava)

lazy val geoidePrintService = (project in file("./modules/print-service"))
	.enablePlugins(PlayJava)
	.dependsOn(geoideCore)
	
libraryDependencies ++= Seq(
  cache,
  
  Common.Dependencies.akkaRemote,
  Common.Dependencies.geoideRemote,
  Common.Dependencies.geoideDomain,
  
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.webjarsFontAwesome
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

// Setup Eclipse:
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java           // Java project. Don't expect Scala IDE
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes 
EclipseKeys.preTasks := Seq(compile in Compile)                  // Compile the project before generating Eclipse files, so that .class files for views and routes are present