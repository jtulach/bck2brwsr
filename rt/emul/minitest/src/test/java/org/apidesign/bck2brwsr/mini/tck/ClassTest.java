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
package org.apidesign.bck2brwsr.mini.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ClassTest {
    @Compare
    public boolean isDoubleArrayAssignableToObject() throws Exception {
        double[] dbl = new double[0];
        Class<?> dblCls = dbl.getClass();
        return Object.class.isAssignableFrom(dblCls);
    }

    @Compare
    public boolean isDoubleArrayAssignableToString() throws Exception {
        double[] dbl = new double[0];
        Class<?> dblCls = dbl.getClass();
        return String.class.isAssignableFrom(dblCls);
    }

    private String toClassInfo(Class<?> c) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("name: ").append(c.getName()).append("\n");
        sb.append("local: ").append(c.isLocalClass()).append("\n");
        sb.append("member: ").append(c.isMemberClass()).append("\n");
        sb.append("annonymous: ").append(c.isAnonymousClass()).append("\n");
        return sb.toString();
    }


    @Compare
    public String globalClass() throws Exception {
        return toClassInfo(ClassTest.class);
    }

    @Compare
    public String localClass() throws Exception {
        class Local {
        }
        return toClassInfo(Local.class);
    }

    class Member {
    }

    @Compare
    public String memberClass() throws Exception {
        return toClassInfo(Member.class);
    }

    static class NonMember {
    }

    @Compare
    public String nonMemberClass() throws Exception {
        return toClassInfo(NonMember.class);
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ClassTest.class);
    }
}
