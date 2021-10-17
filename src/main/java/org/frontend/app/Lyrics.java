package org.frontend.app;

final class Lyrics {
    private Lyrics() {
    }

    static void initialize(Demo model) {
        model.addItem(model.new Item(
                "Why Java in a browser?", null,
                "<p><b>Java</b> is a rock solid programming language "
                + "with more than <em>25 years</em> "
                + "of industry support. Libraries, tooling and overall "
                + "ecosystem of Java is unprecedently large. <em>Write in Java, "
                + "run anywhere</em>. Including in <b>browser</b>!</p>",
                "https://developer.oracle.com/java/", "Learn more about Java"
        ));
        model.addItem(model.new Item(
                "Yes, HTML in a browser!?", "images/html5.svg",
                "<p>HTML (and CSS) is a natural rendering toolkit "
                + "of the browser. <b>HTML</b> is an industry standard even longer "
                + "than <b>Java</b>. "
                + "Reusing it as a primary rendering toolking in Java makes "
                + "a lot of sense! Even when it is not the <em>primary Java UI toolkit</em>."
                + "</p>",
                "https://developer.mozilla.org/en-US/docs/Learn/Getting_started_with_the_web/HTML_basics", "More about HTML"
        ));
        model.addItem(model.new Item(
                "Where's JavaFX?", "images/javafx.png",
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
                "https://gluonhq.com/developer-preview-for-javafx-inside-a-web-browser/", "Real JavaFX in Browser"
        ));
        model.addItem(model.new Item(
                "JavaFX Light!", "images/ds.svg",
                "<p>Let's take just the <em>property bindings</em> API and use it "
                + "in the browser! That allows us to use the same "
                + "<em>programming model</em> as <b>JavaFX</b> while avoiding "
                + "the overhead of the not needed heavyweight stuff."
                + "<p>"
                + "Feels like <b>JavaFX</b>, renders like <b>HTML</b>!"
                + "</p>",
                "https://dukescript.com/javadoc/javafx/com/dukescript/api/javafx/beans/FXBeanInfo.html", "JavaFX Bindings"
        ));
        model.addItem(model.new Item(
                "Try it Yourself!", "images/ds.svg",
                "<p>These are the simple steps to try it yourself:<p>"
                + "<ul> "
                + "<li><code>$ git clone -b GradleDemo https://github.com/jtulach/bck2brwsr.git gradledemo</code></li>"
                + "<li><code>$ cd gradledemo</code></li>"
                + "<li><code>$ JAVA_HOME=/jdk-11 ./gradlew -t bck2brwsrShow</code></li>"
                + "</ul>"
                + "<p>"
                + "A browser page opens. "
                + "Locate HTML pages like <code>src/main/webapp/pages/index.html</code> "
                + "or Java code like like <code>src/main/java/org/frontend/app/Demo.java</code> "
                + "and modify it. Once saved, the browser page shall <em>automatically reload</em> "
                + "with your changes."
                + "</p>",
                "https://github.com/jtulach/bck2brwsr/tree/GradleDemo", "Demo Repository"
        ));
        model.addItem(model.new Item(
                "Ask a Question!", null,
                "<p>Do you want to know more? Ask a question on Twitter: "
                + "Follow the link below and reply with a comment! "
                + "</p>",
                "https://twitter.com/JaroslavTulach/status/1449827890300915718", "Ask a Question"
        ));
    }
}
