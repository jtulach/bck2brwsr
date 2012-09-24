package org.apidesign.bck2brwsr.htmlpage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
