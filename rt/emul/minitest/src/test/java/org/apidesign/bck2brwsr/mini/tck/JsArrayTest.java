/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.mini.tck;

import net.java.html.boot.script.Scripts;
import net.java.html.lib.Array;
import net.java.html.lib.Function;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.netbeans.html.boot.spi.Fn;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JsArrayTest {

    @Compare
    public String sortStringsInArray() {
        Array<String> list = new Array<>();
        list.push("one");
        list.push("two");
        list.push("three");
        list.push("four");
        list.push("five");
        list.push("six");
        list.push("seven");
        list.push("eight");
        list.push("nine");
        list.push("ten");

        list.sort();
        return list.toString();
    }

    @Compare
    public double addAll() {
        Array<Number> list = new Array<>();
        list.push(1);
        list.push(2.5);
        list.push(3);
        Number all = list.reduce((Function.A2<Number, Number, Double>) (Number n1, Number n2) -> n1.doubleValue() + n2.doubleValue(), 0.0);
        return all.doubleValue();
    }

    @Factory
    public static Object[] create() {
        Fn.activate(Scripts.createPresenter());
        return VMTest.create(JsArrayTest.class);
    }

}
