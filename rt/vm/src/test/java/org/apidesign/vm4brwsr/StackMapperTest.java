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
package org.apidesign.vm4brwsr;

import javax.script.ScriptEngine;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class StackMapperTest {
    
    public StackMapperTest() {
    }
    
    @Test public void replaceValueThatAnotherReplaceDependsOn()
    throws Exception {
        StringBuilder sb = new StringBuilder();
        
        StackMapper smapper = new StackMapper();
        
        smapper.assign(sb, VarType.INTEGER, "0");
        
        smapper.assign(sb, VarType.REFERENCE, "arr");
        smapper.assign(sb, VarType.INTEGER, "33");
        smapper.replace(sb, VarType.INTEGER, "@2[@1]", 
            smapper.popI(), smapper.getA(0)
        );

        smapper.replace(sb, VarType.INTEGER, "(@1) + (@2)", 
            smapper.getI(1), smapper.popI()
        );
        
        smapper.assign(sb, VarType.REFERENCE, "arr");
        smapper.assign(sb, VarType.INTEGER, "22");
        smapper.replace(sb, VarType.INTEGER, "@2[@1]", 
            smapper.popI(), smapper.getA(0)
        );
        
        smapper.replace(sb, VarType.INTEGER, "(@1) + (@2)", 
            smapper.getI(1), smapper.popI()
        );
        
        smapper.flush(sb);
        
        ScriptEngine em = TestVM.createEngine();
        Object ret = em.eval("var arr= []; arr[33] = 40; arr[22] = 2; " + sb + "; stI0;");
        
        assertTrue(ret instanceof Number, "Result is number: " + ret);
        assertEquals(((Number)ret).intValue(), 42, "Right value");
    }
    
}
