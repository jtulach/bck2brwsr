/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("org.apidesign.bck2brwsr.vmtest.impl.GenerateZip")
public class GenerateZipProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(GenerateZip.class)) {
            GenerateZip gz = e.getAnnotation(GenerateZip.class);
            if (gz == null) {
                continue;
            }
            PackageElement pe = findPackage(e);
            try {
                generateJar(pe, gz, e);
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, 
                    "Can't generate JAR " + gz.name() + ": " + ex.getMessage()
                );
            }
        }
        return true;
    }

    private static PackageElement findPackage(Element e) {
        while (e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return (PackageElement)e;
    }

    private void generateJar(PackageElement pe, GenerateZip gz, Element e) throws IOException {
        final Filer filer = processingEnv.getFiler();
        FileObject res = filer.createResource(
            StandardLocation.CLASS_OUTPUT, 
            pe.getQualifiedName().toString(), 
            gz.name(), e
        );
        OutputStream os = res.openOutputStream();
        JarOutputStream jar;
        if (gz.manifest().isEmpty()) {
            jar = new JarOutputStream(os);
        } else {
            Manifest mf = new Manifest(new ByteArrayInputStream(gz.manifest().getBytes("UTF-8")));
            jar = new JarOutputStream(os, mf);
        }
        String[] arr = gz.contents();
        for (int i = 0; i < arr.length; i += 2) {
            JarEntry je = new JarEntry(arr[i]);
            jar.putNextEntry(je);
            jar.write(arr[i + 1].getBytes("UTF-8"));
            jar.closeEntry();
        }
        jar.close();
    }

}
