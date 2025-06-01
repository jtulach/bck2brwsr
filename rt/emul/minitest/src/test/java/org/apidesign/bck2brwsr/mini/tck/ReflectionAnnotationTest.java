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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class ReflectionAnnotationTest {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ann {
        int integer() default 33;
        double dbl() default 33.3;
        String string() default "Ahoj";
        E enums() default E.B;
        Class<?> type() default String.class;
    }

    @interface AnnAnn {
        Ann ann() default @Ann(
            dbl = 44.4,
            enums = E.A,
            string = "Hi",
            integer = 44
        );
    }

    @Ann
    @AnnAnn
    class D {
        @Ann
        @AnnAnn
        public void method() {
        }
    }

    @Ann(type = String.class)
    @AnnAnn(ann = @Ann(string = "Ciao"))
    enum E {
        A, B
    };

    
    @Compare public String annoClass() throws Exception {
        Retention r = Ann.class.getAnnotation(Retention.class);
        assert r != null : "Annotation is present";
        assert r.value() == RetentionPolicy.RUNTIME : "Policy value is OK: " + r.value();
        return r.annotationType().getName();
    }

    @Compare public boolean isAnnotation() {
        return Ann.class.isAnnotation();
    }
    @Compare public boolean isNotAnnotation() {
        return String.class.isAnnotation();
    }
    @Compare public boolean isNotAnnotationEnum() {
        return E.class.isAnnotation();
    }

    @Compare public int intDefaultAttrIsRead() {
        return D.class.getAnnotation(Ann.class).integer();
    }

    @Compare public double doubleDefaultAttrIsRead() {
        return D.class.getAnnotation(Ann.class).dbl();
    }

    @Compare public String stringDefaultAttrIsRead() {
        return D.class.getAnnotation(Ann.class).string();
    }

    @Compare public String enumDefaultAttrIsRead() {
        return D.class.getAnnotation(Ann.class).enums().toString();
    }

    @Compare public String classDefaultAttrIsRead() {
        return D.class.getAnnotation(Ann.class).type().getName();
    }

    @Compare public String classAttrIsRead() {
        return E.class.getAnnotation(Ann.class).type().getName();
    }

//    @Compare public String defaultAnnotationAttrIsRead() {
//        final Ann ann = D.class.getAnnotation(AnnAnn.class).ann();
//        return ann.string() + ann.dbl() + ann.enums() + ann.integer() + ann.type();
//    }
//
//    @Compare public String annotationAttrIsRead() {
//        final Ann ann = E.class.getAnnotation(AnnAnn.class).ann();
//        return ann.string();
//    }

    private static String toClassNames(Object[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Object o : arr) {
            if (o == null) {
                sb.append("null");
            } else if (o instanceof Ann) {
                sb.append("Ann");
            } else if (o instanceof AnnAnn) {
                sb.append("AnnAnn");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Compare
    public String twoAnnotations() {
        Annotation[] arr = D.class.getAnnotations();
        if (arr == null) {
            return "Null array";
        }
        return toClassNames(arr);
    }

    @Compare
    public String twoDeclaredAnnotations() {
        Annotation[] arr = D.class.getDeclaredAnnotations();
        if (arr == null) {
            return "Null array";
        }
        return toClassNames(arr);
    }

    @Compare
    public String twoAnnotationsOnMethod() throws Exception {
        Annotation[] arr = D.class.getMethod("method").getAnnotations();
        if (arr == null) {
            return "Null array";
        }
        return toClassNames(arr);
    }

    @Compare
    public String noAnnotation() {
        Annotation[] arr = ReflectionAnnotationTest.class.getAnnotations();
        if (arr == null) {
            return "Null array";
        }
        return toClassNames(arr);
    }

    @Compare
    public boolean noAnnotationSingle() {
        Annotation arr = ReflectionAnnotationTest.class.getAnnotation(Ann.class);
        return arr == null;
    }

    @Compare
    public boolean noAnnotationOnMethodSingle() throws Exception {
        Annotation arr = ReflectionAnnotationTest.class.getMethod("noAnnotationOnMethodSingle").getAnnotation(Ann.class);
        return arr == null;
    }

    @Compare
    public String noDeclaredAnnotation() {
        Annotation[] arr = ReflectionAnnotationTest.class.getDeclaredAnnotations();
        if (arr == null) {
            return "Null array";
        }
        return toClassNames(arr);
    }

    @Compare
    public String twoDeclaredAnnotationsOnMethod() throws Exception {
        Annotation[] arr = D.class.getMethod("method").getAnnotations();
        if (arr == null) {
            return "Null array";
        }
        return toClassNames(arr);
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(ReflectionAnnotationTest.class);
    }
    
}
