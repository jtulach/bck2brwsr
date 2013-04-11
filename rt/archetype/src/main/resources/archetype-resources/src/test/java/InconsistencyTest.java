package ${package};

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/** Bck2brwsr cares about compatibility with real Java. Whatever API is
 * supported by bck2brwsr, it needs to behave the same way as when running
 * in HotSpot VM. 
 * <p>
 * There can be bugs, however. To help us fix them, we kindly ask you to 
 * write an "inconsistency" test. A test that compares behavior of the API
 * between real VM and bck2brwsr VM. This class is skeleton of such test.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InconsistencyTest {
    /** A method to demonstrate inconsistency between bck2brwsr and HotSpot.
     * Make calls to an API that behaves strangely, return some result at
     * the end. No need to use any <code>assert</code>.
     * 
     * @return value to compare between HotSpot and bck2brwsr
     */
    @Compare
    public int checkStringHashCode() throws Exception {
        return "Is string hashCode the same?".hashCode();
    }

    /** Factory method that creates a three tests for each method annotated with
     * {@link org.apidesign.bck2brwsr.vmtest.Compare}. One executes the code in
     * HotSpot, one in Rhino and the last one compares the results.
     * 
     * @see org.apidesign.bck2brwsr.vmtest.VMTest
     */
    @Factory
    public static Object[] create() {
        return VMTest.create(InconsistencyTest.class);
    }
    
}
