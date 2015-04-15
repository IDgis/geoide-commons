name := """geoide-toc"""

Common.settings

// Use IDgis repositories:
updateOptions := updateOptions.value.withLatestSnapshots(true).withCachedResolution(false)

// externalResolvers := Common.resolvers		

resolvers := Common.resolvers

libraryDependencies ++= Seq(
  Common.Dependencies.geoideDomain,
  Common.Dependencies.geoideDomainTest,
  Common.Dependencies.geoideUtil,
  Common.Dependencies.geoideServiceCommon,
  Common.Dependencies.geoideLayerCommon
)
