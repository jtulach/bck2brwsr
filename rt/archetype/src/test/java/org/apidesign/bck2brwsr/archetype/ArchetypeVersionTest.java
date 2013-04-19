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
package org.apidesign.bck2brwsr.archetype;

import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ArchetypeVersionTest {
    private String version;
    
    public ArchetypeVersionTest() {
    }
    
    @BeforeClass public void readCurrentVersion() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL u = l.getResource("META-INF/maven/org.apidesign.bck2brwsr/bck2brwsr-archetype-html-sample/pom.xml");
        assertNotNull(u, "Own pom found: " + System.getProperty("java.class.path"));

        final XPathFactory fact = XPathFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        XPathExpression xp = fact.newXPath().compile("project/version/text()");
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(u.openStream());
        version = xp.evaluate(dom);

        assertFalse(version.isEmpty(), "There should be some version string");
    }
    

    @Test public void testComparePomDepsVersions() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/pom.xml");
        assertNotNull(r, "Archetype pom found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//version[../groupId/text() = 'org.apidesign.bck2brwsr']/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        NodeList arch = (NodeList) xp2.evaluate(dom, XPathConstants.NODESET);

        if (arch.getLength() < 3) {
            fail("There should be at least three dependencies to bck2brwsr APIs: " + arch.getLength());
        }
        
        for (int i = 0; i < arch.getLength(); i++) {
            assertEquals(arch.item(i).getTextContent(), version, i + "th dependency needs to be on latest version of bck2brwsr");
        }
    }
    
    @Test public void testNbActions() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/nbactions.xml");
        assertNotNull(r, "Archetype nb file found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//goal/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        NodeList goals = (NodeList) xp2.evaluate(dom, XPathConstants.NODESET);
        
        for (int i = 0; i < goals.getLength(); i++) {
            String s = goals.item(i).getTextContent();
            if (s.contains("bck2brwsr")) {
                String[] arr = s.split(":");
                assertEquals(arr.length, 4, "Three :");
                assertEquals(arr[2], version, "Proper version is used");
            }
        }
    }
}
