/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.tck;

import java.util.Timer;
import java.util.TimerTask;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TimerTest {
    int miss;
    int exec;
    
    public TimerTest() {
    }
    
    @BrwsrTest public void scheduleTick() throws Exception {
        Timer t = new Timer("MyTest");
        class TT extends TimerTask {
            @Override
            public void run() {
                exec++;
            }
        }
        TT task = new TT();
        t.schedule(task, 15);
        
        if (exec == 0) {
            miss++;
            throw new InterruptedException();
        }
        
        assert exec == 1 : "One exec: " + exec;
        assert miss == 1 : "One miss: " + miss;
    }
    
    @BrwsrTest public void repeatedTicks() throws Exception {
        Timer t = new Timer("MyTest");
        class TT extends TimerTask {
            @Override
            public void run() {
                exec++;
            }
        }
        TT task = new TT();
        t.scheduleAtFixedRate(task, 15, 10);
        
        if (exec != 2) {
            miss++;
            throw new InterruptedException();
        }
        
        assert exec == 2 : "Two execs: " + exec;
        assert miss == 2 : "Two misses: " + miss;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(TimerTest.class);
    }
    
}
