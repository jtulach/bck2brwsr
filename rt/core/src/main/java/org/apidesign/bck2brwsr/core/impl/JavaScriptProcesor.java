/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.core.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(processByteCode = false, resource="")
@ServiceProvider(service = Processor.class)
public final class JavaScriptProcesor extends AbstractProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(JavaScriptBody.class.getName());
        return set;
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Messager msg = processingEnv.getMessager();
        for (Element e : roundEnv.getElementsAnnotatedWith(JavaScriptBody.class)) {
            if (e.getKind() != ElementKind.METHOD && e.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            List<? extends VariableElement> params = ee.getParameters();
            
            JavaScriptBody jsb = e.getAnnotation(JavaScriptBody.class);
            if (jsb == null) {
                continue;
            }
            String[] arr = jsb.args();
            if (params.size() != arr.length) {
                msg.printMessage(Diagnostic.Kind.ERROR, "Number of args arguments does not match real arguments!", e);
            }
            if (ee.getReturnType().getKind() == TypeKind.LONG) {
                msg.printMessage(Diagnostic.Kind.WARNING, "Don't return long. Return double and convert it to long in Java code.", e);
            }
        }
        return true;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element e, 
        AnnotationMirror annotation, ExecutableElement member, String userText
    ) {
        StringBuilder sb = new StringBuilder();
        if (e.getKind() == ElementKind.METHOD && member.getSimpleName().contentEquals("args")) {
            ExecutableElement ee = (ExecutableElement) e;
            String sep = "";
            sb.append("{ ");
            for (VariableElement ve : ee.getParameters()) {
                sb.append(sep).append('"').append(ve.getSimpleName())
                    .append('"');
                sep = ", ";
            }
            sb.append(" }");
            return Collections.nCopies(1, Completions.of(sb.toString()));
        }
        return null;
    }

    
}
