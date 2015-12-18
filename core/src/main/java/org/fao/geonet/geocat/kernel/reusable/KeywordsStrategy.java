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
import jeeves.xlink.XLink;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParams;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordSearchType;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.openrdf.model.URI;
import org.openrdf.sesame.config.AccessDeniedException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.lucene.search.WildcardQuery.WILDCARD_STRING;

public final class KeywordsStrategy extends SharedObjectStrategy {
    public static Logger LOGGER = Log.createLogger(KeywordsStrategy.class.getName());
    public static final String NAMESPACE = "http://custom.shared.obj.ch/concept#";
    public static final String GEOCAT_THESAURUS_NAME = "local._none_.geocat.ch";
    public static final String NON_VALID_THESAURUS_NAME = "local._none_.non_validated";

    private final ThesaurusFinder _thesaurusMan;
    private final Path _styleSheet;
    private final String _currentLocale;
    private final IsoLanguagesMapper _isoLanguagesMapper;

    public KeywordsStrategy(IsoLanguagesMapper isoLanguagesMapper, ThesaurusFinder thesaurusMan, Path appPath, String baseURL, String
            currentLocale) {
        this._thesaurusMan = thesaurusMan;
        this._isoLanguagesMapper = isoLanguagesMapper;
        _styleSheet = appPath.resolve(Utils.XSL_REUSABLE_OBJECT_DATA_XSL);

        _currentLocale = currentLocale;
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception {

        if (XLink.isXLink(originalElem))
            return NULL;

        Collection<Element> results = new ArrayList<>();

        List<Pair<Element, String>> allKeywords = getAllKeywords(originalElem);
        java.util.Set<String> addedIds = new HashSet<>();
        for (Pair<Element, String> elem : allKeywords) {
            if (elem.one().getParent() == null || elem.two() == null || elem.two().trim().isEmpty()) {
                // already processed by another translation.
                continue;
            }

            KeywordsSearcher searcher = search(elem.two());

            List<KeywordBean> keywords = searcher.getResults();
            if (!keywords.isEmpty()) {
                KeywordBean keyword = keywords.get(0);
                elem.one().detach();
                String thesaurus = keyword.getThesaurusKey();
                String uriCode = keyword.getUriCode();

                // do not add if a keyword with the same ID and thesaurus has previously been added
                if (addedIds.add(thesaurus + "@@" + uriCode)) {
                    boolean validated = !thesaurus.equalsIgnoreCase(NON_VALID_THESAURUS_NAME);
                    Element descriptiveKeywords = xlinkIt(thesaurus, uriCode, validated);
                    results.add(descriptiveKeywords);
                }
            }
        }

        // need to return null if not matche are found so the calling class
        // knows there is not changes made
        if (results.isEmpty()) {
            return NULL;
        }

        boolean done = true;
        List<Element> allKeywords1 = Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                "keyword", Geonet.Namespaces.GMD, "MD_Keywords")), Element.class);
        if (allKeywords1.size() > 0) {
            // still have some elements that need to be made re-usable
            done = false;
        }
        return Pair.read(results, done);
    }

    private List<Pair<Element, String>> getAllKeywords(Element originalElem) {
        List<Element> allKeywords1 = Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                "keyword", Geonet.Namespaces.GMD, "MD_Keywords")), Element.class);
        List<Pair<Element, String>> allKeywords = new ArrayList<>();
        for (Element element : allKeywords1) {
            allKeywords.addAll(zip(element, Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                    "CharacterString", Geonet.Namespaces.GCO, "keyword")), Element.class)));
            allKeywords.addAll(zip(element, Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                    "LocalisedCharacterString", Geonet.Namespaces.GMD, "textGroup")), Element.class)));
        }
        return allKeywords;
    }

    private Collection<? extends Pair<Element, String>> zip(Element keywordElem, List<Element> convertToList) {
        List<Pair<Element, String>> zipped = new ArrayList<>();
        for (Element word : convertToList) {
            zipped.add(Pair.read(keywordElem, word.getTextTrim()));
        }
        return zipped;
    }

    private KeywordsSearcher search(String keyword) throws Exception {
        KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(this._isoLanguagesMapper);
        builder.addLang("eng")
                .addLang("ger")
                .addLang("fre")
                .addLang("ita")
                .maxResults(1)
                .keyword(keyword, KeywordSearchType.MATCH, true);

        Collection<Thesaurus> thesauri = new ArrayList<>(_thesaurusMan.getThesauriMap().values());
        for (Iterator<Thesaurus> iterator = thesauri.iterator(); iterator.hasNext(); ) {
            Thesaurus thesaurus = iterator.next();
            if (thesaurus instanceof AllThesaurus) {
                continue;
            }
            String type = thesaurus.getType();

            if (type.equals("external")) {
                builder.addThesaurus(thesaurus.getKey());
                iterator.remove();
            } else if (thesaurus.getKey().equals(NON_VALID_THESAURUS_NAME)) {
                iterator.remove();
            }
        }

        for (Thesaurus thesaurus : thesauri) {
            if (thesaurus instanceof AllThesaurus) {
                continue;
            }
            builder.addThesaurus(thesaurus.getKey());
        }

        builder.addThesaurus(NON_VALID_THESAURUS_NAME);

        KeywordsSearcher searcher = new KeywordsSearcher(this._isoLanguagesMapper, _thesaurusMan);
        builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.DESC));
        searcher.search(builder.build());
        return searcher;
    }

    public Element list(UserSession session, String validated, String language, int maxResults) throws Exception {

        List<String> thesaurusNames = Lists.newArrayList();

        if (validated == null) {
            thesaurusNames.addAll(_thesaurusMan.getThesauriMap().keySet());
        } else if (validated.equalsIgnoreCase(LUCENE_EXTRA_VALIDATED)) {
            thesaurusNames.add(GEOCAT_THESAURUS_NAME);
        } else if (validated.equalsIgnoreCase(LUCENE_EXTRA_NON_VALIDATED)) {
            thesaurusNames.add(NON_VALID_THESAURUS_NAME);
        } else {
            thesaurusNames.add(validated);
        }

        Element keywords = new Element(REPORT_ROOT);

        for (String thesaurusName : thesaurusNames) {
            if (maxResults <= keywords.getContentSize()) {
                break;
            }

            KeywordsSearcher searcher = new KeywordsSearcher(this._isoLanguagesMapper, _thesaurusMan);

            KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(this._isoLanguagesMapper);
            builder.addLang(_currentLocale).addLang("fre").addLang("eng").addLang("ger").addLang("ita")
                    .keyword("*", KeywordSearchType.MATCH, false).maxResults(maxResults - keywords.getContentSize())
                    .addThesaurus(thesaurusName);
            builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.DESC));
            KeywordSearchParams params = builder.build();
            searcher.search(params);
            session.setProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT, searcher);

            addSearchResults(thesaurusName, keywords, searcher, !thesaurusName.equals(NON_VALID_THESAURUS_NAME));
        }

        sortResults(keywords, null);
        return keywords;
    }

    private void addSearchResults(String thesaurusName, Element keywords, KeywordsSearcher searcher, boolean
            validated) throws Exception {
        for (KeywordBean bean : searcher.getResults()) {
            Element e = new Element(REPORT_ELEMENT);
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(XLink.LOCAL_PROTOCOL);
            uriBuilder.append("thesaurus.admin?thesaurus=");
            uriBuilder.append(thesaurusName);
            uriBuilder.append("&id=");
            uriBuilder.append(URLEncoder.encode(bean.getUriCode(), "UTF-8"));
            uriBuilder.append("&lang=");
            uriBuilder.append(bean.getDefaultLang());

            Utils.addChild(e, REPORT_ID, createKeywordId(bean));
            Utils.addChild(e, REPORT_URL, uriBuilder.toString());
            Utils.addChild(e, REPORT_TYPE, "keyword");

            final String xlinkHref = createXlinkHRefImpl(bean, validateName(bean.getThesaurusKey()));
            Utils.addChild(e, REPORT_XLINK, xlinkHref);
            String desc = bean.getDefaultValue();
            if (desc == null || desc.isEmpty()) {
                for (String word : bean.getValues().values()) {
                    if (desc != null && !desc.isEmpty()) {
                        break;
                    }
                    desc = word;
                }
            }

            Utils.addChild(e, REPORT_DESC, desc);
            Utils.addChild(e, REPORT_VALIDATED, "" + validated);
            Utils.addChild(e, REPORT_SEARCH, bean.getThesaurusKey() + bean.getUriCode() + bean.getDefaultValue());

            keywords.addContent(e);
        }
    }

    private String createKeywordId(KeywordBean bean) {
        return "thesaurus=" + bean.getThesaurusKey() + "&id=" + bean.getUriCode();
    }

    @Override
    public Element search(UserSession session, String validated, String search, String language, int maxResults) throws Exception {
        Element results = new Element(REPORT_ELEMENT);

        KeywordsSearcher searcher = new KeywordsSearcher(this._isoLanguagesMapper, _thesaurusMan);

        for (Thesaurus thesaurus : _thesaurusMan.getThesauriMap().values()) {
            final String thesaurusKey = thesaurus.getKey();
            if (!GEOCAT_THESAURUS_NAME.equalsIgnoreCase(thesaurusKey) && !NON_VALID_THESAURUS_NAME.equalsIgnoreCase(thesaurusKey)) {
                doSearch(session, search, results, searcher, thesaurusKey, true, maxResults);
            }
        }

        doSearch(session, search, results, searcher, GEOCAT_THESAURUS_NAME, true, maxResults);
        doSearch(session, search, results, searcher, NON_VALID_THESAURUS_NAME, false, maxResults);

        sortResults(results, search);
        return results;
    }

    private void doSearch(UserSession session, String search, Element results, KeywordsSearcher searcher, String thesaurusKey, boolean
            validated, int maxResults) throws Exception {
        if (maxResults >= results.getContentSize()) {
            KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(this._isoLanguagesMapper);
            builder.addLang("eng").addLang("fre").addLang("ger").addLang("ita").addLang("roh").keyword(search, KeywordSearchType.CONTAINS, false);

            builder.addThesaurus(thesaurusKey);
            builder.maxResults(maxResults - results.getContentSize());
            builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.DESC));
            KeywordSearchParams params = builder.build();
            searcher.search(params);

            session.setProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT, searcher);

            addSearchResults(thesaurusKey, results, searcher, validated);
        }
    }

    public String createXlinkHref(String id, UserSession session, String thesaurusName) throws Exception {
        String thesaurus = validateName(thesaurusName);
        KeywordBean concept = lookup(id);
        return createXlinkHRefImpl(concept, thesaurus);
    }

    public static String createXlinkHRefImpl(KeywordBean concept, String thesaurus) throws UnsupportedEncodingException {
        String uri = concept.getUriCode();
        return XLink.LOCAL_PROTOCOL + "xml.keyword.get?thesaurus=" + thesaurus + "&id=" + URLEncoder.encode(uri, "utf-8") +
               "&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly";
    }

    public void performDelete(String[] ids, UserSession session, String thesaurusName) throws Exception {

        for (String id : ids) {
            try {
                // A test to see if id is from a previous search or 
                KeywordBean concept = lookup(id);
                Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(concept.getThesaurusKey());
                thesaurus.removeElement(concept);
            } catch (NumberFormatException e) {
                Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(validateName(thesaurusName));
                thesaurus.removeElement(NAMESPACE, extractCode(id));
            }
        }

    }

    private static String validateName(String thesaurusName) {
        if (thesaurusName == null) {
            return NON_VALID_THESAURUS_NAME;
        } else {
            return thesaurusName;
        }
    }

    private KeywordBean lookup(String id) {
        final KeywordSearchParamsBuilder paramsBuilder = new KeywordSearchParamsBuilder(_isoLanguagesMapper);
        paramsBuilder.addLang("eng").addLang("fre").addLang("ger").addLang("ita");
        final Pair<String, String> idPair = splitUriAndThesaurusName(id);
        String uri = idPair.one();
        String thesaurusName = idPair.two();

        paramsBuilder.addThesaurus(thesaurusName).uri(uri);
        final KeywordSearchParams build = paramsBuilder.build();

        final List<KeywordBean> keywords;
        try {
            keywords = build.search(_thesaurusMan);
            return keywords.get(0);
        } catch (Throwable e) {
            return null;
        }
    }

    public Pair<String, String> splitUriAndThesaurusName(String id) {
        String[] idParts = id.split("\\&|=");
        String uri = null, thesaurusName = null;

        if (idParts[0].equalsIgnoreCase("id")) {
            uri = idParts[1];
        } else {
            thesaurusName = idParts[1];
        }
        if (idParts[2].equalsIgnoreCase("id")) {
            uri = idParts[3];
        } else {
            thesaurusName = idParts[3];
        }

        return Pair.read(uri, thesaurusName);
    }
    public String updateHrefId(String oldHref, String uriCodeAndThesaurusName, UserSession session)
            throws UnsupportedEncodingException {
        final KeywordBean concept = lookup(uriCodeAndThesaurusName);
        if (concept == null) {
            LOGGER.warning("Didn't find the Thesaurus for " + uriCodeAndThesaurusName);
            return null;
        }
        String base = oldHref.substring(0, oldHref.indexOf('?'));
        String encoded = URLEncoder.encode(concept.getUriCode(), "utf-8");
        return base + "?thesaurus=" + GEOCAT_THESAURUS_NAME + "&id=" + encoded + "&locales=en,it,de,fr";
    }

    public Map<String, String> markAsValidated(String[] ids, UserSession session) throws Exception {

        Thesaurus geocatThesaurus = _thesaurusMan.getThesaurusByName(GEOCAT_THESAURUS_NAME);
        Thesaurus nonValidThesaurus = _thesaurusMan.getThesaurusByName(NON_VALID_THESAURUS_NAME);

        Map<String, String> idMap = new HashMap<>();
        for (String uriCodeAndThesaurusName : ids) {
            KeywordBean concept = lookup(uriCodeAndThesaurusName);
            final String newUri = "thesaurus=" + geocatThesaurus.getKey() + "&id=" + concept.getUriCode();
            idMap.put(uriCodeAndThesaurusName, newUri);
            geocatThesaurus.addElement(concept);
            nonValidThesaurus.removeElement(concept);
        }
        return idMap;
    }

    private Element xlinkIt(String thesaurus, String keywordUri, boolean validated) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(keywordUri, "UTF-8");
        Element descriptiveKeywords = new Element("descriptiveKeywords", Geonet.Namespaces.GMD);

        descriptiveKeywords.setAttribute(XLink.HREF,
                XLink.LOCAL_PROTOCOL + "xml.keyword.get?thesaurus=" + thesaurus +
                "&id=" + encoded + "&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords", XLink.NAMESPACE_XLINK);

        if (!validated) {
            descriptiveKeywords.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE,
                    XLink.NAMESPACE_XLINK);
        }

        descriptiveKeywords.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        return descriptiveKeywords;
    }

    public Collection<Element> add(Element placeholder, Element originalElem, String metadataLang)
            throws Exception {

        String nonValidThesaurusName = NON_VALID_THESAURUS_NAME;
        String code = UUID.randomUUID().toString();

        URI uri = doUpdateKeyword(originalElem, nonValidThesaurusName, code, metadataLang, false);
        if (uri == null) {
            return Collections.emptyList();
        } else {
            return Collections.singleton(xlinkIt(NON_VALID_THESAURUS_NAME, uri.toString(), false));
        }
    }

    private URI doUpdateKeyword(Element originalElem, String nonValidThesaurusName, String code, String metadataLang,
                                boolean update) throws Exception, AccessDeniedException {
        @SuppressWarnings("unchecked")
        List<Element> xml = Xml.transform((Element) originalElem.clone(), _styleSheet).getChildren("keyword");

        Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(nonValidThesaurusName);

        KeywordBean bean = new KeywordBean(this._isoLanguagesMapper).
                setNamespaceCode(NAMESPACE).
                setRelativeCode(code);

        for (Element keywordElement : xml) {
            String keyword = keywordElement.getTextTrim();
            String locale = keywordElement.getAttributeValue("locale");
            if (locale == null || locale.trim().length() < 2) {
                locale = metadataLang;
            } else {
                locale = locale.toLowerCase();
            }

            locale = locale.toLowerCase().substring(0, 2);
            bean.setValue(keyword, locale);
            bean.setDefinition(keyword, locale);
        }
        URI uri;
        if (update) {
            uri = thesaurus.updateElement(bean, true);
        } else {
            uri = thesaurus.addElement(bean);
        }
        return uri;
    }

    public Collection<Element> updateObject(Element xlink, String metadataLang) throws Exception {
        String thesaurusName = Utils.extractUrlParam(xlink, "thesaurus");
        if (!NON_VALID_THESAURUS_NAME.equals(thesaurusName)) {
            return Collections.emptySet();
        }
        String id = Utils.extractUrlParam(xlink, "id");

        String code = extractCode(id);

        doUpdateKeyword(xlink, thesaurusName, code, metadataLang, true);

        return Collections.emptySet();
    }

    private String extractCode(String code) throws UnsupportedEncodingException {
        code = URLDecoder.decode(code, "UTF-8");
        int hashIndex = code.indexOf("#", 1) + 1;

        if (hashIndex > 2) {
            code = code.substring(hashIndex);
        }
        return code;
    }

    public boolean isValidated(String href) throws Exception {
        return !href.contains("thesaurus=local._none_.non_validated");
    }

    @Override
    public String toString() {
        return "Reusable Keyword";
    }

    @Override
    public String getInvalidXlinkLuceneField() {
        return "invalid_xlink_keyword";
    }

    @Override
    public String getValidXlinkLuceneField() {
        return "valid_xlink_keyword";
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {

        String decodedHref = URLDecoder.decode(href, "UTF-8");
        if (!decodedHref.toLowerCase().contains("thesaurus=" + NON_VALID_THESAURUS_NAME.toLowerCase())) {
            return href;
        }

        String rawId = Utils.id(href);
        if (rawId != null) {
            String startId = URLDecoder.decode(rawId, "UTF-8");
            if (startId.startsWith(NAMESPACE)) {
                return href;
            }
        }

        String code = UUID.randomUUID().toString();
        Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(NON_VALID_THESAURUS_NAME);

        KeywordBean keywordBean = new KeywordBean(this._isoLanguagesMapper)
                .setNamespaceCode(NAMESPACE)
                .setRelativeCode(code)
                .setValue("", Geocat.DEFAULT_LANG)
                .setDefinition("", Geocat.DEFAULT_LANG);

        String id = URLEncoder.encode(thesaurus.addElement(keywordBean).toString(), "UTF-8");

        return XLink.LOCAL_PROTOCOL + "xml.keyword.get?thesaurus=" + NON_VALID_THESAURUS_NAME + "&id=" + id +
               "&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly";
    }

    @Override
    public Function<String, String> numericIdToConcreteId(final UserSession session) {
        return new Function<String, String>() {
            public String apply(String id) {
                if (!id.contains("=") && !id.contains("&")) {
                    return id;
                }
                try {
                    return URLEncoder.encode(splitUriAndThesaurusName(id).one(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return id;
                }
            }
        };
    }

    @Override
    public Query createFindMetadataQuery(String field, String concreteId, boolean isValidated) {
        BooleanQuery query = new BooleanQuery();
        Term term = new Term(field, WILDCARD_STRING + concreteId + "," + WILDCARD_STRING);
        Term term2 = new Term(field, WILDCARD_STRING + concreteId + "&" + WILDCARD_STRING);
        query.add(new WildcardQuery(term), BooleanClause.Occur.SHOULD);
        query.add(new WildcardQuery(term2), BooleanClause.Occur.SHOULD);
        return query;
    }
}
