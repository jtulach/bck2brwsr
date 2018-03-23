
enablePlugins(ScalaJSPlugin)

name := "Longs for Bck2Brwsr"
scalaVersion := "2.12.2"

// This is an application with a main method
scalaJSUseMainModuleInitializer := false

scalaJSLinkerConfig ~= { _.withSemantics(_.optimized) }
