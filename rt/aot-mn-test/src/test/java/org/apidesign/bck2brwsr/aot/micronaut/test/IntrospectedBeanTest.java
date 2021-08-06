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

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import net.java.html.json.Models;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.testng.annotations.Factory;

public class IntrospectedBeanTest {
    @Compare
    public void introspectionAccess() throws Exception {
        try (ApplicationContext ac = ApplicationContext.run()) {
            BeanIntrospection<SampleComponent> intro = BeanIntrospection.getIntrospection(SampleComponent.class);

            SampleComponent bean = ac.getBean(SampleComponent.class);
            SampleComponent bean2 = ac.getBean(SampleComponent.class);

            assertTrue("it's ok", bean.ok);
            BeanProperty<SampleComponent, Boolean> okProperty = intro.getProperty("ok", boolean.class).get();
            assertTrue("Read write", okProperty.isReadWrite());

            okProperty.set(bean, false);
            assertFalse("no longer ok", bean.ok);
            
            okProperty.set(bean2, true);
            assertTrue("still ok", bean2.ok);

            final BeanProperty<SampleComponent, Object> immutableProperty = intro.getProperty("immutable").get();
            assertFalse("Read only, not write", immutableProperty.isReadWrite());
            assertTrue("Read only", immutableProperty.isReadOnly());
            assertEquals("Hi", immutableProperty.get(bean));

            bean.noArgCallback();
            
            assertTrue(Models.isModel(bean.getClass()));
        }
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(IntrospectedBeanTest.class);
    }
}
