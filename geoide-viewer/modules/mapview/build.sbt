name := """geoide-mapview"""

Common.settings

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  Common.Dependencies.geoideDomain,
  Common.Dependencies.geoideDomainTest,
  Common.Dependencies.geoideUtil,
  Common.Dependencies.geoideServiceCommon,
  Common.Dependencies.geoideLayerCommon,
  Common.Dependencies.webjarsPlay,
  Common.Dependencies.webjarsBootstrap,
  Common.Dependencies.webjarsDojoBase,
  Common.Dependencies.webjarsOpenLayers,
  Common.Dependencies.webjarsPutSelector,
  Common.Dependencies.springContext,
  Common.Dependencies.springAop,
  Common.Dependencies.springTest
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

// Configure eclipse plugin:
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

EclipseKeys.classpathTransformerFactories := EclipseKeys.classpathTransformerFactories.value.init

EclipseKeys.preTasks := Seq()
