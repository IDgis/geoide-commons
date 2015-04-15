name := """geoide-print-service"""

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
  Common.Dependencies.geoideUtil,
  Common.Dependencies.geoidePrint,
  Common.Dependencies.geoideHttpClient,
  Common.Dependencies.springAop,
  Common.Dependencies.springTest
)
