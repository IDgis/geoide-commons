name := """geoide-core"""

Common.settings

// Use IDgis repositories:
updateOptions := updateOptions.value.withLatestSnapshots(true).withCachedResolution(false)

// externalResolvers := Common.resolvers		

resolvers := Common.resolvers

libraryDependencies ++= Seq(
  cache,
  Common.Dependencies.geoideDomain,
  Common.Dependencies.webjarsPlay,
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.springContext,
  Common.Dependencies.springAop,
  Common.Dependencies.springTest,
  Common.Dependencies.webjarsFontAwesome
)