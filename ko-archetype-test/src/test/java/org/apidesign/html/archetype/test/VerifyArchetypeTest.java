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
    }

    private Verifier generateFromArchetype(final File dir) throws Exception {
        Verifier v = new Verifier(dir.getAbsolutePath());
        v.setAutoclean(false);
        v.deleteDirectory("");
        dir.mkdirs();
        Properties sysProp = v.getSystemProperties();
        sysProp.put("groupId", "org.apidesign.test");
        sysProp.put("artifactId", "o-a-test");
        sysProp.put("package", "org.apidesign.test.oat");
        sysProp.put("archetypeGroupId", "org.apidesign.html");
        sysProp.put("archetypeArtifactId", "knockout4j-archetype");
        sysProp.put("archetypeVersion", ArchetypeVersionTest.findCurrentVersion());
        v.executeGoal("archetype:generate");
        v.verifyErrorFreeLog();
        return v;
    }
}
