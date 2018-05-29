# Let's get started with **Gradle** and [Bck2Brwsr VM](../README.md)!!

The way to create new projects in **Gradle** is to initialize an empty
skeleton. Execute:
```bash
$ mkdir demo
$ cd demo
demo$ gradle init --type java-application
demo$ find *
build.gradle
src/test/java/AppTest.java
src/main/java/App.java
gradle/wrapper/gradle-wrapper.properties
gradle/wrapper/gradle-wrapper.jar
gradlew
gradlew.bat
settings.gradle
```
That command gives you the skeletal structure of your project, together with
`./gradlew` launcher. Use it to run the sample `App` in the regular way:
```bash
demo$ ./gradlew run
...
:run
Hello world.

BUILD SUCCESSFUL
```
Now let's run it in a browser! Apply following change to your project
`build.gradle` configuration file:
```diff
diff -r b3329c3585ca build.gradle
--- a/build.gradle      Fri Jun 01 05:35:47 2018 +0200
+++ b/build.gradle      Fri Jun 01 05:41:25 2018 +0200
@@ -6,22 +6,33 @@
  * user guide available at https://docs.gradle.org/3.5/userguide/tutorial_java_projects.html
  */

+buildscript {
+    repositories {
+        mavenCentral()
+    }
+    dependencies {
+        classpath "org.apidesign.bck2brwsr:bck2brwsr-maven-plugin:0.23"
+    }
+}
+
 // Apply the java plugin to add support for Java
 apply plugin: 'java'

 // Apply the application plugin to add support for building an application
 apply plugin: 'application'
+apply plugin: 'bck2brwsr'

 // In this section you declare where to find the dependencies of your project
 repositories {
     // Use jcenter for resolving your dependencies.
     // You can declare any Maven/Ivy/file repository here.
     jcenter()
+    mavenCentral()
 }

 dependencies {
-    // This dependency is found on compile classpath of this component and consumers.
-    compile 'com.google.guava:guava:21.0'
+// No need for this additional huge library in the browser:
+//    compile 'com.google.guava:guava:21.0'

     // Use JUnit test framework
     testCompile 'junit:junit:4.12'
```
and your project is ready to be executed in the browser:
```bash
demo$ ./gradlew bck2brwsrShow
Launching [xdg-open, http://localhost:10039/index.html]
Hello world.
<===========--> 85% EXECUTING
> :bck2brwsrShow

```
A browser is opened and a **Hello World.** message gets printed into its console
and also console of your terminal window.

## Interacting with Java

While in browser console (press F12 to open it), you can interact with the
Java virtual machine by loading **public** Java class and invoking its
**public** static methods:
```js
vm.loadClass("java.lang.System", function(System) {
  System.exit(0)
});
```

## Coding

Your Java code is in `src/main/java/App.java` open it in your
editor, change it and run again
```bash
demo$ ./gradlew bck2brwsrShow
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
