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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.On;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import org.openide.util.lookup.ServiceProvider;

/** Annotation processor to process an XHTML page and generate appropriate 
 * "id" file.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes({
    "org.apidesign.bck2brwsr.htmlpage.api.Page",
    "org.apidesign.bck2brwsr.htmlpage.api.On"
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
            String className = p.className();
            if (className.isEmpty()) {
                int indx = p.xhtml().indexOf('.');
                className = p.xhtml().substring(0, indx);
            }
            try {
                FileObject java = processingEnv.getFiler().createSourceFile(pkg + '.' + className, e);
                w = new OutputStreamWriter(java.openOutputStream());
                try {
                    w.append("package " + pkg + ";\n");
                    w.append("import org.apidesign.bck2brwsr.htmlpage.api.*;\n");
                    w.append("final class ").append(className).append(" {\n");
                    w.append("  private static boolean locked;\n");
                    w.append("  public ").append(className).append("() {\n");
                    if (!initializeOnClick((TypeElement) e, w, pp)) {
                        return false;
                    }
                    w.append("  }\n");
                    for (String id : pp.ids()) {
                        String tag = pp.tagNameForId(id);
                        String type = type(tag);
                        w.append("  ").append("public final ").
                            append(type).append(' ').append(cnstnt(id)).append(" = new ").
                            append(type).append("(\"").append(id).append("\");\n");
                    }
                    List<String> propsGetSet = new ArrayList<String>();
                    Map<String,Collection<String>> propsDeps = new HashMap<String, Collection<String>>();
                    generateComputedProperties(w, e.getEnclosedElements(), propsGetSet, propsDeps);
                    generateProperties(w, p.properties(), propsGetSet, propsDeps);
                    w.append("  private org.apidesign.bck2brwsr.htmlpage.Knockout ko;\n");
                    if (!propsGetSet.isEmpty()) {
                        w.write("public " + className + " applyBindings() {\n");
                        w.write("  ko = org.apidesign.bck2brwsr.htmlpage.Knockout.applyBindings(");
                        w.write(className + ".class, this, ");
                        w.write("new String[] {\n");
                        String sep = "";
                        for (String n : propsGetSet) {
                            w.write(sep);
                            if (n == null) {
                                w.write("    null");
                            } else {
                                w.write("    \"" + n + "\"");
                            }
                            sep = ",\n";
                        }
                        w.write("\n  });\n  return this;\n}\n");
                    }
                    w.append("}\n");
                } finally {
                    w.close();
                }
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't create " + className + ".java", e);
                return false;
            }
        }
        return true;
    }

    private InputStream openStream(String pkg, String name) throws IOException {
        try {
            FileObject fo = processingEnv.getFiler().getResource(
                StandardLocation.SOURCE_PATH, pkg, name);
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

    private boolean initializeOnClick(TypeElement type, Writer w, ProcessPage pp) throws IOException {
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        { //for (Element clazz : pe.getEnclosedElements()) {
          //  if (clazz.getKind() != ElementKind.CLASS) {
            //    continue;
           // }
            for (Element method : type.getEnclosedElements()) {
                On oc = method.getAnnotation(On.class);
                if (oc != null) {
                    for (String id : oc.id()) {
                        if (pp.tagNameForId(id) == null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "id = " + id + " does not exist in the HTML page. Found only " + pp.ids(), method);
                            return false;
                        }
                        ExecutableElement ee = (ExecutableElement)method;
                        boolean hasParam;
                        if (ee.getParameters().isEmpty()) {
                            hasParam = false;
                        } else {
                            if (ee.getParameters().size() != 1 || ee.getParameters().get(0).asType() != stringType) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@On method should either have no arguments or one String argument", ee);
                                return false;
                            }
                            hasParam = true;
                        }
                        if (!ee.getModifiers().contains(Modifier.STATIC)) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@On method has to be static", ee);
                            return false;
                        }
                        if (ee.getModifiers().contains(Modifier.PRIVATE)) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@On method can't be private", ee);
                            return false;
                        }
                        w.append("  OnEvent." + oc.event()).append(".of(").append(cnstnt(id)).
                            append(").perform(new Runnable() { public void run() {\n");
                        w.append("    ").append(type.getSimpleName().toString()).
                            append('.').append(ee.getSimpleName()).append("(");
                        if (hasParam) {
                            w.append("\"").append(id).append("\"");
                        }
                        w.append(");\n");
                        w.append("  }});\n");
                    }           
                }
            }
        }
        return true;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
        Element element, AnnotationMirror annotation, 
        ExecutableElement member, String userText
    ) {
        if (!userText.startsWith("\"")) {
            return Collections.emptyList();
        }
        
        Element cls = findClass(element);
        Page p = cls.getAnnotation(Page.class);
        PackageElement pe = (PackageElement) cls.getEnclosingElement();
        String pkg = pe.getQualifiedName().toString();
        ProcessPage pp;
        try {
            InputStream is = openStream(pkg, p.xhtml());
            pp = ProcessPage.readPage(is);
            is.close();
        } catch (IOException iOException) {
            return Collections.emptyList();
        }
        
        List<Completion> cc = new ArrayList<Completion>();
        userText = userText.substring(1);
        for (String id : pp.ids()) {
            if (id.startsWith(userText)) {
                cc.add(Completions.of("\"" + id + "\"", id));
            }
        }
        return cc;
    }
    
    private static Element findClass(Element e) {
        if (e == null) {
            return null;
        }
        Page p = e.getAnnotation(Page.class);
        if (p != null) {
            return e;
        }
        return e.getEnclosingElement();
    }

    private static void generateProperties(
        Writer w, Property[] properties, Collection<String> props,
        Map<String,Collection<String>> deps
    ) throws IOException {
        for (Property p : properties) {
            final String tn = typeName(p);
            String[] gs = toGetSet(p.name(), tn);

            w.write("private " + tn + " prop_" + p.name() + ";\n");
            w.write("public " + tn + " " + gs[0] + "() {\n");
            w.write("  if (locked) throw new IllegalStateException();\n");
            w.write("  return prop_" + p.name() + ";\n");
            w.write("}\n");
            w.write("public void " + gs[1] + "(" + tn + " v) {\n");
            w.write("  if (locked) throw new IllegalStateException();\n");
            w.write("  prop_" + p.name() + " = v;\n");
            w.write("  if (ko != null) {\n");
            w.write("    ko.valueHasMutated(\"" + p.name() + "\");\n");
            final Collection<String> dependants = deps.get(p.name());
            if (dependants != null) {
                for (String depProp : dependants) {
                    w.write("    ko.valueHasMutated(\"" + depProp + "\");\n");
                }
            }
            w.write("  }\n");
            w.write("}\n");
            
            props.add(p.name());
            props.add(gs[2]);
            props.add(gs[3]);
        }
    }

    private boolean generateComputedProperties(
        Writer w, Collection<? extends Element> arr, Collection<String> props,
        Map<String,Collection<String>> deps
    ) throws IOException {
        for (Element e : arr) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (e.getAnnotation(ComputedProperty.class) == null) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            final String tn = ee.getReturnType().toString();
            final String sn = ee.getSimpleName().toString();
            String[] gs = toGetSet(sn, tn);
            
            w.write("public " + tn + " " + gs[0] + "() {\n");
            w.write("  if (locked) throw new IllegalStateException();\n");
            int arg = 0;
            for (VariableElement pe : ee.getParameters()) {
                final String dn = pe.getSimpleName().toString();
                final String dt = pe.asType().toString();
                String[] call = toGetSet(dn, dt);
                w.write("  " + dt + " arg" + (++arg) + " = ");
                w.write(call[0] + "();\n");
                
                Collection<String> depends = deps.get(dn);
                if (depends == null) {
                    depends = new LinkedHashSet<String>();
                    deps.put(dn, depends);
                }
                depends.add(sn);
            }
            w.write("  try {\n");
            w.write("    locked = true;\n");
            w.write("    return " + e.getEnclosingElement().getSimpleName() + '.' + e.getSimpleName() + "(");
            String sep = "";
            for (int i = 1; i <= arg; i++) {
                w.write(sep);
                w.write("arg" + i);
                sep = ", ";
            }
            w.write(");\n");
            w.write("  } finally {\n");
            w.write("    locked = false;\n");
            w.write("  }\n");
            w.write("}\n");
            
            props.add(e.getSimpleName().toString());
            props.add(gs[2]);
            props.add(null);
        }
        
        return true;
    }

    private static String[] toGetSet(String name, String type) {
        String n = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String bck2brwsrType = "L" + type.replace('.', '_') + "_2";
        if ("int".equals(type)) {
            bck2brwsrType = "I";
        }
        if ("double".equals(type)) {
            bck2brwsrType = "D";
        }
        String pref = "get";
        if ("boolean".equals(type)) {
            pref = "is";
            bck2brwsrType = "Z";
        }
        final String nu = n.replace('.', '_');
        return new String[]{
            pref + n, 
            "set" + n, 
            pref + nu + "__" + bck2brwsrType,
            "set" + nu + "__V" + bck2brwsrType
        };
    }

    private static String typeName(Property p) {
        try {
            return p.type().getName();
        } catch (MirroredTypeException ex) {
            return ex.getTypeMirror().toString();
        }
    }
}
