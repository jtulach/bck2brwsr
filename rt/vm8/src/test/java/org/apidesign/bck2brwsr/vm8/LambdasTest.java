/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.vm8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LambdasTest extends LambdasSuper {
    @Compare public String StringverifyJSTime() throws Exception {
        return Lambdas.compound();
    }

    @Compare
    public int canCallLambda() {
        int[] arr = {0};
        Runnable incr = () -> {
            arr[0]++;
        };
        incr.run();
        return arr[0];
    }

    @Compare
    public String lambdaReturnsString() throws Exception {
        Callable<String> lambda = () -> "Hello World!";
        return lambda.call();
    }

    private interface Convertor<P, R> {

        public R convert(P value);
    }

    @Compare
    public int convertToLength() {
        Convertor<String, Integer> lambda = (String s) -> s.getBytes().length;
        return lambda.convert("buu");
    }

    private int meaningOfWorld = 0;

    @Compare
    public int accessToInstanceVar() {
        Runnable lambda = () -> {
            meaningOfWorld = 42;
        };
        lambda.run();
        return meaningOfWorld;
    }

    @Compare
    public int localMeaningOfWorld() {
        int[] meansOfWorld = new int[1];
        Runnable lambda = () -> {
            meansOfWorld[0] = 42;
        };
        lambda.run();
        return meansOfWorld[0];
    }

    @Compare
    public int useLocalVars() throws Exception {
        boolean bool = true;
        byte b = 2;
        short s = 3;
        int i = 4;
        long l = 5;
        float f = 6;
        double d = 7;
        char c = 8;
        Callable<Integer> lambda = () -> (int) ((bool ? 1 : 0) + b + s + i + l + f + d + c);
        return lambda.call();
    }

    @Compare
    public String callVirtualMethod() throws Exception {
        String foo = "foo";
        Callable<String> ref = foo::toUpperCase;
        return ref.call();
    }

    @Compare
    public int callInterfaceMethod() throws Exception {
        List<String> foos = Arrays.asList("foo");
        Callable<Integer> ref = foos::size;
        return ref.call();
    }

    @Compare
    public long callStaticMethod() throws Exception {
        long expected = System.currentTimeMillis();
        Callable<Long> ref = System::currentTimeMillis;
        return ref.call() & ~0xffff;
    }

    @Compare
    public String callConstructor() throws Exception {
        Callable<List<String>> ref = ArrayList<String>::new;
        return ref.call().toString();
    }

    @Compare
    public String superMethodOverridenByThis() throws Exception {
        Callable<String> ref = super::inheritedMethod;
        return ref.call();
    }

    @Override
    String inheritedMethod() {
        return "overridden version";
    }

    @Compare
    public String referencePrivateClassMethod() throws Exception {
        StringBuilder sb = new StringBuilder();

        Callable<String> ref1 = LambdasTest::privateClassMethod;
        sb.append(ref1.call());

        Callable<String> ref2 = this::privateInstanceMethod;
        sb.append("\n").append(ref2.call());

        // Normal method calls should still work after our magic
        // of making them them accessible from the lambda classes.
        sb.append("\n").append(privateClassMethod());
        sb.append("\n").append(privateInstanceMethod());

        return sb.toString();
    }

    private String privateInstanceMethod() {
        return "foo";
    }

    private static String privateClassMethod() {
        return "foo";
    }

    private String unrelatedPrivateMethod() {
        return "foo";
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(LambdasTest.class);
    }
}

