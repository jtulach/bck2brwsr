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
package org.apidesign.truffle;

import com.oracle.truffle.api.TruffleLanguage;


@TruffleLanguage.Registration(
    name = "Java",
    mimeType = { "application/x-jar", "application/x-java-class", "text/java" },
    version = "0.20"
)
public class Bck2BrwsrLanguage extends TruffleLanguage<VM> {

    @Override
    protected VM createContext(Env env) {
        return new VM(env);
    }

    @Override
    protected void initializeContext(VM context) throws Exception {
        context.initialize();
    }

    @Override
    protected Object findExportedSymbol(VM context, String globalName, boolean onlyExplicit) {
        return null;
    }

    @Override
    protected Object getLanguageGlobal(VM context) {
        return null;
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return false;
    }

}
