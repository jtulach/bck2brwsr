/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
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
    public String callConstructorWithArgs() throws Exception {
        CallWithArgs<String> ref = MyList::new;
        return ref.call("Hello", "World").toString();
    }

    @Compare
    public String callWithMessyArgs() throws Exception {
        CallWithArgs<String> ref = (x, y) -> Arrays.asList("x: " + x.length(), "y: " + y.length());
        CallWithArgs raw = ref;
        try {
            return raw.call(10, 20).toString();
        } catch (Exception ex) {
            // on JDK11 the full message is:
            // java.lang.ClassCastException:class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')
            final String simpleMessage = ex.getMessage().replaceAll("class *", "").replaceAll(" *\\(.*\\)$", "");
            return ex.getClass().getName() + ":" + simpleMessage;
        }
    }

    private static String describe(Object object) {
        return object.getClass().getSimpleName() + ": " + object;
    }

    @Compare
    public String argBox() {
        LongFunction<String> ref = LambdasTest::describe;
        return ref.apply(42);
    }

    @Compare
    public String argUnbox() {
        // there is only one variant of toHexString: static, accepting primitive long
        Function<Long, String> ref = Long::toHexString;
        return ref.apply(0x123456789abcdefL);
    }

    @Compare
    public String argWiden() {
        IntFunction<String> ref = Long::toHexString;
        return ref.apply(42);
    }

    @Compare
    public String argUnboxWiden() {
        Function<Integer, String> ref = Long::toHexString;
        return ref.apply(42);
    }

    @Compare
    public String retBox() {
        Function<String, Long> ref = Long::parseLong;
        return describe(ref.apply("123456789123456789"));
    }

    @Compare
    public long retUnbox() {
        ToLongFunction<String> ref = Long::valueOf;
        return ref.applyAsLong("123456789123456789");
    }

    @Compare
    public double retWiden() {
        ToDoubleFunction<String> ref = Long::parseLong;
        return ref.applyAsDouble("42");
    }

    @Compare
    public double retUnboxWiden() {
        ToDoubleFunction<String> ref = Long::valueOf;
        return ref.applyAsDouble("42");
    }

    @Compare
    public String binaryOperator() {
        BinaryOperator<String> concat = String::concat;
        return concat.apply("cares", "s");
    }

    interface CallWithArgs<T> {
        List<T> call(T e1, T e2);
    }

    static class MyList extends ArrayList<String> {
        public MyList(String e1, String e2) {
            add(e1);
            add(e2);
        }
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

    @Compare
    public String streamToInt() {
        String[] keywords = { "private", "package", "int" };
        IntSummaryStatistics charStats = Arrays.stream(keywords).flatMapToInt(String::chars).summaryStatistics();
        int start = charStats.getMin();
        int size = charStats.getMax() - start + 1;
        return start + ":" + size;
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

