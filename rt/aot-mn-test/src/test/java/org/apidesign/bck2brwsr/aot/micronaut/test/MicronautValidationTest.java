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
package org.apidesign.bck2brwsr.aot.micronaut.test;

import io.micronaut.core.annotation.Introspected;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Positive;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

public class MicronautValidationTest {

    @Compare
    public String triangle() {
        Triangle t = new Triangle(3, 4, 5);
        return t.toString();
    }

    @Compare
    public boolean validTriangle() {
        Triangle t = new Triangle(3, 4, 5);
        TriangleValidatorFactory f = new TriangleValidatorFactory();
        return f.triangleValidator().isValid(t, null);
    }

    @Compare
    public boolean invalidTriangle() {
        Triangle t = new Triangle(3, 4, 15);
        TriangleValidatorFactory f = new TriangleValidatorFactory();
        return f.triangleValidator().isValid(t, null);
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(MicronautValidationTest.class);
    }

    @ValidTriangle
    @Introspected
    public static class Triangle {
        @Positive(message = "Side must be greater than zero") Integer a;
        @Positive(message = "Side must be greater than zero") Integer b;
        @Positive(message = "Hypotenuse must be greater than zero") Integer c;

        public Triangle() {
        }

        public Triangle(Integer a, Integer b, Integer c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public Integer getA() {
            return a;
        }

        public Integer getB() {
            return b;
        }

        public Integer getC() {
            return c;
        }

        @Override
        public String toString() {
            return "Triangle{" + "a=" + a + ", b=" + b + ", c=" + c + '}';
        }
    }

    @Target({ TYPE })
    @Retention(RUNTIME)
    @Documented
    @Constraint(validatedBy = { })//TriangleValidator.class })
    public @interface ValidTriangle {
        String message() default "{ This triangle dimensions are not valid }";
    }

    public static class TriangleValidator implements ConstraintValidator<ValidTriangle, Triangle> {
        @Override
        public void initialize(ValidTriangle constraintAnnotation) {
        }

        @Override
        public boolean isValid(Triangle t, ConstraintValidatorContext context) {
            return t == null || (t.a + t.b > t.c && t.a + t.c > t.b && t.b + t.c > t.a);
        }
    }

    @io.micronaut.context.annotation.Factory
    public static class TriangleValidatorFactory {

        @javax.inject.Singleton
        ConstraintValidator<ValidTriangle, Triangle> triangleValidator() {
            return (t, context)
                    -> t == null || (t.a + t.b > t.c && t.a + t.c > t.b && t.b + t.c > t.a);
        }
    }}
