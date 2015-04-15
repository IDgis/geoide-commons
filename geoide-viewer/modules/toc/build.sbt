name := """geoide-toc"""

Common.settings

// Use IDgis repositories:
updateOptions := updateOptions.value.withLatestSnapshots(false).withCachedResolution(true)

externalResolvers := Common.resolvers		

resolvers := Seq()

libraryDependencies ++= Seq(
  Common.Dependencies.geoideDomain,
  Common.Dependencies.geoideDomainTest,
  Common.Dependencies.geoideUtil,
  Common.Dependencies.geoideServiceCommon,
  Common.Dependencies.geoideLayerCommon
)
