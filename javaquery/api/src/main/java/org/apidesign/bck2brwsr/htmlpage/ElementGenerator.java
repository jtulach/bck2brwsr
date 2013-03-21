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
package org.apidesign.bck2brwsr.htmlpage;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelProvider;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;

/**
 *
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public class ElementGenerator {

    static final Map<String, String> NAMING_EXCEPTIONS = new HashMap<String, String>() {
        {
            put("img", "Image");
            put("class", "Clazz");
        }
    };
    
    static final String javaKeywords[] = {
        "abstract", "assert", "boolean", "break", "byte", "case",
        "catch", "char", "class", "const", "continue", "default", 
        "do", "double", "else", "extends", "false", "final", "finally", 
        "float", "for", "goto", "if", "implements", "import", 
        "instanceof", "int", "interface", "long", "native", "new",
        "null", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "true", "try", "void", "volatile", "while"
    };
    
    private static Map<String, String> elements = new HashMap<String, String>();
    private final ProcessingEnvironment processingEnv;
    private HtmlModel model = null;

    ElementGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    String getType(String pkg, String tag, Element e) {
        String className = elements.get(tag);
        if (className == null) {
            className = createClass(pkg, tag, e);
            elements.put(tag, className);
        }
        return className;
    }

    private String createClass(String pkg, String tag, Element e) {
        String className = className(tag);
        Writer w;
        try {
            FileObject java = processingEnv.getFiler().createSourceFile(pkg + '.' + className, e);
            w = new OutputStreamWriter(java.openOutputStream());
            try {
                w.append("package " + pkg + ";\n\n");
                w.append("import org.apidesign.bck2brwsr.htmlpage.api.*;\n");
                PredefinedFields.appendImports(w, tag);
                w.append("\n");
                
                w.append("class ").append(className).append(" extends Element {\n\n");
                w.append("    public ").append(className).append("(String id) {\n");
                w.append("        super(id);\n");
                w.append("    }\n\n");
                for (Entry<String, String> entry : getAttributes(tag).entrySet()) {
                    String attrName = entry.getKey();
                    String attrType = entry.getValue();
                    // getter
                    w.append("    public ").append(attrType).append(" ")
                            .append("get").append(className(attrName)).append("() {\n");
                    w.append("        return (").append(attrType).append(")getAttribute(\"")
                            .append(attrName).append("\");\n");
                    w.append("    }\n\n");
                    // setter
                    w.append("    public void ")
                            .append("set").append(className(attrName)).append("(")
                            .append(attrType).append(" ").append(attributeName(attrName)).append(") {\n");
                    w.append("        setAttribute(\"").append(attrName).append("\", ").append(attributeName(attrName)).append(");\n");
                    w.append("    }\n\n");
                }
                PredefinedFields.appendFields(w, tag);
                w.append("}\n");
            } finally {
                w.close();
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't create " + className + ".java", e);
            return null;
        }
        return className;
    }

    Map<String, String> getAttributes(String tagName) {
        Map<String, String> result = new HashMap<String, String>();

        if (model == null) {
            // HtmlModelProvider modelProvider = Lookup.getDefault().lookup(HtmlModelProvider.class);
            ServiceLoader<HtmlModelProvider> hmpLoader = 
                    ServiceLoader.load(HtmlModelProvider.class, this.getClass().getClassLoader());
            for (HtmlModelProvider htmlModelProvider : hmpLoader) {
                model = htmlModelProvider.getModel(HtmlVersion.HTML5);
                if (model != null) {
                    break;
                }
            }
        }
        
        if (model == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                    "HTML 5 model provider was not found on classpath");
            return Collections.emptyMap();
        }
        HtmlTag tag = model.getTag(tagName);
        for (HtmlTagAttribute attr : tag.getAttributes()) {
            String name = attr.getName();
            String type = Attributes.TYPES.get(name);
            if (type != null) {
                result.put(name, type);
            }
        }
        
        return result;
    }

    private String className(String s) {
        if (s.length() == 0) {
            return s;
        }
        String name = NAMING_EXCEPTIONS.get(s.toLowerCase());
        if (name == null) {
            name = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
        return name;
    }

    private String attributeName(String s) {
        if (Arrays.binarySearch(javaKeywords, s) >= 0) {
            return String.format("%sAttr", s);
        }
        return s;
    }
    
}
