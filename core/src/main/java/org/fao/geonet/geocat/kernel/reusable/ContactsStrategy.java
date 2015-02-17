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

import com.google.common.base.Function;
import jeeves.server.UserSession;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.GeocatXslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ContactsStrategy extends AbstractSubtemplateStrategy {
    public static final String LUCENE_ROOT_RESPONSIBLE_PARTY = "che:CHE_CI_ResponsibleParty";
    protected static final String LUCENE_EMAIL = "electronicmailaddress";
    protected static final String LUCENE_FIRST_NAME = "individualfirstname";
    protected static final String LUCENE_LAST_NAME = "individuallastname";
    private static final String LUCENE_ORG_NAME = "organisationname";


    public ContactsStrategy(ApplicationContext context) {
        super(context);
    }

    @Override
    protected String createExtraData(String href) {
        final String regex = ".+\\?.*codeListValue~([^&#]+)&?.*";
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(href);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("cannot find role");
        }
        return matcher.group(1);
    }

    @Override
    protected BooleanQuery createSearchQuery(Element originalElem, String twoCharLangCode, String threeCharLangCode) {
        String email = lookupElement(originalElem, "electronicMailAddress", twoCharLangCode);
        String firstname = lookupElement(originalElem, "individualFirstName", twoCharLangCode);
        String lastname = lookupElement(originalElem, "individualLastName", twoCharLangCode);
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(LUCENE_FIRST_NAME, firstname)), BooleanClause.Occur.SHOULD);
        query.add(new TermQuery(new Term(LUCENE_LAST_NAME, lastname)), BooleanClause.Occur.SHOULD);
        query.add(new TermQuery(new Term(LUCENE_EMAIL, email)), BooleanClause.Occur.SHOULD);

        query.add(new TermQuery(new Term(LUCENE_LOCALE_FIELD, threeCharLangCode)), BooleanClause.Occur.SHOULD);

        return query;
    }

    @Override
    protected Collection<Element> xlinkIt(Element originalElem, String uuid, boolean validated) {
        @SuppressWarnings("unchecked")
        Iterator<Content> descendants = originalElem.getDescendants(new ElementFinder("CI_RoleCode", Geonet.Namespaces.GMD, "role"));

        Element roleElem = Utils.nextElement(descendants);
        String role;
        if (roleElem == null) {
            role = "";
            Log.warning(Geocat.Module.REUSABLE,
                    "A contact does not have a role associated with it: " + Xml.getString(originalElem));
        } else {
            role = roleElem.getAttributeValue("codeListValue");
        }
        return this.xlinkIt(originalElem, role, uuid, validated);
    }

    @Override
    protected FindResult calculateFit(Element originalElement, Document doc, String twoCharLangCode, String threeCharLangCode) {
        String email = lookupElement(originalElement, "electronicMailAddress", twoCharLangCode);
        String firstname = lookupElement(originalElement, "individualFirstName", twoCharLangCode);
        String lastname = lookupElement(originalElement, "individualLastName", twoCharLangCode);

        String[] docEmail = doc.getValues(LUCENE_EMAIL);
        String docFirstName = doc.get(LUCENE_FIRST_NAME);
        String docLastName = doc.get(LUCENE_LAST_NAME);

        String elemOrg = lookupElement(originalElement, "organisationName", twoCharLangCode);
        String docLocale = doc.get(LUCENE_LOCALE_FIELD);
        String docOrgName = doc.get(LUCENE_ORG_NAME);
        if (docLocale != null && docLocale.equals(threeCharLangCode) &&
            docOrgName != null && docOrgName.equalsIgnoreCase(elemOrg)) {
            int newRating = 0;

            HashSet<String> lowerCaseEmails = new HashSet<String>();

            for (String e : docEmail) {
                lowerCaseEmails.add(e.trim().toLowerCase());
            }

            if (lowerCaseEmails.contains(email.trim().toLowerCase())) {
                newRating += 10;
            }

            if (firstname.trim().equals(docFirstName)) {
                newRating += 2;
            }

            if (lastname.trim().equals(docLastName)) {
                newRating += 4;
            }

            return new FindResult(newRating == (10 + 2 + 4), newRating);
        }

        return new FindResult(false, -1);
    }

    @SuppressWarnings("unchecked")
    public static String lookupElement(Element originalElem, String name, final String twoCharLangCode) {
        Element elem = Utils.nextElement(originalElem.getDescendants(new ContactElementFinder("CharacterString",
                Geonet.Namespaces.GCO, name)));
        if (elem == null) {
            Iterator<Element> freeTexts = originalElem.getDescendants(new ContactElementFinder("PT_FreeText",
                    Geonet.Namespaces.GMD, name));
            while (freeTexts.hasNext()) {
                Element next = freeTexts.next();
                Iterator<Element> defaultLangElem = next.getDescendants(new Filter() {

                    private static final long serialVersionUID = 1L;

                    public boolean matches(Object arg0) {
                        if (arg0 instanceof Element) {
                            Element element = (Element) arg0;
                            return element.getName().equals("LocalisedCharacterString")
                                   && ("#" + twoCharLangCode).equalsIgnoreCase(element.getAttributeValue("locale"));
                        }
                        return false;
                    }

                });

                if (defaultLangElem.hasNext()) {
                    return defaultLangElem.next().getTextTrim();
                }

            }

            return "";
        } else {
            return elem.getTextTrim();
        }
    }

    public static String baseHref(String id) {
        return XLink.LOCAL_PROTOCOL + "subtemplate?uuid=" + id;
    }

    private Collection<Element> xlinkIt(Element originalElem, String role, String id, boolean validated) {
        originalElem.removeContent();
        // param order is important, id param must be first
        String href = baseHref(id);
        if (role != null && !role.trim().isEmpty()) {
            href += "&process=*//gmd:CI_RoleCode/@codeListValue~" + role;
        }
        originalElem.setAttribute(XLink.HREF, href, XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
        return Collections.singleton(originalElem);
    }

    @Override
    protected Element getSubtemplate(Element originalElem, String metadataLang) throws Exception {

        Element responsibleParty = originalElem.getChild("CHE_CI_ResponsibleParty", ISO19139cheNamespaces.CHE);
        if (responsibleParty == null) {
            return null;
        }
        final Element parent = responsibleParty.getChild("parentResponsibleParty", ISO19139cheNamespaces.CHE);

        processParent(responsibleParty, parent, metadataLang);

        return responsibleParty;
    }

    private void processParent(Element parent, Element parentInfo, String metadataLang) throws Exception {
        if (parentInfo == null || parentInfo.getChildren().isEmpty()) return;

        Element parentResponsibleParty = parentInfo.getChild("CHE_CI_ResponsibleParty", GeocatXslUtil.CHE_NAMESPACE);
        if (parentInfo.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK) == null) {
            Element placeholder = new Element("placeholder");
            Pair<Collection<Element>, Boolean> findResult = find(placeholder, parentResponsibleParty, metadataLang);

            if (!findResult.two()) {
                UpdateResult afterQuery = updateSubtemplate(parentInfo, null, false, metadataLang);
                if (afterQuery != null) {
                    parent.addContent(xlinkIt(parentInfo, afterQuery.uuid, false));
                } else {
                    parent.addContent(findResult.one());
                }
            } else {
                parent.addContent(findResult.one());
            }
        } else if (!ReusableObjManager.isValidated(parentInfo)) {
            Processor.uncacheXLinkUri(parentInfo.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));

            updateObject((Element) parentInfo.clone(), metadataLang);
        }
    }

    public Element list(UserSession session, String validated, String language) throws Exception {
        return super.listFromIndex(this.searchManager, LUCENE_ROOT_RESPONSIBLE_PARTY, validated, language, session, this,
                30000, new ContactDescFunc(), null);
    }

    @Override
    public Element search(UserSession session, String search, String language, int maxResults) throws Exception {
        return super.listFromIndex(this.searchManager, LUCENE_ROOT_RESPONSIBLE_PARTY, null, language, session, this,
                maxResults, new ContactDescFunc(), search
        );
    }

    public String createXlinkHref(String uuid, UserSession session, String role) {
        String href = XLink.LOCAL_PROTOCOL + "subtemplate?" + Params.UUID + "=" + uuid;
        if (role != null && !role.trim().isEmpty()) {
            href = href + "&process=*//gmd:CI_RoleCode/@codeListValue~"+role;
        }
        return href;
    }

    @Override
    public String toString() {
        return "Reusable Contact";
    }

    private static class ContactElementFinder extends ElementFinder {

        public ContactElementFinder(String name, Namespace ns, String parent) {
            super(name, ns, parent);
        }

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean otherChecks(Element elem) {
            return !isParentResponsibleParty(elem);
        }

        public boolean isParentResponsibleParty(Element elem) {
            if (elem == null) return false;
            if (elem.getName().equals("parentResponsibleParty")) {
                return true;
            }
            return isParentResponsibleParty(elem.getParentElement());
        }
    }

    @Override
    public String getInvalidXlinkLuceneField() {
        return "invalid_xlink_contact";
    }

    @Override
    public String getValidXlinkLuceneField() {
        return "valid_xlink_contact";
    }

    @Override
    protected String getEmptyTemplate() {
        return "<che:CHE_CI_ResponsibleParty xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:CI_ResponsibleParty\">\n"
               + "      <gmd:contactInfo>\n"
               + "        <gmd:CI_Contact>\n"
               + "          <gmd:address>\n"
               + "            <che:CHE_CI_Address gco:isoType=\"gmd:CI_Address\">\n"
               + "              <gmd:electronicMailAddress>\n"
               + "                <gco:CharacterString gco:nilReason=\"missing\"></gco:CharacterString>\n"
               + "              </gmd:electronicMailAddress>\n"
               + "            </che:CHE_CI_Address>\n"
               + "          </gmd:address>\n"
               + "        </gmd:CI_Contact>\n"
               + "      </gmd:contactInfo>\n"
               + "      <gmd:role>\n"
               + "        <gmd:CI_RoleCode codeListValue=\"pointOfContact\" codeList=\"http://www.isotc211" +
               ".org/2005/resources/codeList.xml#CI_RoleCode\"/>\n"
               + "      </gmd:role>\n"
               + "    </che:CHE_CI_ResponsibleParty>";
    }

    private static class ContactDescFunc implements Function<DescData, String> {
        @Nullable
        @Override
        public String apply(@Nonnull DescData data) {
            String email = safeField(data.doc, LUCENE_EMAIL);
            String name = safeField(data.doc, LUCENE_FIRST_NAME);
            String surname = safeField(data.doc, LUCENE_LAST_NAME);

            String desc;
            if (email == null || email.length() == 0) {
                desc = data.uuid;
            } else {
                desc = email;
            }

            return name + " " + surname + " (" + desc + ")";
        }

        private String safeField(Document doc, String fieldName) {
            final IndexableField field = doc.getField(fieldName);
            if (field != null) {
                return field.stringValue();
            } else {
                return "";
            }
        }

    }
}
