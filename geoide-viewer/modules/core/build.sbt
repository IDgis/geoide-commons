name := """geoide-core"""

Common.settings

// Use IDgis repositories:
updateOptions := updateOptions.value.withLatestSnapshots(false).withCachedResolution(true)

externalResolvers := Common.resolvers		

resolvers := Seq()

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