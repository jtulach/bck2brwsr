[![Build Status](http://hudson.apidesign.org/hudson/job/bck2brwsr/badge/icon)](http://hudson.apidesign.org/hudson/job/bck2brwsr/badge/icon)


# Bring Java Back to Browser!

[Bck2Brwsr](http://bck2brwsr.apidesign.org) VM is a Java virtual machine which is capable to take bytecode 
and transpile it
(either ahead-of-time - e.g. during compilation on your build machine - or just-in-time, e.g. in a browser)
into appropriate JavaScript code which does the same thing. As a result one can write in Java, 
compile with `javac` compiler, run it with [Bck2Brwsr](http://bck2brwsr.apidesign.org) in any modern browser.

# Getting Started

If you have a **Maven** or **Gradle** project it is as easy as adding one plugin
to get your Java application into browser. Imagine you have a simple hello world
application in `src/main/java` structure of your project:
```java
public class Hello {
    public static void main(String... args) {
        System.err.println("Hello from Java in JS!");
    }
}
```
then it is just about adding following plugin into your `pom.xml`:
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apidesign.bck2brwsr</groupId>
                <artifactId>bck2brwsr-maven-plugin</artifactId>
                <version>0.51</version>
            </plugin>
        </plugins>
    </build>
```
and executing `mvn clean install bck2brwsr:aot bck2brwsr:show` -
more info in a [dedicated page](docs/MAVEN.md).

It is similarly easy with **Gradle**. Just by adding (the same)
single plugin and configuring your script to use it:
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.apidesign.bck2brwsr:bck2brwsr-maven-plugin:0.51"
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'bck2brwsr'
```
you'll be able to invoke `./gradlew bck2brwsrShow` and see your Java 
application greetings from the browser. 
An in-depth [tutorial is available](docs/GRADLE.md) as well.

## How do I deploy?

Just copy the generated files to any web hosting service! No application
server needed, no code needs to be executed on the server. The whole application is 
*just* a set of pages that can be copied anywhere. In case of **Gradle** it consists of:
```bash
$ find build/web/
build/web/
build/web/index.html
build/web/lib
build/web/lib/emul-0.51-rt.js
build/web/main.js
build/web/bck2brwsr.js
```
The structure of pages generated by **Maven** is similar with the primary HTML file being `target/index.html`.

## Talking to Your Java Code

Once your application is running in the browser, you can interact with it
from a console. Btw. output of `System.out` and `System.err` is primarily 
printed into the console. Open the console and try:
```js
var System = vm.loadClass("java.lang.System")
System.exit(0)
```
and voilá! your browser is closed. Obviously the above snippet is more useful for 
other methods in your application than `System.exit`. You can use it to 
locate any `public class` and invoke any of its `public static` methods.

## Launching

Browsers download scripts asynchronously, as such the `vm.loadClass` may not be
immediatelly ready, but it may get loaded with a delay. To accomodate such restriction
one can use a callback style of the above:
```js
vm.loadClass("java.lang.System", function(System) {
  System.exit(0)
});
```
Usage of callback is recommended when executing the first Java method (as that usually means
loading of new scripts) and is exactly what the default content of `index.html` does:
```html
<script src='bck2brwsr.js'></script>
<script>
var vm = bck2brwsr('main.js');
vm.loadClass('Hello', function(mainClass) {
  mainClass.invoke('main');
});
</script>
```
The first line loads the [Bck2Brwsr](http://bck2brwsr.apidesign.org) virtual machine. 
Then one fills it with transpiled code of one's application `var vm = bck2brwsr('main.js');`
together with required libraries. At the end one invokes `main` method of the `Hello` class
using the callback syntax.

# More?

More info at [project home page](http://bck2brwsr.apidesign.org). Questions to [bck2brwsr group](https://groups.google.com/forum/#!forum/bck2brwsr).
