/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.bck2brwsr.ide.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsniCommentTokenizerTest {

    private static class MockSink implements JsniCommentTokenizer.Sink {
        final List<String> out = new ArrayList<String>();

        public void javascript(String s) {
            out.add("J " + s);
        }

        public void method(String clazz, String method, String signature) {
            out.add("M " + clazz + "|" + method + "|" + signature);
        }

        public void field(String clazz, String field) {
            out.add("F " + clazz + "|" + field);
        }
    }


    @Test
    public void testProcess_nop() throws IOException {
        final String in = "foo bar";
        final List<String> expected = new ArrayList<String>();
        expected.add("J foo bar");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }

    @Test
    public void testProcess_read_static_field() throws IOException {
        final String in = " @com.google.gwt.examples.JSNIExample::myStaticField = val + \" and stuff\";";
        final List<String> expected = new ArrayList<String>();
        expected.add("J  ");
        expected.add("F com.google.gwt.examples.JSNIExample|myStaticField");
        expected.add("J  = val + \" and stuff\";");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }

    @Test
    public void testProcess_write_instance_field() throws IOException {
        final String in = " x.@com.google.gwt.examples.JSNIExample::myInstanceField = val + \" and stuff\";";
        final List<String> expected = new ArrayList<String>();
        expected.add("J  x.");
        expected.add("F com.google.gwt.examples.JSNIExample|myInstanceField");
        expected.add("J  = val + \" and stuff\";");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }

    @Test
    public void testProcess_read_instance_field() throws IOException {
        final String in = " var val = this.@com.google.gwt.examples.JSNIExample::myInstanceField;";
        final List<String> expected = new ArrayList<String>();
        expected.add("J  var val = this.");
        expected.add("F com.google.gwt.examples.JSNIExample|myInstanceField");
        expected.add("J ;");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }


    @Test
    public void testProcess_static_method() throws IOException {
        final String in = " @com.google.gwt.examples.JSNIExample::staticFoo(Ljava/lang/String;)(s);";
        final List<String> expected = new ArrayList<String>();
        expected.add("J  ");
        expected.add("M com.google.gwt.examples.JSNIExample|staticFoo|Ljava/lang/String;");
        expected.add("J (s);");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }


    @Test
    public void testProcess_instance_method() throws IOException {
        final String in = " x.@com.google.gwt.examples.JSNIExample::instanceFoo(Ljava/lang/String;)(s);";
        final List<String> expected = new ArrayList<String>();
        expected.add("J  x.");
        expected.add("M com.google.gwt.examples.JSNIExample|instanceFoo|Ljava/lang/String;");
        expected.add("J (s);");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }


    @Test
    public void testProcess_multiline() throws IOException {
        final String in =
            " x.@com.google.gwt.examples.JSNIExample::instanceFoo(Ljava/lang/String;)(s);" +
            " @com.google.gwt.examples.JSNIExample::myStaticField = val + \" and stuff\";";
        final List<String> expected = new ArrayList<String>();
        expected.add("J  x.");
        expected.add("M com.google.gwt.examples.JSNIExample|instanceFoo|Ljava/lang/String;");
        expected.add("J (s); ");
        expected.add("F com.google.gwt.examples.JSNIExample|myStaticField");
        expected.add("J  = val + \" and stuff\";");

        final JsniCommentTokenizer jsniCommentTokenizer = new JsniCommentTokenizer();
        final MockSink out = new MockSink();
        jsniCommentTokenizer.process(in, out);

        Assert.assertEquals(expected, out.out);
    }
}
