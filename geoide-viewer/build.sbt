// import play.PlayImport.PlayKeys.playPackageAssets

name := """geoide-viewer"""

Common.settings

lazy val viewer = (project in file(".")).enablePlugins(PlayJava)

lazy val mapView = (project in file("./modules/mapview")).enablePlugins(PlayJava)

lazy val toc = (project in file("./modules/toc")).enablePlugins(PlayJava)

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
  Common.Dependencies.webjarsPlay,
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsOpenLayers,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.springContext,
  Common.Dependencies.springAop,
  Common.Dependencies.springTest
)

// Also deploy the assets:
// packagedArtifacts += ((artifact in playPackageAssets).value -> playPackageAssets.value)

// Use IDgis repositories:
resolvers ++= Common.resolvers		
