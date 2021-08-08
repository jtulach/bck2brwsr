# Let's get started with **Gradle** and [Bck2Brwsr VM](../README.md)!!

The way to create new projects in **Gradle** is to initialize an empty
skeleton. Execute (with Gradle 7.0 and JDK8 or JDK11):
```bash
$ mkdir demo
$ cd demo
demo$ gradle init --type java-application
demo$ find *
app/build.gradle
app/src/main/java/mytest/App.java
app/src/test/java/mytest/AppTest.java
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
diff --git a/app/build.gradle b/app/build.gradle
index 2259ee7..adf3461 100644
--- a/app/build.gradle
+++ b/app/build.gradle
@@ -6,11 +6,22 @@
  * User Manual available at https://docs.gradle.org/7.0/userguide/building_java_projects.html
  */
 
+buildscript {
+    repositories {
+        mavenCentral()
+    }
+    dependencies {
+        classpath "org.apidesign.bck2brwsr:bck2brwsr-maven-plugin:0.51"
+    }
+}
+
 plugins {
     // Apply the application plugin to add support for building a CLI application in Java.
     id 'application'
 }
 
+apply plugin: 'bck2brwsr'
+
 repositories {
     // Use Maven Central for resolving dependencies.
     mavenCentral()
@@ -20,8 +31,8 @@ dependencies {
     // Use JUnit test framework.
     testImplementation 'junit:junit:4.13.1'
 
-    // This dependency is used by the application.
-    implementation 'com.google.guava:guava:30.0-jre'
+    // No need for such a huge dependency in browser
+    // implementation 'com.google.guava:guava:30.0-jre'
 }
 
 application {
jarda@xps:~/tmp/x$ git diff
diff --git a/app/build.gradle b/app/build.gradle
index 2259ee7..adf3461 100644
--- a/app/build.gradle
+++ b/app/build.gradle
@@ -6,11 +6,22 @@
  * User Manual available at https://docs.gradle.org/7.0/userguide/building_java_projects.html
  */
 
+buildscript {
+    repositories {
+        mavenCentral()
+    }
+    dependencies {
+        classpath "org.apidesign.bck2brwsr:bck2brwsr-maven-plugin:0.50"
+    }
+}
+
 plugins {
     // Apply the application plugin to add support for building a CLI application in Java.
     id 'application'
 }
 
+apply plugin: 'bck2brwsr'
+
 repositories {
     // Use Maven Central for resolving dependencies.
     mavenCentral()
@@ -20,8 +31,8 @@ dependencies {
     // Use JUnit test framework.
     testImplementation 'junit:junit:4.13.1'
 
-    // This dependency is used by the application.
-    implementation 'com.google.guava:guava:30.0-jre'
+    // No need for such a huge dependency in browser
+    // implementation 'com.google.guava:guava:30.0-jre'
 }
 
 application {
```
Now verify your `build.gradle` project changes are correct and your project
can still be executed from a command line. Run:
```bash
demo$ ./gradlew run

> Task :app:run
Hello World!

BUILD SUCCESSFUL in 1s
2 actionable tasks: 2 executed
```
again. If it succeeds, your project should also be ready to be executed in
a browser:
```bash
demo$ $ ./gradlew bck2brwsrShow
Starting a Gradle Daemon, 2 incompatible Daemons could not be reused, use --status for details
[INFO] Started listener bound to [0.0.0.0:28099]
[INFO] [HttpServer] Started.
[INFO] Showing http://localhost:28099/index.html
[INFO] Trying Desktop.browse on OpenJDK 64-Bit Server VM 11.0.8+10-LTS by Azul Systems, Inc.
[INFO] Desktop.browse not supported: The BROWSE action is not supported on the current platform!
[INFO] Launching [xdg-open, http://localhost:28099/index.html]

> Task :app:bck2brwsrShow
Avaiting HTTP server...
Started listener bound to [0.0.0.0:28099]
[HttpServer] Started.
Showing http://localhost:28099/index.html
Launching [xdg-open, http://localhost:28099/index.html]
Hello World!
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
uses [Apache HTML/Java](https://github.com/apache/netbeans-html4j/)
`@JavaScriptBody` annotation. See the [package tutorial](http://bits.netbeans.org/html+java/1.7.1/net/java/html/js/package-summary.html)
for list of examples.

There are some ready to use libraries built around this annotation:
[Charts](https://dukescript.com/javadoc/charts/),
[canvas](https://dukescript.com/javadoc/canvas/),
[maps](https://dukescript.com/javadoc/leaflet4j/).
Include their coordinates in the dependency in your `pom.xml` and you are
ready to use them all!
