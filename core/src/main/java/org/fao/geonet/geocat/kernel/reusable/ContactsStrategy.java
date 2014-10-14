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
import com.google.common.collect.Lists;
import jeeves.server.UserSession;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.User_;
import org.fao.geonet.domain.geocat.GeocatUserInfo_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.BatchUpdateQuery;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.geocat.specification.GeocatUserSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheSchemaPlugin;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.GeocatXslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import static org.springframework.data.jpa.domain.Specifications.where;

public final class ContactsStrategy extends ReplacementStrategy {
    public static final String LUCENE_ROOT_RESPONSIBLE_PARTY = "che:CHE_CI_ResponsibleParty";
    protected static final String LUCENE_EMAIL = "electronicmailaddress";
    protected static final String LUCENE_FIRST_NAME = "individualfirstname";
    protected static final String LUCENE_LAST_NAME = "individuallastname";
    private static final String LUCENE_ORG_NAME = "organisationname";

    private final UserRepository _userRepository;
    private final UserGroupRepository _userGroupRepository;
    private final SearchManager searchManager;
    private final IsoLanguagesMapper mapper;
    private final MetadataRepository metadataRepository;
    private final SettingRepository settingRepository;
    private final DataManager dataManager;

    public ContactsStrategy(ApplicationContext context, String appPath) {
        this._userRepository = context.getBean(UserRepository.class);
        this.metadataRepository = context.getBean(MetadataRepository.class);
        this._userGroupRepository = context.getBean(UserGroupRepository.class);
        this.searchManager = context.getBean(SearchManager.class);
        this.settingRepository = context.getBean(SettingRepository.class);
        this.dataManager = context.getBean(DataManager.class);
        this.mapper = context.getBean(IsoLanguagesMapper.class);
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception {
        if (XLink.isXLink(originalElem)) {
            return NULL;
        }

        @SuppressWarnings("unchecked")
        Iterator<Content> descendants = originalElem.getDescendants(new ElementFinder("CI_RoleCode",
                Geonet.Namespaces.GMD, "role"));
        Element roleElem = Utils.nextElement(descendants);
        String role;
        if (roleElem == null) {
            role = "";
            Log.warning(Geocat.Module.REUSABLE,
                    "A contact does not have a role associated with it: " + Xml.getString(originalElem));
        } else {
            role = roleElem.getAttributeValue("codeListValue");
        }

        String email = lookupElement(originalElem, "electronicMailAddress", defaultMetadataLang);
        String firstname = lookupElement(originalElem, "individualFirstName", defaultMetadataLang);
        String lastname = lookupElement(originalElem, "individualLastName", defaultMetadataLang);

        String locale = mapper.iso639_1_to_iso639_2(defaultMetadataLang);
        final IndexAndTaxonomy indexAndTaxonomy = this.searchManager.getNewIndexReader(locale);
        try {
            BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term(LUCENE_FIRST_NAME, firstname)), BooleanClause.Occur.SHOULD);
            query.add(new TermQuery(new Term(LUCENE_LAST_NAME, lastname)), BooleanClause.Occur.SHOULD);
            query.add(new TermQuery(new Term(LUCENE_EMAIL, email)), BooleanClause.Occur.SHOULD);

            query.add(new TermQuery(new Term(LUCENE_LOCALE_FIELD, locale)), BooleanClause.Occur.SHOULD);

            TopFieldCollector collector = TopFieldCollector.create(Sort.RELEVANCE, 30000, true, false, false, false);

            final GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.search(query, collector);

            ScoreDoc[] topDocs = collector.topDocs().scoreDocs;

            Document bestFit = null;
            int rating = 0;
            for (ScoreDoc scoreDoc : topDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String[] docEmail = doc.getValues(LUCENE_EMAIL);
                String docFirstName = doc.get(LUCENE_FIRST_NAME);
                String docLastName = doc.get(LUCENE_LAST_NAME);

                String elemOrg = lookupElement(originalElem, "organisationName", defaultMetadataLang);
                String docLocale = doc.get(LUCENE_LOCALE_FIELD);
                String docOrgName = doc.get(LUCENE_ORG_NAME);
                if (docLocale.equals(locale) && docOrgName.equalsIgnoreCase(elemOrg)) {
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

                    if (newRating > rating) {
                        rating = newRating;
                        bestFit = doc;
                    }

                    if (newRating == (10 + 2 + 4)) {
                        break;
                    }
                }
            }

            if (bestFit == null) {
                return NULL;
            } else {

                String uuid = bestFit.get(LUCENE_UUID_FIELD);
                boolean validated = LUCENE_EXTRA_VALIDATED.equals(bestFit.get(LUCENE_EXTRA_FIELD));
                Collection<Element> xlinkIt = xlinkIt(originalElem, role, uuid, validated);

                return Pair.read(xlinkIt, true);
            }
        } finally {
            this.searchManager.releaseIndexReader(indexAndTaxonomy);
        }
    }

    @SuppressWarnings("unchecked")
    public static String lookupElement(Element originalElem, String name, final String defaultMetadataLang) {
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
                                   && ("#" + defaultMetadataLang).equalsIgnoreCase(element.getAttributeValue("locale"));
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
        originalElem.setAttribute(XLink.HREF, baseHref(id) + "&process=*//gmd:CI_RoleCode/@codeListValue~" + role, XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
        return Collections.singleton(originalElem);
    }

    public Collection<Element> add(Element placeholder, Element originalElem, String metadataLang)
            throws Exception {
        UpdateResult result = processQuery(originalElem, null, false, metadataLang);

        return xlinkIt(originalElem, result.role, result.uuid, false);
    }


    private static class UpdateResult {
        String uuid;
        String role;
    }

    /**
     * Executes the query using all the user data from the element. There must
     * be exactly 28 ? in the query. id is not one of them
     *
     * @param metadataLang
     */
    private UpdateResult processQuery(Element originalElem, Integer id, boolean validated,
                                      String metadataLang) throws Exception {
        Element responsibleParty = originalElem.getChild("CHE_CI_ResponsibleParty", ISO19139cheNamespaces.CHE);

        final Metadata metadata = new Metadata();
        if (id != null) {
            metadata.setId(id);
        }
        metadata.setDataAndFixCR(responsibleParty);
        String uuid = UUID.randomUUID().toString();
        metadata.setUuid(uuid);
        MetadataDataInfo dataInfo = new MetadataDataInfo().
                setExtra(validated ? LUCENE_EXTRA_VALIDATED : LUCENE_EXTRA_NON_VALIDATED).
                setRoot(responsibleParty.getQualifiedName()).
                setSchemaId(ISO19139cheSchemaPlugin.IDENTIFIER).
                setType(MetadataType.SUB_TEMPLATE);
        metadata.setDataInfo(dataInfo);
        metadata.setSourceInfo(new MetadataSourceInfo().setSourceId(getSourceId()));

        this.metadataRepository.save(metadata);

        this.dataManager.indexMetadata(String.valueOf(metadata.getId()), true, false, false, false);

        final String role = Xml.selectString(responsibleParty, "*//gmd:CI_RoleCode/@codeListValue");
        final Element parent = Xml.selectElement(responsibleParty, "*//che:parentResponsibleParty");

        processParent(responsibleParty, parent, metadataLang);

        UpdateResult result = new UpdateResult();
        result.uuid = uuid;
        result.role = role;

        return result;
    }

    private String getSourceId() {
        return this.settingRepository.findOne(SettingManager.SYSTEM_SITE_SITE_ID_PATH).getValue();
    }

    private void processParent(Element parent, Element parentInfo, String metadataLang) throws Exception {
        if (parentInfo == null || parentInfo.getChildren().isEmpty()) return;

        Element parentResponsibleParty = parentInfo.getChild("CHE_CI_ResponsibleParty", GeocatXslUtil.CHE_NAMESPACE);
        if (parentInfo.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK) == null) {
            Element placeholder = new Element("placeholder");
            Pair<Collection<Element>, Boolean> findResult = find(placeholder, parentResponsibleParty, metadataLang);

            if (!findResult.two()) {
                UpdateResult afterQuery = processQuery(parentResponsibleParty, null, false, metadataLang);
                parent.addContent(xlinkIt(parentInfo, afterQuery.role, afterQuery.uuid, false));
            } else {
                parent.addContent(findResult.one());
            }
        } else if (!ReusableObjManager.isValidated(parentInfo)) {
            Processor.uncacheXLinkUri(parentInfo.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));

            updateObject((Element) parentInfo.clone(), metadataLang);
        }
    }

    public Element list(UserSession session, boolean validated, String language) throws Exception {
        return super.listFromIndex(this.searchManager, LUCENE_ROOT_RESPONSIBLE_PARTY, validated, language, session, this,
                new Function<DescData, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable DescData data) {
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

                });
    }

    public void performDelete(String[] ids, UserSession session, String ignored) throws Exception {

        List<Integer> intIds = Lists.transform(Arrays.asList(ids), new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable String input) {
                return input == null ? -1 : Integer.parseInt(input);
            }
        });

        final BatchUpdateQuery batchUpdateQuery = _userRepository.createBatchUpdateQuery(new PathSpec() {

            @Override
            public Path getPath(Root root) {
                return root.get(User_.geocatUserInfo).get(GeocatUserInfo_.parentInfo);
            }
        }, null, GeocatUserSpecs.hasParentIdIn(intIds));

        batchUpdateQuery.execute();

        _userGroupRepository.deleteAllByIdAttribute(UserGroupId_.userId, intIds);

        _userRepository.deleteAll(UserSpecs.hasUserIdIn(intIds));
    }

    public String createXlinkHref(String id, UserSession session, String notRequired) {
        return XLink.LOCAL_PROTOCOL + "subtemplate?" + Params.UUID + "=" + id;
    }

    public String updateHrefId(String oldHref, String id, UserSession session) {
        return oldHref.replaceAll("id=\\d+", "id=" + id).replaceAll("/fra/|/deu/|/ita/|/___/", "/eng/");
    }

    public Map<String, String> markAsValidated(String[] ids, UserSession session) throws Exception {
        List<Integer> intIds = Lists.transform(Arrays.asList(ids), new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable String input) {
                return input == null ? -1 : Integer.parseInt(input);
            }
        });

        final Specification<User> spec = where(UserSpecs.hasUserIdIn(intIds))
                .and(UserSpecs.hasProfile(Profile.Shared));

        _userRepository.createBatchUpdateQuery(new PathSpec<User, Character>() {
            @Override
            public Path<Character> getPath(Root<User> root) {
                return root.get(User_.geocatUserInfo).get(GeocatUserInfo_.jpaWorkaround_validated);
            }
        }, Constants.toYN_EnabledChar(true), spec);

        Map<String, String> idMap = new HashMap<String, String>();

        for (String id : ids) {
            idMap.put(id, id);
        }
        return idMap;
    }

    public Collection<Element> updateObject(Element xlink, String metadataLang) throws Exception {
        int id = Integer.parseInt(Utils.extractUrlParam(xlink, "id"));

        UpdateResult results = processQuery(xlink, id, false, metadataLang);

        String href = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
        int roleIndex = href.indexOf("role=");
        href = href.substring(0, roleIndex) + "role=" + results.role;
        xlink.setAttribute(XLink.HREF, href, XLink.NAMESPACE_XLINK);

        return Collections.emptySet();
    }

    public boolean isValidated(String href) throws NumberFormatException, SQLException {
        String id = Utils.id(href);
        if (id == null) return false;
        try {
            Specifications<User> spec = where(UserSpecs.hasUserId(Integer.parseInt(id)))
                    .and(UserSpecs.hasProfile(Profile.Shared))
                    .and(GeocatUserSpecs.isValidated(true));
            return _userRepository.count(spec) == 1;
        } catch (NumberFormatException e) {
            return false;
        }
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
    public String[] getInvalidXlinkLuceneField() {
        return new String[]{"invalid_xlink_contact"};
    }

    @Override
    public String[] getValidXlinkLuceneField() {
        return new String[]{"valid_xlink_contact"};
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {
        String startId = Utils.id(href);
        if (startId != null) return href;

        final String regex = ".+\\?.*role=([^&#]+)&?.*";
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(href);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("cannot find role");
        }
        String role = matcher.group(1);

        String username = UUID.randomUUID().toString();
        String email = username + "@generated.org";
        User user = new User();
        user.setUsername(username);
        user.getSecurity().setPassword("");
        user.setProfile(Profile.Shared);
        user.getEmailAddresses().add(email);
        user.getGeocatUserInfo().setValidated(false);

        final User saved = _userRepository.save(user);
        int id = saved.getId();

        return XLink.LOCAL_PROTOCOL + "xml.user.get?id=" + id + "&schema=iso19139.che&role=" + role;
    }
}
