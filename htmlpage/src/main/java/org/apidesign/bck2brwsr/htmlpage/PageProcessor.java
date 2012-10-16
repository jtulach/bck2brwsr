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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apidesign.bck2brwsr.htmlpage.api.OnClick;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.openide.util.lookup.ServiceProvider;

/** Annotation processor to process an XHTML page and generate appropriate 
 * "id" file.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes({
    "org.apidesign.bck2brwsr.htmlpage.api.Page",
    "org.apidesign.bck2brwsr.htmlpage.api.OnClick"
})
public final class PageProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(Page.class)) {
            Page p = e.getAnnotation(Page.class);
            PackageElement pe = (PackageElement)e.getEnclosingElement();
            String pkg = pe.getQualifiedName().toString();
            
            ProcessPage pp;
            try {
                InputStream is = openStream(pkg, p.xhtml());
                pp = ProcessPage.readPage(is);
                is.close();
            } catch (IOException iOException) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't read " + p.xhtml(), e);
                return false;
            }
            Writer w;
            try {
                FileObject java = processingEnv.getFiler().createSourceFile(pkg + '.' + p.name(), e);
                w = new OutputStreamWriter(java.openOutputStream());
                try {
                    w.append("package " + pkg + ";\n");
                    w.append("import org.apidesign.bck2brwsr.htmlpage.api.*;\n");
                    w.append("class ").append(p.name()).append(" {\n");
                    for (String id : pp.ids()) {
                        String tag = pp.tagNameForId(id);
                        String type = type(tag);
                        w.append("  ").append("public static final ").
                            append(type).append(' ').append(cnstnt(id)).append(" = new ").
                            append(type).append("(\"").append(id).append("\");\n");
                    }
                    w.append("  static {\n");
                    if (!initializeOnClick(pe, w, pp)) {
                        return false;
                    }
                    w.append("  }\n");
                    w.append("}\n");
                } finally {
                    w.close();
                }
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't create " + p.name() + ".java", e);
                return false;
            }
        }
        return true;
    }

    private InputStream openStream(String pkg, String name) throws IOException {
        FileObject fo = processingEnv.getFiler().getResource(
            StandardLocation.SOURCE_PATH, pkg, name);
        try {
            return fo.openInputStream();
        } catch (IOException ex) {
            return processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, pkg, name).openInputStream();
        }
    }

    private static String type(String tag) {
        if (tag.equals("title")) {
            return "Title";
        }
        if (tag.equals("button")) {
            return "Button";
        }
        if (tag.equals("input")) {
            return "Input";
        }
        return "Element";
    }

    private static String cnstnt(String id) {
        return id.toUpperCase(Locale.ENGLISH).replace('.', '_');
    }

    private boolean initializeOnClick(PackageElement pe, Writer w, ProcessPage pp) throws IOException {
        for (Element clazz : pe.getEnclosedElements()) {
            if (clazz.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement type = (TypeElement)clazz;
            for (Element method : clazz.getEnclosedElements()) {
                OnClick oc = method.getAnnotation(OnClick.class);
                if (oc != null) {
                    if (pp.tagNameForId(oc.id()) == null) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "id = " + oc.id() + " does not exist in the HTML page. Found only " + pp.ids(), method);
                        return false;
                    }
                    ExecutableElement ee = (ExecutableElement)method;
                    if (!ee.getParameters().isEmpty()) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@OnClose method can't take arguments", ee);
                        return false;
                    }
                    if (!ee.getModifiers().contains(Modifier.STATIC)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@OnClose method has to be static", ee);
                        return false;
                    }
                    if (ee.getModifiers().contains(Modifier.PRIVATE)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@OnClose method can't be private", ee);
                        return false;
                    }
                    w.append("  ").append(cnstnt(oc.id())).
                        append(".addOnClick(new Runnable() { public void run() {\n");
                    w.append("    ").append(type.getSimpleName().toString()).
                        append('.').append(ee.getSimpleName()).append("();\n");
                    w.append("  }});\n");
                }
            }
        }
        return true;
    }
}
