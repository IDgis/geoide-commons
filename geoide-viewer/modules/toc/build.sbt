name := """geoide-toc"""

Common.settings

libraryDependencies ++= Seq(
  Common.Dependencies.geoideDomain,
  Common.Dependencies.geoideDomainTest,
  Common.Dependencies.geoideUtil,
  Common.Dependencies.geoideServiceCommon,
  Common.Dependencies.geoideLayerCommon
)

// Use IDgis repositories:
resolvers := Common.resolvers
