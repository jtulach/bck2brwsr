package org.apidesign.vm4brwsr;

/** Checks if everything works OK, when we switch the
 * order of loaded classes.
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstanceSubTest extends InstanceTest {

    @Override
    protected String startCompilationWith() {
        return "org/apidesign/vm4brwsr/InstanceSub";
    }
    
}
