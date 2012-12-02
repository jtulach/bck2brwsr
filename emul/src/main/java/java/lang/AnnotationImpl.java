/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package java.lang;

import java.lang.annotation.Annotation;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class AnnotationImpl implements Annotation {
    public Class<? extends Annotation> annotationType() {
        return getClass();
    }

    @JavaScriptBody(args = { "a", "n", "values" }, body =
          "var v = values;"
        + "for (p in values) {"
        + "  a[p] = function() { return v[p]; }"
        + "}"
        + "a['$instOf_' + n] = true;"
        + "return a;"
    )
    private static <T extends Annotation> T create(AnnotationImpl a, String n, Object values) {
        return null;
    }
    static <T extends Annotation> T create(Class<T> annoClass, Object values) {
        return create(new AnnotationImpl(), annoClass.getName().replace('.', '_'), values);
    }

    static Annotation[] create(Object anno) {
        String[] names = findNames(anno);
        Annotation[] ret = new Annotation[names.length];
        for (int i = 0; i < names.length; i++) {
            String n = names[i].substring(1, names[i].length() - 1).replace('/', '_');
            ret[i] = create(new AnnotationImpl(), n, findData(anno, names[i]));
        }
        return ret;
    }
    @JavaScriptBody(args = "anno", body =
          "var arr = new Array();"
        + "for (p in anno) {"
        + "  arr.push(p);"
        + "}"
        + "return arr;"
    )
    private static String[] findNames(Object anno) {
        throw new UnsupportedOperationException();
    }

    @JavaScriptBody(args={ "anno", "p"}, body="return anno[p];")
    private static Object findData(Object anno, String p) {
        throw new UnsupportedOperationException();
    }
}
