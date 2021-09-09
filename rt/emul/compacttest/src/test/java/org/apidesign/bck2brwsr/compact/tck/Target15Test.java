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
package org.apidesign.bck2brwsr.compact.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.slf4j.spi.LoggerFactoryBinder;
import org.testng.annotations.Factory;

public class Target15Test {
    @Compare
    public String loadLoggerFactoryBinder() {
        Class<LoggerFactoryBinder> c = LoggerFactoryBinder.class;
        return c.getName();
    }

    @Compare
    public String hasBinder() {
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        if (binder == null) {
            throw new IllegalStateException("No binder: " + binder);
        }
        return binder.getClass().getName();
    }

    @Compare
    public String log() {
        Logger log = LoggerFactory.getLogger(Target15Test.class);
        log.trace("Hi there!");
        return log.getName();
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(Target15Test.class);
    }
}
