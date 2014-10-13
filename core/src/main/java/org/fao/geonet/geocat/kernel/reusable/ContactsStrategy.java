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
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.User_;
import org.fao.geonet.domain.geocat.GeocatUserInfo_;
import org.fao.geonet.domain.geocat.Phone;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.BatchUpdateQuery;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.geocat.specification.GeocatUserSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.GeocatXslUtil;
import org.fao.geonet.util.LangUtils;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.fao.geonet.util.LangUtils.FieldType.STRING;
import static org.fao.geonet.util.LangUtils.FieldType.URL;
import static org.springframework.data.jpa.domain.Specifications.where;

public final class ContactsStrategy extends ReplacementStrategy {
    public static final String LUCENE_ROOT_RESPONSIBLE_PARTY = "che:CHE_CI_ResponsibleParty";
    private final String _styleSheet;
    private final String _appPath;
    private final UserRepository _userRepository;
    private final UserGroupRepository _userGroupRepository;
    private final SearchManager searchManager;

    public ContactsStrategy(ApplicationContext context, String appPath) {
        this._userRepository = context.getBean(UserRepository.class);
        this._userGroupRepository = context.getBean(UserGroupRepository.class);
        _styleSheet = appPath + Utils.XSL_REUSABLE_OBJECT_DATA_XSL;
        this._appPath = appPath;
        this.searchManager = context.getBean(SearchManager.class);
    }

    public static Specification<User> matchSharedUserSpecification(final String email, final String firstName, final String lastName) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                Predicate emailExp = cb.isMember(email, root.get(User_.emailAddresses));
                Predicate firstNameExp = cb.equal(cb.trim(cb.lower(root.get(User_.name))), cb.lower(cb.literal(firstName)));
                Predicate lastNameExp = cb.equal(cb.trim(root.get(User_.surname)), cb.lower(cb.literal(lastName)));

