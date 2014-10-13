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
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.geocat.Format;
import org.fao.geonet.domain.geocat.Format_;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.geocat.FormatRepository;
import org.fao.geonet.repository.geocat.specification.FormatSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public final class FormatsStrategy extends ReplacementStrategy
{


    private final String _styleSheet;
    private final FormatRepository _formatRepo;
    private final SearchManager searchManager;


    public FormatsStrategy(ApplicationContext context, String appPath)
    {
        this._formatRepo = context.getBean(FormatRepository.class);
        this.searchManager = context.getBean(SearchManager.class);
        this._styleSheet = appPath + Utils.XSL_REUSABLE_OBJECT_DATA_XSL;
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception
    {

        if (XLink.isXLink(originalElem))
            return NULL;

        Element name = name(originalElem);
        if (name != null && name.getChild("CharacterString", Geonet.Namespaces.GCO) != null) {
            String sname = name.getChildTextTrim("CharacterString", Geonet.Namespaces.GCO);
            Element version = version(originalElem);
            String sversion = "";
            if (version != null && version.getChild("CharacterString", Geonet.Namespaces.GCO) != null) {
                sversion = version.getChildTextTrim("CharacterString", Geonet.Namespaces.GCO);
            }

            Specification<org.fao.geonet.domain.geocat.Format> spec = searchSpec(sname, sversion);

            final org.fao.geonet.domain.geocat.Format format = _formatRepo.findOne(spec);

            if (format != null) {
                Integer id = format.getId();
                boolean validated = format.isValidated();
                xlinkIt(originalElem, ""+id, validated);
                Collection<Element> results = Collections.singleton(originalElem);
                return Pair.read(results, true);
            }
        }

        return NULL;
    }

    private Specification<org.fao.geonet.domain.geocat.Format> searchSpec(final String sname, final String sversion) {
        return new Specification<org.fao.geonet.domain.geocat.Format>() {
            @Override
            public Predicate toPredicate(Root<org.fao.geonet.domain.geocat.Format> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate nameExp = null;
                if (sname != null) {
                    nameExp = cb.equal(cb.lower(cb.trim(root.get(Format_.name))), cb.lower(cb.trim(cb.literal(sname))));
                }
                Predicate versionExp = null;
                if (sversion != null) {
                    versionExp = cb.equal(cb.lower(cb.trim(root.get(Format_.version))), cb.lower(cb.trim(cb.literal(sversion))));
                }
                Predicate finalExp = null;
                if (nameExp != null) {
                    finalExp = nameExp;
                }
                if (versionExp != null) {
                    if (finalExp == null) {
                        finalExp = versionExp;
                    } else {
                        finalExp = cb.and(finalExp, versionExp);
                    }
                }

                if (finalExp == null) {
                    finalExp = cb.equal(cb.literal(1), cb.literal(2));
                }
                return finalExp;
            }
        };
    }

    private Element version(Element originalElem)
    {
        List<Element> version = Utils.convertToList(originalElem.getDescendants(new ElementFinder("version",
                Geonet.Namespaces.GMD, "MD_Format")), Element.class);

        if(version.isEmpty()) return null;
        return version.get(0);
    }

    private Element name(Element originalElem)
    {
        List<Element> name = Utils.convertToList(originalElem.getDescendants(new ElementFinder("name",
                Geonet.Namespaces.GMD, "MD_Format")), Element.class);

        if(name.isEmpty()) {
            return null;
        } else {
            return name.get(0);
        }
    }

    public Element list(UserSession session, boolean validated, String language) throws Exception
    {
        return super.listFromIndex(searchManager, "gmd:MD_Format", validated, language, session, this,
                new Function<DescData, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable DescData data) {
                        String name = data.doc.get("name");
                        if (name == null || name.length() == 0) {
                            name = data.uuid;
                        }
                        String version = data.doc.get("version");
                        if (version == null) {
                            version = "";
                        } else {
                            version = " (" + version + ")";
                        }
                        return name + version;
                    }
                });
    }

    public String createXlinkHref(String id, UserSession session, String notRequired) {
        return XLink.LOCAL_PROTOCOL+"subtemplate?uuid=" + id;
    }

    public void performDelete(String[] ids, UserSession session, String ignored) throws Exception {
        List<Integer> intIds = toIntLists(ids);

        _formatRepo.deleteAll(FormatSpecs.hasIdIn(intIds));
    }

    private List<Integer> toIntLists(String[] ids) {
        return Lists.transform(Arrays.asList(ids), new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable String input) {
                return input == null ? -1 : Integer.parseInt(input);
            }
        });
    }

    public String updateHrefId(String oldHref, String id, UserSession session)
    {
        return createXlinkHref(id, session, null).replace("/___/","/eng/");
    }

    public Map<String, String> markAsValidated(String[] ids, UserSession session) throws Exception {
        List<Integer> intIds = toIntLists(ids);

        _formatRepo.createBatchUpdateQuery(new PathSpec<org.fao.geonet.domain.geocat.Format, Character>() {
            @Override
            public Path<Character> getPath(Root<org.fao.geonet.domain.geocat.Format> root) {
                return root.get(Format_.jpaWorkaround_validated);
            }
        }, Constants.toYN_EnabledChar(true), FormatSpecs.hasIdIn(intIds));

        Map<String, String> idMap = new HashMap<String, String>();

        for (String id : ids) {
            idMap.put(id, id);
        }
        return idMap;
    }

    private void xlinkIt(Element originalElem, String id, boolean validated)
    {
        originalElem.setAttribute(XLink.HREF, XLink.LOCAL_PROTOCOL+"xml.format.get?id=" + id, XLink.NAMESPACE_XLINK);

        if (!validated) {
            originalElem.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        originalElem.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        originalElem.detach();
    }

    public Collection<Element> add(Element placeholder, Element originalElem, String metadataLang)
            throws Exception
    {
        @SuppressWarnings("unchecked")
		List<Element> xml = Xml.transform(originalElem, _styleSheet).getChildren("format");
        if (!xml.isEmpty()) {
            List<Element> results = new ArrayList<Element>();
            for (Element element : xml) {
                String name = element.getAttributeValue("name");
                String version = element.getAttributeValue("version");
                int id = insertNewFormat(name, version);
                Element newElem = (Element) originalElem.clone();
                xlinkIt(newElem, String.valueOf(id), false);
                results.add(newElem);
            }
            return results;
        }
        return Collections.emptySet();
    }

    private int insertNewFormat(String name, String version) throws SQLException {
        if (version == null) {
            version = "";
        }

        final org.fao.geonet.domain.geocat.Format format = new org.fao.geonet.domain.geocat.Format();
        format.setName(name);
        format.setVersion(version);

        final org.fao.geonet.domain.geocat.Format saved = _formatRepo.save(format);

        return saved.getId();
    }

    public Collection<Element> updateObject(Element xlink, String metadataLang) throws Exception
    {
        @SuppressWarnings("unchecked")
		List<Element> xml = Xml.transform((Element) xlink.clone(), _styleSheet).getChildren("format");

        if(!xml.isEmpty()) {
	        Element element = xml.get(0);
	        String id = Utils.extractUrlParam(xlink, "id");
	
	        final String name = element.getAttributeValue("name");
	        String version = element.getAttributeValue("version");
	        if (version == null) {
	            version = "";
	        }

            final String finalVersion = version;
            _formatRepo.update(Integer.parseInt(id), new Updater<org.fao.geonet.domain.geocat.Format>() {
                @Override
                public void apply(@Nonnull org.fao.geonet.domain.geocat.Format entity) {
                    entity.setName(name);
                    entity.setVersion(finalVersion);
                }
            });
        }
        return Collections.emptyList();

    }

    public boolean isValidated(String href) throws Exception
    {
        String id = Utils.id(href);
        if(id==null) return false;
        try {
            int group = Integer.parseInt(id);
            final Specifications<org.fao.geonet.domain.geocat.Format> spec = Specifications.where(FormatSpecs.hasIdIn(Arrays.asList
                    (group))).and(FormatSpecs.isValidated(false));
            long count = _formatRepo.count(spec);

            return count == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "Reusable Format";
    }

    public static final class Formats implements Iterable<Format> {
        List<Format> formats = new ArrayList<Format>();

        public Formats(FormatRepository repo) throws SQLException {
            this.formats = repo.findAll();
        }

        public Iterator<Format> iterator() {
            return formats.iterator();
        }

        public List<Format> matches(Format format) {
            List<Format> matches = new ArrayList<Format>();
            for (Format other : this) {
                if(other.match(format)) {
                    if(other.isValidated()) {
                        matches.add(0, other);
                    } else {
                        matches.add(other);
                    }
                }
            }
            return matches;
        }

        public int size() {
            return formats.size();
        }
    }

    @Override
    public String[] getInvalidXlinkLuceneField() {
        return new String[]{"invalid_xlink_format"};
    }
    
    @Override
    public String[] getValidXlinkLuceneField() {
    	return new String[]{"valid_xlink_format"};
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {

        String startId = Utils.id(href);
        if(startId!=null) return href;

        int id = insertNewFormat("", "");
        return XLink.LOCAL_PROTOCOL+"xml.format.get?id="+id;
    }
}
