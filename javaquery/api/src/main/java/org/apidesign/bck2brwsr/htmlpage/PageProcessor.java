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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.Model;
import org.apidesign.bck2brwsr.htmlpage.api.On;
import org.apidesign.bck2brwsr.htmlpage.api.OnFunction;
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
    "org.apidesign.bck2brwsr.htmlpage.api.Model",
    "org.apidesign.bck2brwsr.htmlpage.api.Page",
    "org.apidesign.bck2brwsr.htmlpage.api.OnFunction",
    "org.apidesign.bck2brwsr.htmlpage.api.On"
})
public final class PageProcessor extends AbstractProcessor {
    private final Map<Element,String> models = new WeakHashMap<>();
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean ok = true;
        for (Element e : roundEnv.getElementsAnnotatedWith(Model.class)) {
            if (!processModel(e)) {
                ok = false;
            }
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Page.class)) {
            if (!processPage(e)) {
                ok = false;
            }
        }
        if (roundEnv.processingOver()) {
            models.clear();
        }
        return ok;
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
    
    private boolean processModel(Element e) {
        boolean ok = true;
        Model m = e.getAnnotation(Model.class);
        if (m == null) {
            return true;
        }
        String pkg = findPkgName(e);
        Writer w;
        String className = m.className();
        try {
            StringWriter body = new StringWriter();
            List<String> propsGetSet = new ArrayList<>();
            List<String> functions = new ArrayList<>();
            Map<String, Collection<String>> propsDeps = new HashMap<>();
            if (!generateComputedProperties(body, m.properties(), e.getEnclosedElements(), propsGetSet, propsDeps)) {
                ok = false;
            }
            if (!generateProperties(e, body, m.properties(), propsGetSet, propsDeps)) {
                ok = false;
            }
            if (!generateFunctions(e, body, className, e.getEnclosedElements(), functions)) {
                ok = false;
            }
            FileObject java = processingEnv.getFiler().createSourceFile(pkg + '.' + className, e);
            w = new OutputStreamWriter(java.openOutputStream());
            try {
                w.append("package " + pkg + ";\n");
                w.append("import org.apidesign.bck2brwsr.htmlpage.api.*;\n");
                w.append("import org.apidesign.bck2brwsr.htmlpage.KOList;\n");
                w.append("import org.apidesign.bck2brwsr.core.JavaScriptOnly;\n");
                w.append("final class ").append(className).append(" {\n");
                w.append("  private Object json;\n");
                w.append("  private boolean locked;\n");
                w.append("  private org.apidesign.bck2brwsr.htmlpage.Knockout ko;\n");
                w.append(body.toString());
                w.append("  private static Class<" + inPckName(e) + "> modelFor() { return null; }\n");
                w.append("  public ").append(className).append("() {\n");
                w.append("    ko = org.apidesign.bck2brwsr.htmlpage.Knockout.applyBindings(this, ");
                writeStringArray(propsGetSet, w);
                w.append(", ");
                writeStringArray(functions, w);
                w.append("    );\n");
                w.append("  };\n");
                writeToString(m.properties(), w);
                w.append("}\n");
            } finally {
                w.close();
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't create " + className + ".java", e);
            return false;
        }
        return ok;
    }
    
    private boolean processPage(Element e) {
        boolean ok = true;
        Page p = e.getAnnotation(Page.class);
        if (p == null) {
            return true;
        }
        String pkg = findPkgName(e);

        ProcessPage pp;
        try (InputStream is = openStream(pkg, p.xhtml())) {
            pp = ProcessPage.readPage(is);
            is.close();
        } catch (IOException iOException) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't read " + p.xhtml() + " as " + iOException.getMessage(), e);
            ok = false;
            pp = null;
        }
        Writer w;
        String className = p.className();
        if (className.isEmpty()) {
            int indx = p.xhtml().indexOf('.');
            className = p.xhtml().substring(0, indx);
        }
        try {
            StringWriter body = new StringWriter();
            List<String> propsGetSet = new ArrayList<>();
            List<String> functions = new ArrayList<>();
            Map<String, Collection<String>> propsDeps = new HashMap<>();
            if (!generateComputedProperties(body, p.properties(), e.getEnclosedElements(), propsGetSet, propsDeps)) {
                ok = false;
            }
            if (!generateProperties(e, body, p.properties(), propsGetSet, propsDeps)) {
                ok = false;
            }
            if (!generateFunctions(e, body, className, e.getEnclosedElements(), functions)) {
                ok = false;
            }
            
            FileObject java = processingEnv.getFiler().createSourceFile(pkg + '.' + className, e);
            w = new OutputStreamWriter(java.openOutputStream());
            try {
                w.append("package " + pkg + ";\n");
                w.append("import org.apidesign.bck2brwsr.htmlpage.api.*;\n");
                w.append("import org.apidesign.bck2brwsr.htmlpage.KOList;\n");
                w.append("final class ").append(className).append(" {\n");
                w.append("  private boolean locked;\n");
                if (!initializeOnClick(className, (TypeElement) e, w, pp)) {
                    ok = false;
                } else {
                    if (pp != null) for (String id : pp.ids()) {
                        String tag = pp.tagNameForId(id);
                        String type = type(tag);
                        w.append("  ").append("public final ").
                            append(type).append(' ').append(cnstnt(id)).append(" = new ").
                            append(type).append("(\"").append(id).append("\");\n");
                    }
                }
                w.append("  private org.apidesign.bck2brwsr.htmlpage.Knockout ko;\n");
                w.append(body.toString());
                if (!propsGetSet.isEmpty()) {
                    w.write("public " + className + " applyBindings() {\n");
                    w.write("  ko = org.apidesign.bck2brwsr.htmlpage.Knockout.applyBindings(");
                    w.write(className + ".class, this, ");
                    writeStringArray(propsGetSet, w);
                    w.append(", ");
                    writeStringArray(functions, w);
                    w.write(");\n  return this;\n}\n");

                    w.write("public void triggerEvent(Element e, OnEvent ev) {\n");
                    w.write("  org.apidesign.bck2brwsr.htmlpage.Knockout.triggerEvent(e.getId(), ev.getElementPropertyName());\n");
                    w.write("}\n");
                }
                w.append("}\n");
            } finally {
                w.close();
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't create " + className + ".java", e);
            return false;
        }
        return ok;
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
        if (tag.equals("canvas")) {
            return "Canvas";
        }
        if (tag.equals("img")) {
            return "Image";
        }
        return "Element";
    }

    private static String cnstnt(String id) {
        return id.replace('.', '_').replace('-', '_');
    }

    private boolean initializeOnClick(
        String className, TypeElement type, Writer w, ProcessPage pp
    ) throws IOException {
        boolean ok = true;
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        { //for (Element clazz : pe.getEnclosedElements()) {
          //  if (clazz.getKind() != ElementKind.CLASS) {
            //    continue;
           // }
            w.append("  public ").append(className).append("() {\n");
            StringBuilder dispatch = new StringBuilder();
            int dispatchCnt = 0;
            for (Element method : type.getEnclosedElements()) {
                On oc = method.getAnnotation(On.class);
                if (oc != null) {
                    for (String id : oc.id()) {
                        if (pp == null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "id = " + id + " not found in HTML page.");
                            ok = false;
                            continue;
                        }
                        if (pp.tagNameForId(id) == null) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "id = " + id + " does not exist in the HTML page. Found only " + pp.ids(), method);
                            ok = false;
                            continue;
                        }
                        ExecutableElement ee = (ExecutableElement)method;
                        CharSequence params = wrapParams(ee, id, className, "ev", null);
                        if (!ee.getModifiers().contains(Modifier.STATIC)) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@On method has to be static", ee);
                            ok = false;
                            continue;
                        }
                        if (ee.getModifiers().contains(Modifier.PRIVATE)) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@On method can't be private", ee);
                            ok = false;
                            continue;
                        }
                        w.append("  OnEvent." + oc.event()).append(".of(").append(cnstnt(id)).
                            append(").perform(new OnDispatch(" + dispatchCnt + "));\n");

                        dispatch.
                            append("      case ").append(dispatchCnt).append(": ").
                            append(type.getSimpleName().toString()).
                            append('.').append(ee.getSimpleName()).append("(").
                            append(params).
                            append("); break;\n");
                        
                        dispatchCnt++;
                    }
                }
            }
            w.append("  }\n");
            if (dispatchCnt > 0) {
                w.append("class OnDispatch implements OnHandler {\n");
                w.append("  private final int dispatch;\n");
                w.append("  OnDispatch(int d) { dispatch = d; }\n");
                w.append("  public void onEvent(Object ev) {\n");
                w.append("    switch (dispatch) {\n");
                w.append(dispatch);
                w.append("    }\n");
                w.append("  }\n");
                w.append("}\n");
            }
            

        }
        return ok;
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
        String pkg = findPkgName(cls);
        ProcessPage pp;
        try {
            InputStream is = openStream(pkg, p.xhtml());
            pp = ProcessPage.readPage(is);
            is.close();
        } catch (IOException iOException) {
            return Collections.emptyList();
        }
        
        List<Completion> cc = new ArrayList<>();
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

    private boolean generateProperties(
        Element where,
        Writer w, Property[] properties,
        Collection<String> props, Map<String,Collection<String>> deps
    ) throws IOException {
        boolean ok = true;
        for (Property p : properties) {
            final String tn;
            tn = typeName(where, p);
            String[] gs = toGetSet(p.name(), tn, p.array());

            if (p.array()) {
                w.write("private KOList<" + tn + "> prop_" + p.name() + " = new KOList<" + tn + ">(\""
                    + p.name() + "\"");
                final Collection<String> dependants = deps.get(p.name());
                if (dependants != null) {
                    for (String depProp : dependants) {
                        w.write(", ");
                        w.write('\"');
                        w.write(depProp);
                        w.write('\"');
                    }
                }
                w.write(");\n");
                w.write("public java.util.List<" + tn + "> " + gs[0] + "() {\n");
                w.write("  if (locked) throw new IllegalStateException();\n");
                w.write("  prop_" + p.name() + ".assign(ko);\n");
                w.write("  return prop_" + p.name() + ";\n");
                w.write("}\n");
            } else {
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
            }
            
            props.add(p.name());
            props.add(gs[2]);
            props.add(gs[3]);
            props.add(gs[0]);
        }
        return ok;
    }

    private boolean generateComputedProperties(
        Writer w, Property[] fixedProps,
        Collection<? extends Element> arr, Collection<String> props,
        Map<String,Collection<String>> deps
    ) throws IOException {
        boolean ok = true;
        for (Element e : arr) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (e.getAnnotation(ComputedProperty.class) == null) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            final TypeMirror rt = ee.getReturnType();
            final Types tu = processingEnv.getTypeUtils();
            TypeMirror ert = tu.erasure(rt);
            String tn = ert.toString();
            boolean array = false;
            if (tn.equals("java.util.List")) {
                array = true;
            }
            
            final String sn = ee.getSimpleName().toString();
            String[] gs = toGetSet(sn, tn, array);
            
            w.write("public " + tn + " " + gs[0] + "() {\n");
            w.write("  if (locked) throw new IllegalStateException();\n");
            int arg = 0;
            for (VariableElement pe : ee.getParameters()) {
                final String dn = pe.getSimpleName().toString();
                
                if (!verifyPropName(pe, dn, fixedProps)) {
                    ok = false;
                }
                
                final String dt = pe.asType().toString();
                String[] call = toGetSet(dn, dt, false);
                w.write("  " + dt + " arg" + (++arg) + " = ");
                w.write(call[0] + "();\n");
                
                Collection<String> depends = deps.get(dn);
                if (depends == null) {
                    depends = new LinkedHashSet<>();
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
            props.add(gs[0]);
        }
        
        return ok;
    }

    private static String[] toGetSet(String name, String type, boolean array) {
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
        if (array) {
            return new String[] { 
                "get" + n,
                null,
                "get" + nu + "__Ljava_util_List_2",
                null
            };
        }
        return new String[]{
            pref + n, 
            "set" + n, 
            pref + nu + "__" + bck2brwsrType,
            "set" + nu + "__V" + bck2brwsrType
        };
    }

    private String typeName(Element where, Property p) {
        String ret;
        boolean isModel = false;
        boolean isEnum = false;
        try {
            ret = p.type().getName();
        } catch (MirroredTypeException ex) {
            TypeMirror tm = processingEnv.getTypeUtils().erasure(ex.getTypeMirror());
            final Element e = processingEnv.getTypeUtils().asElement(tm);
            final Model m = e == null ? null : e.getAnnotation(Model.class);
            if (m != null) {
                ret = findPkgName(e) + '.' + m.className();
                isModel = true;
                models.put(e, m.className());
            } else {
                ret = tm.toString();
            }
            TypeMirror enm = processingEnv.getElementUtils().getTypeElement("java.lang.Enum").asType();
            enm = processingEnv.getTypeUtils().erasure(enm);
            isEnum = processingEnv.getTypeUtils().isSubtype(tm, enm);
        }
        if (p.array()) {
            String bt = findBoxedType(ret);
            if (bt != null) {
                return bt;
            }
        }
        if (!isModel && !"java.lang.String".equals(ret) && !isEnum) {
            String bt = findBoxedType(ret);
            if (bt == null) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, 
                    "Only primitive types supported in the mapping. Not " + ret,
                    where
                );
            }
        }
        return ret;
    }
    
    private static String findBoxedType(String ret) {
        if (ret.equals("boolean")) {
            return Boolean.class.getName();
        }
        if (ret.equals("byte")) {
            return Byte.class.getName();
        }
        if (ret.equals("short")) {
            return Short.class.getName();
        }
        if (ret.equals("char")) {
            return Character.class.getName();
        }
        if (ret.equals("int")) {
            return Integer.class.getName();
        }
        if (ret.equals("long")) {
            return Long.class.getName();
        }
        if (ret.equals("float")) {
            return Float.class.getName();
        }
        if (ret.equals("double")) {
            return Double.class.getName();
        }
        return null;
    }

    private boolean verifyPropName(Element e, String propName, Property[] existingProps) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Property property : existingProps) {
            if (property.name().equals(propName)) {
                return true;
            }
            sb.append(sep);
            sb.append('"');
            sb.append(property.name());
            sb.append('"');
            sep = ", ";
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            propName + " is not one of known properties: " + sb
            , e
        );
        return false;
    }

    private static String findPkgName(Element e) {
        for (;;) {
            if (e.getKind() == ElementKind.PACKAGE) {
                return ((PackageElement)e).getQualifiedName().toString();
            }
            e = e.getEnclosingElement();
        }
    }

    private boolean generateFunctions(
        Element clazz, StringWriter body, String className, 
        List<? extends Element> enclosedElements, List<String> functions
    ) {
        for (Element m : enclosedElements) {
            if (m.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement e = (ExecutableElement)m;
            OnFunction onF = e.getAnnotation(OnFunction.class);
            if (onF == null) {
                continue;
            }
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "@OnFunction method needs to be static", e
                );
                return false;
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "@OnFunction method cannot be private", e
                );
                return false;
            }
            if (e.getReturnType().getKind() != TypeKind.VOID) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "@OnFunction method should return void", e
                );
                return false;
            }
            String n = e.getSimpleName().toString();
            body.append("private void ").append(n).append("(Object data, Object ev) {\n");
            body.append("  ").append(clazz.getSimpleName()).append(".").append(n).append("(");
            body.append(wrapParams(e, null, className, "ev", "data"));
            body.append(");\n");
            body.append("}\n");
            
            functions.add(n);
            functions.add(n + "__VLjava_lang_Object_2Ljava_lang_Object_2");
        }
        return true;
    }

    private CharSequence wrapParams(
        ExecutableElement ee, String id, String className, String evName, String dataName
    ) {
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        StringBuilder params = new StringBuilder();
        boolean first = true;
        for (VariableElement ve : ee.getParameters()) {
            if (!first) {
                params.append(", ");
            }
            first = false;
            String toCall = null;
            if (ve.asType() == stringType) {
                if (ve.getSimpleName().contentEquals("id")) {
                    params.append('"').append(id).append('"');
                    continue;
                }
                toCall = "org.apidesign.bck2brwsr.htmlpage.ConvertTypes.toString(";
            }
            if (ve.asType().getKind() == TypeKind.DOUBLE) {
                toCall = "org.apidesign.bck2brwsr.htmlpage.ConvertTypes.toDouble(";
            }
            if (ve.asType().getKind() == TypeKind.INT) {
                toCall = "org.apidesign.bck2brwsr.htmlpage.ConvertTypes.toInt(";
            }
            if (dataName != null && ve.getSimpleName().contentEquals(dataName) && isModel(ve.asType())) {
                toCall = "org.apidesign.bck2brwsr.htmlpage.ConvertTypes.toModel(" + ve.asType() + ".class, ";
            }

            if (toCall != null) {
                params.append(toCall);
                if (dataName != null && ve.getSimpleName().contentEquals(dataName)) {
                    params.append(dataName);
                    params.append(", null");
                } else {
                    params.append(evName);
                    params.append(", \"");
                    params.append(ve.getSimpleName().toString());
                    params.append("\"");
                }
                params.append(")");
                continue;
            }
            String rn = ve.asType().toString();
            int last = rn.lastIndexOf('.');
            if (last >= 0) {
                rn = rn.substring(last + 1);
            }
            if (rn.equals(className)) {
                params.append(className).append(".this");
                continue;
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                "@On method can only accept String named 'id' or " + className + " arguments",
                ee
            );
        }
        return params;
    }
    
    private boolean isModel(TypeMirror tm) {
        final Element e = processingEnv.getTypeUtils().asElement(tm);
        if (e == null) {
            return false;
        }
        for (Element ch : e.getEnclosedElements()) {
            if (ch.getKind() == ElementKind.METHOD) {
                ExecutableElement ee = (ExecutableElement)ch;
                if (ee.getParameters().isEmpty() && ee.getSimpleName().contentEquals("modelFor")) {
                    return true;
                }
            }
        }
        return models.values().contains(e.getSimpleName().toString());
    }

    private void writeStringArray(List<String> strings, Writer w) throws IOException {
        w.write("new String[] {\n");
        String sep = "";
        for (String n : strings) {
            w.write(sep);
            if (n == null) {
                w.write("    null");
            } else {
                w.write("    \"" + n + "\"");
            }
            sep = ",\n";
        }
        w.write("\n  }");
    }
    
    private void writeToString(Property[] props, Writer w) throws IOException {
        w.write("  public String toString() {\n");
        w.write("    StringBuilder sb = new StringBuilder();\n");
        w.write("    sb.append('{');\n");
        String sep = "";
        for (Property p : props) {
            w.write(sep);
            w.append("    sb.append(\"" + p.name() + ": \");\n");
            w.append("    sb.append(org.apidesign.bck2brwsr.htmlpage.ConvertTypes.toJSON(prop_");
            w.append(p.name()).append("));\n");
            sep =    "    sb.append(',');\n";
        }
        w.write("    sb.append('}');\n");
        w.write("    return sb.toString();\n");
        w.write("  }\n");
    }

    private String inPckName(Element e) {
        StringBuilder sb = new StringBuilder();
        while (e.getKind() != ElementKind.PACKAGE) {
            if (sb.length() == 0) {
                sb.append(e.getSimpleName());
            } else {
                sb.insert(0, '.');
                sb.insert(0, e.getSimpleName());
            }
            e = e.getEnclosingElement();
        }
        return sb.toString();
    }
}
