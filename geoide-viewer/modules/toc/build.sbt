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
resolvers ++= Common.resolvers

publishTo := {
	val nexus = "http://nexus.idgis.eu/content/repositories/"
	if (isSnapshot.value)
		Some ("idgis-restricted-snapshots" at nexus + "restricted-snapshots")
	else
		Some ("idgis-restricted-releases" at nexus + "restricted-releases")
}
