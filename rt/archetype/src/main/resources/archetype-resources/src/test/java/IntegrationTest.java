package ${package};

import org.apidesign.bck2brwsr.htmlpage.api.OnEvent;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/** Sometimes it is useful to run tests inside of the real browser. 
 * To do that just annotate your method with {@link org.apidesign.bck2brwsr.vmtest.BrwsrTest}
 * and that is it. If your code references elements on the HTML page,
 * you can pass in an {@link org.apidesign.bck2brwsr.vmtest.HtmlFragment} which
 * will be made available on the page before your test starts.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IntegrationTest {
    
    /** Write to testing code here. Use <code>assert</code> (but not TestNG's
     * Assert, as TestNG is not compiled with target 1.6 yet).
     */
    @HtmlFragment(
        "<h1 data-bind=\"text: helloMessage\">Loading Bck2Brwsr's Hello World...</h1>\n" +
        "Your name: <input id='input' data-bind=\"value: name, valueUpdate: 'afterkeydown'\"></input>\n" +
        "<button id=\"hello\">Say Hello!</button>\n" +
        "<p>\n" +
        "    <canvas id=\"canvas\" width=\"300\" height=\"50\"></canvas>\n" +
        "</p>\n"
    )
    @BrwsrTest
    public void modifyValueAssertChangeInModel() {
        Index m = new Index();
        m.setName("Joe Hacker");
        m.applyBindings();
        assert "Joe Hacker".equals(m.input.getValue()) : "Value is really Joe Hacker: " + m.input.getValue();
        m.input.setValue("Happy Joe");
        m.triggerEvent(m.input, OnEvent.CHANGE);
        assert "Happy Joe".equals(m.getName()) : "Name property updated to Happy Joe: " + m.getName();
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(IntegrationTest.class);
    }
    
}
