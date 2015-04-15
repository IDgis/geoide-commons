name := """geoide-viewerconfig"""

Common.settings

// Use IDgis repositories:
updateOptions := updateOptions.value.withLatestSnapshots(true).withCachedResolution(false)

// externalResolvers := Common.resolvers		

resolvers := Common.resolvers

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
  Common.Dependencies.geoidePrint,
  Common.Dependencies.geoideMap,
  Common.Dependencies.geoideHttpClient,
  Common.Dependencies.webjarsPlay,
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsOpenLayers,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.springContext,
  Common.Dependencies.springAop,
  Common.Dependencies.springTest
)
