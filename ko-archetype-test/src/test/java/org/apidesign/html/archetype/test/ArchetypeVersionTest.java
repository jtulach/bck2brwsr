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

import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ArchetypeVersionTest {
    private String version;
    
    public ArchetypeVersionTest() {
    }
    
    @BeforeClass public void readCurrentVersion() throws Exception {
        version = findCurrentVersion();
        assertFalse(version.isEmpty(), "There should be some version string");
    }
    

    @Test public void testComparePomDepsVersions() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/pom.xml");
        assertNotNull(r, "Archetype pom found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//properties/net.java.html.version/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        String arch = (String) xp2.evaluate(dom, XPathConstants.STRING);

        assertEquals(arch, version, "net.java.html.json dependency needs to be on latest version");
    }
    
    @Test public void testCheckLauncher() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/pom.xml");
        assertNotNull(r, "Archetype pom found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//properties/bck2brwsr.launcher.version/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        String arch = (String) xp2.evaluate(dom, XPathConstants.STRING);

        
        assertTrue(arch.matches("[0-9\\.]+"), "launcher version seems valid: " + arch);
    }
    
    @Test public void testCheckBck2Brwsr() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/pom.xml");
        assertNotNull(r, "Archetype pom found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//properties/bck2brwsr.version/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        String arch = (String) xp2.evaluate(dom, XPathConstants.STRING);
        
        assertTrue(arch.matches("[0-9\\.]+"), "bck2brwsr version seems valid: " + arch);
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
            if (s.contains("apidesign")) {
                assertFalse(s.matches(".*apidesign.*[0-9].*"), "No numbers: " + s);
            }
        }
    }

    static String findCurrentVersion() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException, XPathFactoryConfigurationException {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL u = l.getResource("META-INF/maven/org.apidesign.html/knockout4j-archetype/pom.xml");
        assertNotNull(u, "Own pom found: " + System.getProperty("java.class.path"));

        final XPathFactory fact = XPathFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        XPathExpression xp = fact.newXPath().compile("project/version/text()");
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(u.openStream());
        return xp.evaluate(dom);
    }
}
