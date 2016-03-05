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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.lang.annotation.Annotation;
import org.apidesign.bck2brwsr.vmtest.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** A TestNG {@link Factory} that seeks for {@link Compare} annotations
 * in provided class and builds set of tests that compare the computations
 * in real as well as JavaScript virtual machines. Use as:<pre>
 * {@code @}{@link Factory} public static create() {
 *   return @{link VMTest}.{@link #create(YourClass.class);
 * }</pre>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class CompareCase implements ITest {
    private final Bck2BrwsrCase first, second;
    private final Method m;
    private final double slowdown;
    
    private CompareCase(Method m, Bck2BrwsrCase first, Bck2BrwsrCase second, double slowdown) {
        this.first = first;
        this.second = second;
        this.m = m;
        this.slowdown = slowdown;
    }

    /** Inspects <code>clazz</code> and for each {@lik Compare} method creates
     * instances of tests. Each instance runs the test in different virtual
     * machine and at the end they compare the results.
     * 
     * @param clazz the class to inspect
     * @return the set of created tests
     */
    public static Object[] create(String[] brwsr, Class[] classes, Class<? extends Annotation> brwsrTest) {
        List<Object> ret = new ArrayList<>();
        
        final LaunchSetup l = LaunchSetup.INSTANCE;
        ret.add(l);
        
        {
            String p = System.getProperty("vmtest.brwsrs");
            if (p != null) {
                brwsr = p.split(",");
            }
        }
        
        for (Class clazz : classes) {
            Method[] arr = clazz.getMethods();
            for (Method m : arr) {
                registerCompareCases(m, l, ret, brwsr);
                registerBrwsrCases(brwsrTest, m, l, ret, brwsr);
            }
        }
        return ret.toArray();
    }

    /** Test that compares the previous results.
     * @throws Throwable 
     */
    @Test(dependsOnGroups = "run") public void compareResults() throws Throwable {
        Object v1 = first.value;
        Object v2 = second.value;
        if (v1 instanceof Integer || v1 instanceof Long || v1 instanceof Byte || v1 instanceof Short) {
            try {
                v1 = Long.parseLong(v1.toString());
            } catch (NumberFormatException nfe) {
                v1 = "Can't parse " + v1.toString();
            }
            try {
                v2 = Long.parseLong(v2.toString());
            } catch (NullPointerException | NumberFormatException nfe) {
                v2 = "Can't parse " + v2.toString();
            }
        } else if (v1 instanceof Number) {
            try {
                v1 = Double.parseDouble(v1.toString());
            } catch (NullPointerException | NumberFormatException nfe) {
                v1 = "Can't parse " + v1.toString();
            }
            try {
                v2 = Double.parseDouble(v2.toString());
            } catch (NullPointerException | NumberFormatException nfe) {
                v2 = "Can't parse " + v2.toString();
            }
        } else {
            if (v1 != null) {
                v1 = v1.toString();
            } else {
                v1 = "null";
            }
        }
        try {
            Assert.assertEquals(v2, v1, "Comparing results");
        } catch (AssertionError e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage());
            Bck2BrwsrCase.dumpJS(sb, second);
            throw new AssertionError(sb.toString());
        }
        if (slowdown > 0.0) {
            Bck2BrwsrCase slow;
            Bck2BrwsrCase fast;
            if (first.time >= second.time) {
                slow = second;
                fast = first;
            } else {
                fast = second;
                slow = first;
            }
            if (slow.time * slowdown < fast.time) {
                Assert.fail("Too slow " + slow.getTestName() + " took " + slow.time + " ms vs. " + fast.time + " ms of " + fast.getTestName());
            }
        }
    }
    
    /** Test name.
     * @return name of the tested method followed by a suffix
     */
    @Override
    public String getTestName() {
        return m.getName() + "[Compare " + second.typeName() + "]";
    }
    
    private static void registerCompareCases(Method m, final LaunchSetup l, List<Object> ret, String[] brwsr) {
        Compare c = m.getAnnotation(Compare.class);
        if (c == null) {
            return;
        }
        String slowdownOverride = System.getProperty("vmtest.slowdown");
        final Bck2BrwsrCase real = new Bck2BrwsrCase(m, "Java", null, false, null, null);
        ret.add(real);
        double slowdown = c.slowdown();
        if (slowdown > 0.0 && slowdownOverride != null) {
            slowdown = Double.parseDouble(slowdownOverride);
        }
        Set<Launcher> unique = new HashSet<>();
        for (String b : brwsr) {
            final Launcher s = l.brwsr(b);
            ret.add(s);
            unique.add(s);
            final Bck2BrwsrCase cse = new Bck2BrwsrCase(m, b, s, false, null, null);
            ret.add(cse);
            ret.add(new CompareCase(m, real, cse, slowdown));
        }
        if (c.scripting()) {
            final Launcher jsLauncher = l.javaScript();
            if (unique.add(jsLauncher)) {
                final Bck2BrwsrCase js = new Bck2BrwsrCase(m, "JavaScript", jsLauncher, false, null, null);
                ret.add(js);
                ret.add(new CompareCase(m, real, js, slowdown));
            }
        }
    }
    private static void registerBrwsrCases(Class<? extends Annotation> brwsrTest, Method m, final LaunchSetup l, List<Object> ret, String[] brwsr) {
        Object c = m.getAnnotation(brwsrTest);
        if (c == null) {
            return;
        }
        HtmlFragment f = m.getAnnotation(HtmlFragment.class);
        if (f == null) {
            f = m.getDeclaringClass().getAnnotation(HtmlFragment.class);
        }
        Http.Resource[] r = {};
        Http h = m.getAnnotation(Http.class);
        if (h == null) {
            h = m.getDeclaringClass().getAnnotation(Http.class);
            if (h != null) {
                r = h.value();
            }
        } else {
            r = h.value();
        }
        if (brwsr.length == 0) {
            final Launcher s = l.brwsr(null);
            ret.add(s);
            ret.add(new Bck2BrwsrCase(m, "Brwsr", s, true, f, r));
        } else {
            for (String b : brwsr) {
                final Launcher s = l.brwsr(b);
                ret.add(s);
                ret.add(new Bck2BrwsrCase(m, b, s, true, f, r));
            }
        }
    }
}