                return cb.or(emailExp, firstNameExp, lastNameExp);
            }
        };
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception
    {
        if (XLink.isXLink(originalElem))
            return NULL;

        String email = lookupElement(originalElem, "electronicMailAddress", defaultMetadataLang);
        String firstname = lookupElement(originalElem, "individualFirstName", defaultMetadataLang);
        String lastname = lookupElement(originalElem, "individualLastName", defaultMetadataLang);

        final List<User> users = _userRepository.findAll(matchSharedUserSpecification(email, firstname, lastname));

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

        User bestFit = null;
        int rating = 0;

        for (User user: users) {

            Element recordOrg = LangUtils.loadInternalMultiLingualElem(user.getOrganisation());

            String elemOrg = lookupElement(originalElem, "organisationName", defaultMetadataLang);

            if (translation(recordOrg, defaultMetadataLang).equalsIgnoreCase(elemOrg)) {
                int newRating = 0;

                HashSet<String> lowerCaseEmails = new HashSet<String>();

                for (String e : user.getEmailAddresses()) {
                    lowerCaseEmails.add(e.trim().toLowerCase());
                }

                if (lowerCaseEmails.contains(email.trim().toLowerCase())) {
                    newRating += 10;
                }

                if (firstname.trim().equals(user.getName())) {
                    newRating += 2;
                }

                if (lastname.trim().equals(user.getSurname())) {
                    newRating += 4;
                }

                if (newRating > rating) {
                    rating = newRating;
                    bestFit = user;
                }

                if (newRating == (10 + 2 + 4)) {
                    break;
                }
            }
        }

        if (bestFit == null) {
            return NULL;
        } else {

            int id = bestFit.getId();
            boolean validated = bestFit.getGeocatUserInfo().isValidated();
            Collection<Element> xlinkIt = xlinkIt(originalElem, role, "" + id, validated);

            return Pair.read(xlinkIt, true);
        }
    }

    @SuppressWarnings("unchecked")
	private String translation(Element elemOrg, String defaultMetadataLang)
    {
        defaultMetadataLang = defaultMetadataLang.substring(0, 2);
        for (Element e : (List<Element>) elemOrg.getChildren()) {
            if (e.getName().equalsIgnoreCase(defaultMetadataLang)) {
                return e.getTextTrim();
            }
        }
        if (elemOrg.getTextTrim().length() > 0) {
            return elemOrg.getTextTrim();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public static String lookupElement(Element originalElem, String name, final String defaultMetadataLang)
    {
        Element elem = Utils.nextElement(originalElem.getDescendants(new ContactElementFinder("CharacterString",
                Geonet.Namespaces.GCO, name)));
        if (elem == null) {
            Iterator<Element> freeTexts = originalElem.getDescendants(new ContactElementFinder("PT_FreeText",
                    Geonet.Namespaces.GMD, name));
            while (freeTexts.hasNext()) {
                Element next = freeTexts.next();
                Iterator<Element> defaultLangElem = next.getDescendants(new Filter()
                {

                    private static final long serialVersionUID = 1L;

                    public boolean matches(Object arg0)
                    {
                        if (arg0 instanceof Element) {
                            Element element = (Element) arg0;
                            return element.getName().equals("LocalisedCharacterString")
                                    && ("#" + defaultMetadataLang)
                                            .equalsIgnoreCase(element.getAttributeValue("locale"));
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
        return  XLink.LOCAL_PROTOCOL+"xml.user.get?id=" + id;
    }
    private Collection<Element> xlinkIt(Element originalElem, String role, String id, boolean validated)
    {
        String schema = "iso19139";
        if (originalElem.getChild("CHE_CI_ResponsibleParty", GeocatXslUtil.CHE_NAMESPACE) != null) {
            schema = "iso19139.che";
        }

        originalElem.removeContent();
        // param order is important, id param must be first
        originalElem.setAttribute(XLink.HREF,baseHref(id) + "&schema=" + schema
                + "&role=" + role, XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem
                    .setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
        return Collections.singleton(originalElem);
    }

    public Collection<Element> add(Element placeholder, Element originalElem, String metadataLang)
            throws Exception {
        UpdateResult result = processQuery(originalElem, null, false, metadataLang);

        return xlinkIt(originalElem, result.role, String.valueOf(result.id), false);
    }


    private static class UpdateResult {
        int id;
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
        Element xml = Xml.transform((Element) originalElem.clone(), _styleSheet);

        // not all strings need to default to a space so they show up in editor.
        // If not then they will be lost
        String email1 = getTextPadEmpty(xml, "email1");
        String name = Utils.getText(xml, "firstName");
        String surname = Utils.getText(xml, "lastName");
        String username = Utils.getText(xml, "email1", UUID.randomUUID().toString());

        if (notEmpty(email1)) {
            username = email1 + "_" + id;
        } else if (notEmpty(name) || notEmpty(surname)) {
            username = name + "_" + surname + "_" + id;
        }

        if (username.trim().length() == 0) {
            username = "" + id;
        }
        username = username.replaceAll("\\s+", "_");

        if (id == null && _userRepository.count(UserSpecs.hasUserName(username)) > 0) {
            username = UUID.randomUUID().toString();
        }

        String passwd = UUID.randomUUID().toString().substring(0, 12);

        String address = Utils.getText(xml, "addressLine");
        String state = Utils.getText(xml, "adminArea");
        String zip = Utils.getText(xml, "postalCode");
        String country = Utils.getText(xml, "country");
        String organ = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("orgName"), STRING);
        String streetnb = Utils.getText(xml, "streetNumber");
        String street = Utils.getText(xml, "streetName");
        String postbox = Utils.getText(xml, "postBox");
        String city = Utils.getText(xml, "city");

        String position = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("position"), STRING);
        String onlineResource = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("online"), URL);
        String onlinename = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("name"), STRING);
        String onlinedesc = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("desc"), STRING);

        String hours = Utils.getText(xml, "hoursOfService");
        String instruct = Utils.getText(xml, "contactInstructions");
        String orgacronym = LangUtils.toInternalMultilingual(metadataLang, _appPath, xml.getChild("acronym"), STRING);
        String role = Utils.getText(xml, "role");


        Integer parentInfo = processParent(originalElem, xml.getChild("parentInfo"), metadataLang);


        User user = new User();
        if (id != null) {
            user.setId(id);
        }

        user.setKind("");
        user.setName(name);
        user.setOrganisation(organ);
        user.getSecurity().setPassword(passwd);
        user.getGeocatUserInfo()
                .setParentInfo(parentInfo)
                .setContactinstructions(instruct)
                .setHoursofservice(hours)
                .setOnlinedescription(onlinedesc)
                .setOnlinename(onlinename)
                .setOnlineresource(onlineResource)
                .setOrgacronym(orgacronym)
                .setPositionname(position)
                .setPublicaccess("y")
                .setValidated(validated);

        List<Element> emails = xml.getChildren("email");

        for (Element email : emails) {
            user.getEmailAddresses().add(email.getTextTrim());
        }

        final List<Element> phones = xml.getChildren("phone");

        for (Element phone : phones) {
            String voice = getTextPadEmpty(phone, "voice");
            String mobile = getTextPadEmpty(phone, "mobile");
            String facsimile = getTextPadEmpty(phone, "facsimile");
            String directNumber = getTextPadEmpty(phone, "directNumber");
            user.getPhones().add(new Phone()
                    .setPhone(voice)
                    .setFacsimile(facsimile)
                    .setDirectnumber(directNumber)
                    .setMobile(mobile));
        }

        user.setProfile(Profile.Shared);
        user.setSurname(surname);
        user.setUsername(username);

        Address addressEntity = new Address()
                .setAddress(address)
                .setZip(zip)
                .setCity(city)
                .setCountry(country)
                .setState(state)
                .setPostbox(postbox)
                .setStreetname(street)
                .setStreetnumber(streetnb);

        user.getAddresses().add(addressEntity);

        user = _userRepository.save(user);

        UpdateResult result = new UpdateResult();
        result.id = user.getId();
        result.role = role;

        return result;
    }

    private Integer processParent(Element original, Element xml, String metadataLang) throws Exception
    {
        if (xml==null || xml.getChildren().isEmpty()) return null;

        Element parentInfo = xml.getChild("CHE_CI_ResponsibleParty", GeocatXslUtil.CHE_NAMESPACE);
        @SuppressWarnings("unchecked")
		Iterator<Content> descendants = original.getDescendants(new ElementFinder("parentResponsibleParty",
                GeocatXslUtil.CHE_NAMESPACE, "CHE_CI_ResponsibleParty"));
		Element toReplace = Utils.nextElement(descendants);


        Integer finalId = null;

        if (toReplace.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK)==null){
            Collection<Element> result;
            Element placeholder = new Element("placeholder");
            Pair<Collection<Element>, Boolean> findResult = find(placeholder, parentInfo, metadataLang);
            if(!findResult.two()){
                UpdateResult afterQuery = processQuery(xml, null, false, metadataLang);

                result = xlinkIt(xml, afterQuery.role, "" + afterQuery.id, false);
            } else {
                result = findResult.one();
            }


            Element xlinkedParent = result.iterator().next();

            try {
                int parsedId = Integer.parseInt(Utils.extractUrlParam(xlinkedParent, "id"));
                Element parent = toReplace.getParentElement();

                toReplace.detach();

                parent.addContent(xlinkedParent);
                finalId = parsedId;

            } catch (NumberFormatException e) {
                Log.error(Geocat.Module.REUSABLE, "Error parsing the id of the parentResponsibleParty: "
                        + Utils.extractUrlParam(xlinkedParent, "id"));
            }
        } else if( !ReusableObjManager.isValidated(toReplace)){
            Processor.uncacheXLinkUri(toReplace.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));

            updateObject((Element)toReplace.clone(), metadataLang);
            int parsedId = Integer.parseInt(Utils.extractUrlParam(toReplace, "id"));
            finalId = parsedId;
        }
        return finalId;

    }

    private String getTextPadEmpty(Element xml, String name)
    {
        String val = Utils.getText(xml, name, null);
        if (val == null)
            return null;
        // need space so xslt from user-xml.xsl doesnt get rid of the element
        if (val.length() == 0)
            return " ";
        return val;
    }

    private boolean notEmpty(String email)
    {
        return email != null && email.trim().length() > 0;
    }

    public Element list(UserSession session, boolean validated, String language) throws Exception {
        return super.listFromIndex(this.searchManager, LUCENE_ROOT_RESPONSIBLE_PARTY, validated, language, session, this,
                new Function<DescData, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable DescData data) {
                        String email = safeField(data.doc, "electronicmailaddress");
                        String name = safeField(data.doc, "individualfirstname");
                        String surname = safeField(data.doc, "individuallastname");

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

    public String createXlinkHref(String id, UserSession session, String notRequired)
    {
        return XLink.LOCAL_PROTOCOL + "subtemplate?" + Params.UUID + "=" + id;
    }

    public String updateHrefId(String oldHref, String id, UserSession session)
    {
        return oldHref.replaceAll("id=\\d+","id="+id).replaceAll("/fra/|/deu/|/ita/|/___/","/eng/");
    }

    public Map<String, String> markAsValidated(String[] ids, UserSession session) throws Exception
    {
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

    public Collection<Element> updateObject(Element xlink, String metadataLang) throws Exception
    {
        int id = Integer.parseInt(Utils.extractUrlParam(xlink, "id"));

        UpdateResult results = processQuery(xlink, id, false, metadataLang);

        String href = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
        int roleIndex = href.indexOf("role=");
        href = href.substring(0, roleIndex) + "role=" + results.role;
        xlink.setAttribute(XLink.HREF, href, XLink.NAMESPACE_XLINK);

        return Collections.emptySet();
    }

    public boolean isValidated(String href) throws NumberFormatException, SQLException
    {
        String id = Utils.id(href);
        if(id==null) return false;
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
    public String toString()
    {
        return "Reusable Contact";
    }

    private static class ContactElementFinder extends ElementFinder {

        public ContactElementFinder(String name, Namespace ns, String parent)
        {
            super(name, ns, parent);
        }

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean otherChecks(Element elem)
        {
            return !isParentResponsibleParty(elem);
        }

        public boolean isParentResponsibleParty(Element elem)
        {
            if(elem == null) return false;
            if(elem.getName().equals("parentResponsibleParty")){
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
        if(startId!=null) return href;

        final String regex = ".+\\?.*role=([^&#]+)&?.*";
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(href);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("cannot find role");
        }
        String role = matcher.group(1);
        
        String username = UUID.randomUUID().toString();
        String email = username+"@generated.org";
        User user = new User();
        user.setUsername(username);
        user.getSecurity().setPassword("");
        user.setProfile(Profile.Shared);
        user.getEmailAddresses().add(email);
        user.getGeocatUserInfo().setValidated(false);

        final User saved = _userRepository.save(user);
        int id = saved.getId();

        return XLink.LOCAL_PROTOCOL+"xml.user.get?id="+id+"&schema=iso19139.che&role="+role;
    }
}
