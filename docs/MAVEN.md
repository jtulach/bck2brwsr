# Let's get started with **Maven** and [Bck2Brwsr VM](../README.md)!

The way to create new projects in **Maven** is to generate them from an
archetype. Let's use [maven quickstart](https://maven.apache.org/archetypes/maven-archetype-quickstart/)
and execute:
```bash
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.maven.archetypes \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.3 -DgroupId=org.yourorg -DartifactId=demo -Dversion=1.0-SNAPSHOT
```
A project in the `demo` directory is created with its `pom.xml` configuration file.
In addition to that it also contains two source files `App.java` and `AppTest.java`
in its directory tree. By default the `main` application method just prints
`Hello World!`. Let's run it in a browser! Apply the following change to the
`pom.xml` file:
```diff
diff -r 37410ddd908f pom.xml
--- a/pom.xml   Tue May 29 07:37:34 2018 +0200
+++ b/pom.xml   Tue May 29 07:39:32 2018 +0200
@@ -28,6 +28,14 @@
   </dependencies>

   <build>
+      <plugins>
+          <plugin>
+              <groupId>org.apidesign.bck2brwsr</groupId>
+              <artifactId>bck2brwsr-maven-plugin</artifactId>
+              <version>0.23</version>
+          </plugin>
+      </plugins>
+
     <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
       <plugins>
         <plugin>
```
and your project is ready to be executed in the browser:
```bash
demo$ mvn clean install bck2brwsr:aot bck2brwsr:show
```
A browser is opened and a **Hello World!** message gets printed into its console
and also console of your terminal window.

## Interacting with Java

While in browser console (press F12 to open the developer tools with the console tab),
you can interact with the Java virtual machine by loading **public** Java class
and invoking its **public** static methods:
```js
vm.loadClass("java.lang.System", function(System) {
  System.exit(0)
});
```

## Coding

Your Java code is in `src/main/java/org/yourorg/App.java` open it in your
editor, change it and run again
```bash
demo$ mvn clean install bck2brwsr:aot bck2brwsr:show
```
Well done!

The next step is to interact with JavaScript. For that purpose **Bck2Brwsr VM**
uses [Apache HTML/Java](https://github.com/apache/incubator-netbeans-html4j/)
`@JavaScriptBody` annotation. See the [package tutorial](http://bits.netbeans.org/html+java/1.5.1/net/java/html/js/package-summary.html)
for list of examples.

There are some ready to use libraries built around this annotation:
[Charts](https://dukescript.com/javadoc/charts/),
[canvas](https://dukescript.com/javadoc/canvas/),
[maps](https://dukescript.com/javadoc/leaflet4j/).
Include their coordinates in the dependency in your `pom.xml` and you are
ready to use them all!
