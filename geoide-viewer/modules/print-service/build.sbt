name := """geoide-print-service"""

Common.settings

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

// Use IDgis repositories:
resolvers ++= Common.resolvers
