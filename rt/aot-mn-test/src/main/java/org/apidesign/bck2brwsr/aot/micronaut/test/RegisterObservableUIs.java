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

import io.micronaut.context.BeanContext;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.event.annotation.EventListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class RegisterObservableUIs {
    private final BeanContext ctx;
    private final Map<Class<?>, MicroHtml4Java> TYPES = new HashMap<>();

    RegisterObservableUIs(BeanContext beanContext) {
        this.ctx = beanContext;
    }

    private synchronized <T> MicroHtml4Java<T> register(BeanContext context, BeanDefinition<T> def) {
        Class<T> subClass = def.getBeanType();
        BeanIntrospection intro = BeanIntrospection.getIntrospection(subClass.getSuperclass());
        return new MicroHtml4Java(intro, subClass, def);
    }

    final <T> MicroHtml4Java<T> find(Class<T> type) {
        MicroHtml4Java micro = TYPES.get(type);
        assert micro != null;
        return micro;
    }

    @EventListener
    synchronized void init(StartupEvent event) {
        Collection<BeanDefinition<?>> beanDefinitions = ctx.getBeanDefinitions(Qualifiers.byStereotype(Observable.UI.class));
        for (BeanDefinition<?> def : beanDefinitions) {
            MicroHtml4Java<?> micro = register(ctx, def);
            TYPES.put(def.getBeanType(), micro);
        }
    }
}
