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

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.beans.BeanProperty;
import java.util.List;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.spi.Proto;

@Prototype
public final class ObservableInterceptor<T> implements MethodInterceptor<T, Object> {
    private final RegisterObservableUIs registry;
    Proto proto;
    MicroHtml4Java<T> micro;

    ObservableInterceptor(RegisterObservableUIs r) {
        this.registry = r;
    }

    private Proto proto(Class<T> type, T bean) {
        if (proto == null) {
            micro = registry.find((Class) bean.getClass());
            proto = micro.createProto(bean, BrwsrCtx.findDefault(type));
        }
        return proto;
    }

    @Override
    public Object intercept(MethodInvocationContext<T, Object> context) {
        final Proto p = proto(context.getDeclaringType(), context.getTarget());
        if (context.getDeclaringType() == Observable.class) {
            return p;
        }
        boolean[] setterGetter = { false, false };
        BeanProperty<? extends Object, Object> prop = micro.findProperty(context, setterGetter);
        if (setterGetter[1]) {
            p.accessProperty(prop.getName());
            p.acquireLock(prop.getName());
        }
        Object res = context.proceed();
        if (setterGetter[1]) {
            p.releaseLock();
        }
        if (setterGetter[0]) {
            p.valueHasMutated(prop.getName());
        }

        if (List.class.equals(context.getReturnType().getType())) {
            return ObservableList.wrap(prop.getName(), p, (List<?>) res);
        }

        return res;
    }
}
