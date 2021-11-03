package org.frontend.app;

import java.util.Objects;
import org.frontend.app.Demo.Item;

final class Lyrics {
    private Lyrics() {
    }

    static void initialize(Demo model) {
        model.addItem(model.new Item(
                "Why Java in a browser?", null, "https://www.youtube.com/embed/cUSDknlhxcY",
                "<p><b>Java</b> is a rock solid programming language "
                + "with more than <em>25 years</em> "
                + "of industry support. Libraries, tooling and overall "
                + "ecosystem of Java is unprecedently large. <em>Write in Java, "
                + "run anywhere</em>. Including in <b>browser</b>!</p>",
                "https://www.youtube.com/watch?v=cUSDknlhxcY", "Learn more about Java"));
        model.addItem(model.new Item(
                "Yes, HTML in a browser!?", "images/html5.svg", null,
                "<p>HTML (and CSS) is a natural rendering toolkit "
                + "of the browser. <b>HTML</b> is an industry standard even longer "
                + "than <b>Java</b>. "
                + "Reusing it as a primary rendering toolking in Java makes "
                + "a lot of sense! Even when it is not the <em>primary Java UI toolkit</em>."
                + "</p>",
                "https://developer.mozilla.org/en-US/docs/Learn/Getting_started_with_the_web/HTML_basics", "More about HTML"));
        model.addItem(model.new Item(
                "Where's JavaFX?", "images/javafx.svg", null,
                "<p>HTML!? <em>I want my JavaFX</em>!<p> "
                + "<p>Sure, you can have your JavaFX even in the browser "
                + "(btw. thanks to the <em>same transpiler</em> technology), "
                + "including all the scene graph, rendering pipelines & other "
                + "goodies that make full featured <b>JavaFX</b> so <em>heavyweight</em>. "
                + "</p><p>"
                + "The problem is that browser already contains a WebGL graph, canvas, "
                + "and heavily optimized rendering toolkit. Rather than duplicating it, "
                + "let's reuse it!"
                + "</p>",
                "https://gluonhq.com/developer-preview-for-javafx-inside-a-web-browser/", "Real JavaFX in Browser"));
        model.addItem(model.new Item(
                "JavaFX Lite!", "images/ds.svg", null,
                "<p>Let's take just the <em>property bindings</em> API and use it "
                + "in the browser! That allows us to use the same "
                + "<em>programming model</em> as <b>JavaFX</b> while avoiding "
                + "the overhead of the not needed heavyweight stuff."
                + "<p>"
                + "Feels like <b>JavaFX</b>, renders like <b>HTML</b>!"
                + "</p>",
                "https://dukescript.com/javadoc/javafx/com/dukescript/api/javafx/beans/FXBeanInfo.html", "JavaFX Bindings"));
        model.addItem(model.new Item(
                "Transpile Java into JavaScript!", "images/js.svg", null,
                "<p>Unlike other solutions that try to bring <b>Java</b>  "
                + "experience to browser, this one is based on <em>transpiling</em> - "
                + "converting <em>Java bytecode</em> into <em>JavaScript</em> that does the same thing. "
                + "</p><p> "
                + "There are many JVM to JavaScript transpilers including <b>Bck2Brwsr VM</b>, <b>TeaVM</b> and more. "
                + "They work suprisingly well. The execution speed is usually not a problem. "
                + "However one cannot expect miracles - browser environment is limited - "
                + "no <em>filesystem</em>, no <em>sockets</em>, etc. The <b>Java</b> code needs to "
                + "be properly crafted for the browser.",
                "https://github.com/jtulach/bck2brwsr", "Learn about a transpiler"));
        model.addItem(model.new Item(
                "Try it Yourself!", "images/ds.svg", null,
                "<p>These are the simple steps to try it yourself:<p>"
                + "<ul> "
                + "<li><code>$ git clone -b GradleDemo https://github.com/jtulach/bck2brwsr.git gradledemo</code></li>"
                + "<li><code>$ cd gradledemo</code></li>"
                + "<li><code>$ JAVA_HOME=/jdk-11 ./gradlew -t bck2brwsrShow</code></li>"
                + "</ul>"
                + "<p>"
                + "A browser page opens. "
                + "Locate HTML pages like <code>src/main/webapp/pages/index.html</code> "
                + "or Java code like <code>src/main/java/org/frontend/app/Demo.java</code> "
                + "and modify it. Once saved, the browser page shall <em>automatically reload</em> "
                + "with your changes."
                + "</p>",
                "https://github.com/jtulach/bck2brwsr/tree/GradleDemo", "Demo Repository"));
        model.addItem(model.new Item(
                "Debugging!", "images/java.svg", null,
                "<p>Launch the application in classical JVM mode:</p>"
                + "<pre>\n"
                + "$ JAVA_HOME=/jdk-11 ./gradlew run --debug-jvm\n"
                + "Listening for transport dt_socket at address: 5005\n"
                + "</pre>\n"
                + "<p>"
                + "A <code>WebView</code> page opens. Attach your favorite debugger "
                + "and step through your code as with any regular <em>Java(FX) code</em>."
                + "</p>",
                "https://netbeans.org", "Get some good Java IDE!"));
        model.addItem(model.new Item(
                "Ask a Question!", "images/twitter.svg", null,
                "<p>Do you want to know more? Ask a question on Twitter: "
                + "Follow the link below and reply with a comment! "
                + "</p>",
                null, null));

        String hash = Route.getLocation("hash");
        for (Item item : model.todos) {
            String text = Route.sanitize(item.desc);
            if (Objects.equals(hash, text)) {
                model.selected.set(item);
            }
        }
    }
}
