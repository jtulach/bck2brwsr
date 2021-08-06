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

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.spi.Proto;

final class MicroHtml4Java<T> extends Proto.Type<T> {
    private final BeanIntrospection<T> intro;
    private final List<ExecutableMethod<T, ?>> methods;

    MicroHtml4Java(BeanIntrospection<T> intro, Class<? extends T> subclazz, BeanDefinition<T> def) {
        super(subclazz, subclazz.getSuperclass(), intro.getBeanProperties().size(), def.getExecutableMethods().size());
        this.intro = intro;
        this.methods = new ArrayList<>(def.getExecutableMethods());
        final Collection<BeanProperty<T, Object>> props = intro.getBeanProperties();
        final Collection<ExecutableMethod<T, ?>> tmp = def.getExecutableMethods();
        int i = 0;
        for (BeanProperty<T, Object> p : props) {
            registerProperty(p.getName(), i++, p.isReadOnly());
        }
        i = 0;
        for (ExecutableMethod<T, ?> m : tmp) {
            registerFunction(m.getMethodName(), i++);
        }
    }

    @Override
    protected void setValue(T model, int i, Object o) {
        String name = intro.getPropertyNames()[i];
        BeanProperty<T, Object> prop = intro.getProperty(name).get();
        prop.set(model, o);
    }

    @Override
    protected Object getValue(T model, int i) {
        String name = intro.getPropertyNames()[i];
        BeanProperty<T, Object> prop = intro.getProperty(name).get();
        return prop.get(model);
    }

    @Override
    protected void call(T model, int i, Object o, Object o1) throws Exception {
        ExecutableMethod<T, ?> m = methods.get(i);
        switch (m.getArguments().length) {
            case 0:
                m.invoke(model);
                break;
            case 1:
                m.invoke(model, o);
                break;
            default:
                m.invoke(model, o, o1);
                break;
        }
    }

    @Override
    protected T cloneTo(T model, BrwsrCtx bc) {
        T copy = intro.instantiate();
        for (BeanProperty<T, Object> p : intro.getBeanProperties()) {
            Object value = p.get(model);
            p.set(copy, value);
        }
        return copy;
    }

    @Override
    protected T read(BrwsrCtx bc, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onChange(T model, int i) {
    }

    @Override
    protected Proto protoFor(Object o) {
        return o instanceof Observable ? ((Observable) o).proto() : null;
    }

    final BeanProperty<T, Object> findProperty(MethodInvocationContext<T, Object> context, boolean[] setterGetter) {
        final String methodName = context.getMethodName();
        String expProp;
        if (methodName.startsWith("set")) {
            expProp = methodName.substring(3);
            setterGetter[0] = true;
        } else if (methodName.startsWith("get")) {
            expProp = methodName.substring(3);
            setterGetter[1] = true;
        } else if (methodName.startsWith("is")) {
            expProp = methodName.substring(2);
            setterGetter[1] = true;
        } else {
            return null;
        }
        if (expProp.length() > 0 && Character.isUpperCase(expProp.charAt(0))) {
            expProp = Character.toLowerCase(expProp.charAt(0)) + expProp.substring(1);
        }
        for (BeanProperty<T, Object> p : intro.getBeanProperties()) {
            if (p.getName().equals(expProp)) {
                return p;
            }
        }
        return null;
    }
}
