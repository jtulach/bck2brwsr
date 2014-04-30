package ${package};

import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/** Sometimes it is useful to run tests inside of the real browser. 
 * To do that just annotate your method with {@link org.apidesign.bck2brwsr.vmtest.BrwsrTest}
 * and that is it. If your code references elements on the HTML page,
 * you can pass in an {@link org.apidesign.bck2brwsr.vmtest.HtmlFragment} which
 * will be made available on the page before your test starts.
 */
public class IntegrationTest {
    
    /** Write to testing code here. Use <code>assert</code> (but not TestNG's
     * Assert, as TestNG is not compiled with target 1.6 yet).
     */
    @HtmlFragment(
        "<h1>Put this snippet on the HTML page</h1>\n"
    )
    @BrwsrTest
    public void runThisTestInABrowser() {
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(IntegrationTest.class);
    }
    
}
