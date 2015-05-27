name := """geoide-core"""

Common.settings

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

// Use IDgis repositories:
resolvers ++= Common.resolvers

publishTo := {
	val nexus = "http://nexus.idgis.eu/content/repositories/"
	if (isSnapshot.value)
		Some ("idgis-restricted-snapshots" at nexus + "restricted-snapshots")
	else
		Some ("idgis-restricted-releases" at nexus + "restricted-releases")
}
