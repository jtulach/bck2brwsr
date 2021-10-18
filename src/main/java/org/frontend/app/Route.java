package org.frontend.app;

import net.java.html.js.JavaScriptBody;

final class Route {
    private Route() {
    }

    public static String getLocation(String attr) {
        String hash = getLocationImpl(attr);
        if (hash.startsWith("#")) {
            return hash.substring(1);
        } else {
            return hash;
        }
    }

    public static void setLocation(String attr, String value) {
        setLocationImpl(attr, value);
    }

    public static String sanitize(String text) {
        StringBuilder sb = new StringBuilder();
        for (int at = 0; at < text.length(); at++) {
            char ch = Character.toLowerCase(text.charAt(at));
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @JavaScriptBody(args = { "attr" }, body
            = "var w = window || document;\n"
            + "return w.location[attr];\n"
    )
    private static native String getLocationImpl(String attr);

    @JavaScriptBody(args = { "attr", "value" }, body
            = "var w = window || document;\n"
            + "w.location[attr] = value;\n"
    )
    private static native void setLocationImpl(String attr, String value);

}
