/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.archetype.test;

import java.io.File;
import java.util.Properties;
import java.util.zip.ZipFile;
import org.apache.maven.it.Verifier;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VerifyArchetypeTest {
    @Test public void fxBrwsrCompiles() throws Exception {
        final File dir = new File("target/tests/fxcompile/").getAbsoluteFile();
        generateFromArchetype(dir);
        
        File created = new File(dir, "o-a-test");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");
        
        Verifier v = new Verifier(created.getAbsolutePath());
        v.executeGoal("verify");
        
        v.verifyErrorFreeLog();
        
        for (String l : v.loadFile(v.getBasedir(), v.getLogFileName(), false)) {
            if (l.contains("j2js")) {
                fail("No pre-compilaton:\n" + l);
            }
        }
        
        v.verifyTextInLog("org.apidesign.bck2brwsr.launcher.FXBrwsrLauncher");
        v.verifyTextInLog("fxcompile/o-a-test/target/o-a-test-1.0-SNAPSHOT-fxbrwsr.zip");
    }
    
    @Test public void bck2BrwsrCompiles() throws Exception {
        final File dir = new File("target/tests/b2bcompile/").getAbsoluteFile();
        generateFromArchetype(dir);
        
        File created = new File(dir, "o-a-test");
        assertTrue(created.isDirectory(), "Project created");
        assertTrue(new File(created, "pom.xml").isFile(), "Pom file is in there");
        
        Verifier v = new Verifier(created.getAbsolutePath());
        v.addCliOption("-Pbck2brwsr");
        v.executeGoal("verify");
        
        v.verifyErrorFreeLog();
        
        // does pre-compilation to JavaScript
        v.verifyTextInLog("j2js");
        // uses Bck2BrwsrLauncher
        v.verifyTextInLog("BaseHTTPLauncher stopServerAndBrwsr");
        // building zip:
        v.verifyTextInLog("b2bcompile/o-a-test/target/o-a-test-1.0-SNAPSHOT-bck2brwsr.zip");
        
        for (String l : v.loadFile(v.getBasedir(), v.getLogFileName(), false)) {
            if (l.contains("fxbrwsr")) {
                fail("No fxbrwsr:\n" + l);
            }
        }

        File zip = new File(new File(created, "target"), "o-a-test-1.0-SNAPSHOT-bck2brwsr.zip");
        assertTrue(zip.isFile(), "Zip file with website was created");
        
        ZipFile zf = new ZipFile(zip);
        assertNotNull(zf.getEntry("public_html/index.html"), "index.html found");
        assertNotNull(zf.getEntry("public_html/twitterExample.css"), "css file found");
        
    }

    private Verifier generateFromArchetype(final File dir, String... params) throws Exception {
        Verifier v = new Verifier(dir.getAbsolutePath());
        v.setAutoclean(false);
        v.setLogFileName("generate.log");
        v.deleteDirectory("");
        dir.mkdirs();
        Properties sysProp = v.getSystemProperties();
        sysProp.put("groupId", "org.apidesign.test");
        sysProp.put("artifactId", "o-a-test");
        sysProp.put("package", "org.apidesign.test.oat");
        sysProp.put("archetypeGroupId", "org.apidesign.html");
        sysProp.put("archetypeArtifactId", "knockout4j-archetype");
        sysProp.put("archetypeVersion", ArchetypeVersionTest.findCurrentVersion());
        
        for (String p : params) {
            v.addCliOption(p);
        }
        v.executeGoal("archetype:generate");
        v.verifyErrorFreeLog();
        return v;
    }
}
