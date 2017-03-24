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
package org.apidesign.bck2brwsr.htmlpage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ExtraJavaScript(processByteCode = false, resource = "")
class ProcessPage {
    private final Map<String,String> ids2Elems = new TreeMap<String, String>();
    
    public Set<String> ids() {
        return Collections.unmodifiableSet(ids2Elems.keySet());
    }
    
    public String tagNameForId(String id) {
        return ids2Elems.get(id);
    }
    
    public static ProcessPage readPage(InputStream is) throws IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        f.setIgnoringComments(true);
        
        Document doc = null;
        try {
            DocumentBuilder b = f.newDocumentBuilder();
            doc = b.parse(is);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception e) {
            throw new IOException(e);
        }
        Element root = doc.getDocumentElement();
        
        ProcessPage pp = new ProcessPage();
        pp.seekForIds(root);
        return pp;
    }

    private void seekForIds(Element e) {
        String val = e.getAttribute("id");
        if (val != null && !val.isEmpty()) {
            String prev = ids2Elems.put(val, e.getTagName());
        }
        NodeList arr = e.getChildNodes();
        for (int i = 0; i < arr.getLength(); i++) {
            final Node n = arr.item(i);
            if (n instanceof Element) {
                seekForIds((Element)n);
            }
        }
    }
}
