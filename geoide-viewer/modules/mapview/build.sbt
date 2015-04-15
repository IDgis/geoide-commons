name := """geoide-mapview"""

Common.settings

// Use IDgis repositories:
updateOptions := updateOptions.value.withLatestSnapshots(false).withCachedResolution(true)

externalResolvers := Common.resolvers		

resolvers := Seq()

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
  Common.Dependencies.geoideLayerCommon,
  Common.Dependencies.geoideMap,
  Common.Dependencies.webjarsPlay,
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsOpenLayers,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.springContext,
  Common.Dependencies.springAop,
  Common.Dependencies.springTest
)
