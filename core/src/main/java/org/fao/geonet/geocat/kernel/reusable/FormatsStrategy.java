//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.geocat.kernel.reusable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import jeeves.server.UserSession;
import jeeves.xlink.XLink;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class FormatsStrategy extends AbstractSubtemplateStrategy {

    protected static final List<Namespace> NAMESPACES = Arrays.asList(ISO19139Namespaces.GCO, ISO19139Namespaces.GMD);
    private static final String LUCENE_FORMAT_NAME = "name";
    private static final String LUCENE_FORMAT_VERSION = "version";

    public FormatsStrategy(ApplicationContext context)
    {
        super(context);
    }

    @Override
    protected String createExtraData(String href) {
        return null;
    }

    @Override
    protected FindResult calculateFit(Element originalElem, Document doc, String twoCharLangCode, String threeCharLangCode) throws JDOMException {

        String name = Xml.selectString(originalElem, "*/gmd:name/gco:CharacterString", NAMESPACES);
        String version = Xml.selectString(originalElem, "*/gmd:version/gco:CharacterString", NAMESPACES);


        String docName = doc.get(LUCENE_FORMAT_NAME);
        String docVersion = doc.get(LUCENE_FORMAT_VERSION);

        int rating = rate(name, docName, 10, 5);
        rating += rate(version, docVersion, 5, 2);

        return new FindResult(rating == 15, rating);
    }

    private int rate(String name, String docName, int perfectMatch, int nullMatch) {
        if (docName != null) {
            if (docName.equalsIgnoreCase(name)) {
                return perfectMatch;
            }
        } else {
            if (name == null || name.trim().isEmpty()) {
                return nullMatch;
            }
        }
        return 0;
    }

    @Override
    protected BooleanQuery createSearchQuery(Element originalElem, String twoCharLangCode, String threeCharLangCode) throws JDOMException {
        String name = Xml.selectString(originalElem, "*/gmd:name/gco:CharacterString", NAMESPACES);
        String version = Xml.selectString(originalElem, "*/gmd:version/gco:CharacterString", NAMESPACES);

        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(LUCENE_FORMAT_NAME, name)), BooleanClause.Occur.SHOULD);
        query.add(new TermQuery(new Term(LUCENE_FORMAT_VERSION, version)), BooleanClause.Occur.SHOULD);
        query.add(new TermQuery(new Term(LUCENE_LOCALE_FIELD, threeCharLangCode)), BooleanClause.Occur.SHOULD);

        return query;
    }

    public Element list(UserSession session, String validated, String language, int maxResults) throws Exception
    {
        return super.listFromIndex(searchManager, "gmd:MD_Format", validated, language, session, this,
                maxResults, new FormatDescFunction(), null);
    }

    @Override
    public Element search(UserSession session, String search, String language, int maxResults) throws Exception {
        return super.listFromIndex(searchManager, "gmd:MD_Format", null, language, session, this,
                maxResults, new FormatDescFunction(), search);
    }

    public String createXlinkHref(String id, UserSession session, String notRequired) {
        return XLink.LOCAL_PROTOCOL+"subtemplate?uuid=" + id;
    }

    protected Collection<Element> xlinkIt(Element originalElem, String id, boolean validated)
    {
        originalElem.setAttribute(XLink.HREF, createXlinkHref(id, null, null), XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
        return Collections.singleton(originalElem);
    }

    @Override
    protected Element getSubtemplate(Element originalElem, String metadataLang) throws Exception {
        return originalElem.getChild("MD_Format", ISO19139Namespaces.GMD);
    }

    @Override
    public String toString()
    {
        return "Reusable Format";
    }

    @Override
    public String getInvalidXlinkLuceneField() {
        return "invalid_xlink_format";
    }
    
    @Override
    public String getValidXlinkLuceneField() {
    	return "valid_xlink_format";
    }

    @Override
    protected String getEmptyTemplate() {
        return "<gmd:MD_Format>\n"
               + "          <gmd:name gco:nilReason=\"missing\">\n"
               + "            <gco:CharacterString gco:nilReason=\"missing\"/>\n"
               + "          </gmd:name>\n"
               + "          <gmd:version gco:nilReason=\"missing\">\n"
               + "            <gco:CharacterString/>\n"
               + "          </gmd:version>\n"
               + "        </gmd:MD_Format>";
    }

    @VisibleForTesting
    public static class FormatDescFunction implements Function<DescData, String> {
        @Nonnull
        @Override
        public String apply(@Nonnull DescData data) {
            String name = stringValue(data, "name");
            String version  = stringValue(data, "version");

            if (name.isEmpty()) {
                if (version.isEmpty()) {
                    return "(" + data.uuid + ")";
                } else {
                    return version;
                }
            } else if (version.isEmpty()) {
                return name;
            } else {
                return name + " (" + version + ")";
            }
        }

        @Nonnull
        private String stringValue(DescData data, String fieldName) {
            for (Document document : data.langToDoc.values()) {
                final String fieldValue = document.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    return fieldValue;
                }
            }
            return "";
        }
    }
}
