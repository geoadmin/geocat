package org.fao.xsl;

import com.google.common.collect.Sets;
import org.fao.xsl.support.Requirement;
import org.jdom.Element;

import java.util.Iterator;
import java.util.Set;

/**
 * Test that each responsible party has a unique TID.
 *
 * @author Jesse on 9/6/2015.
 */
public class DifferentTIDForContacts implements Requirement {
    @Override
    public boolean eval(Element e) {
        Set<String> tids = Sets.newHashSet();

        Iterator descendants = e.getDescendants();
        while (descendants.hasNext()) {
            Object next = descendants.next();
            if (next instanceof Element) {
                Element element = (Element) next;
                if (element.getName().equals("contactInfo")) {
                    String tid = element.getAttributeValue("REF");
                    if (tid != null) {
                        tids.add(tid);
                    }
                }
            }
        }

        return tids.size() != 1;
    }

    @Override
    public String toString() {
        return "All Contact Info should have different TIDs";
    }
}
