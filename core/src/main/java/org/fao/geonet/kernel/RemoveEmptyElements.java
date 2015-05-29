package org.fao.geonet.kernel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.fao.geonet.constants.Geonet;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;
import java.util.Set;

/**
 * Remove empty subtrees.  Empty means subtrees with no character data or relevant attributes.
 *
 * @author Jesse on 5/29/2015.
 */
public class RemoveEmptyElements {

    public static Element apply(Element metadataEl) {
        List<Element> toRemove = Lists.newArrayList();
        for (Element child : (List<Element>) metadataEl.getChildren()) {
            apply(child);
            if (emptyChild(child)) {
                toRemove.add(child);
            }
        }

        for (Element e : toRemove) {
            e.detach();
        }

        return metadataEl;
    }

    private static boolean emptyChild(Element child) {
        return child.getChildren().isEmpty() && child.getTextTrim().isEmpty() && !hasRelevantAttribute(child);
    }

    private static final Set<String> IRRELEVANT_ATTS_NSURI = Sets.newHashSet(
            Geonet.Namespaces.XLINK.getURI(),
            "http://www.w3.org/2001/XMLSchema-instance"
            );

    private static final Set<String> IRRELEVANT_ATTS_NAME = Sets.newHashSet("codeList", "locale", "isoType");
    private static boolean hasRelevantAttribute(Element child) {
        boolean relevantAtt = false;
        for (Attribute attribute : (List<Attribute>) child.getAttributes()) {
            relevantAtt |= !IRRELEVANT_ATTS_NSURI.contains(attribute.getNamespace().getURI()) &&
                           !IRRELEVANT_ATTS_NAME.contains(attribute.getName()) &&
                           !attribute.getNamespace().getPrefix().equals("xmlns");
        }
        return relevantAtt;
    }
}
