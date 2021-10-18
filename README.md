
# JavaFX Light Tutorial

Demo and tutorial for **JavaFX** coding used to control **HTML5**
components. Check http://javafx.apidesign.org for live demo and interactive
presentation of many additional details.

## Getting Started

Get the demo and run in it in development mode:

```bash
$ git clone -b GradleDemo https://github.com/jtulach/bck2brwsr.git gradledemo
$ cd gradledemo
$ JAVA_HOME=/jdk-11 ./gradlew -t bck2brwsrShow
```

A browser page opens. Locate *HTML pages* like 
`src/main/webapp/pages/index.html` or *Java code* like 
`src/main/java/org/frontend/app/Demo.java` and modify it.
Once saved, the browser page shall automatically reload with your changes.

## Debugging

Launch the application in classical JVM mode:

```bash
$ JAVA_HOME=/jdk-11 ./gradlew run --debug-jvm
Listening for transport dt_socket at address: 5005
```
A `WebView` page opens. Attach your favorite debugger
and step through your code as with any regular <em>Java(FX) code</em>.

# Ask a Question

Do you want to know more? Ask a question on Twitter.
Reply to [this tweet with a comment](https://twitter.com/JaroslavTulach/status/1449827890300915718)!
