/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.kernel.unpublish;

import com.google.common.collect.Maps;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UnpublishInvalidMetadataReport implements Service {

    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";
    private static final String ALL = "all";
    private static final Set<String> INCLUDE_OPTIONS = new HashSet<String>(Arrays.asList(ALL,AUTO,MANUAL));

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        List includes = params.getChildren("include");
        HashSet<String> includeSet = new HashSet<String>();
        Iterator iter = includes.iterator();
        while(iter.hasNext()) {
            Element next = (Element) iter.next();
            String text = next.getTextTrim().toLowerCase();
            includeSet.add(text);
            if(!INCLUDE_OPTIONS.contains(text)) {
                throw new IllegalArgumentException("The legal values for the includes parameter are: "+INCLUDE_OPTIONS);
            }
        }
        Integer keepDuration = context.getBean(SettingManager.class).getValueAsInt("system/metadata/publish_tracking_duration");
        if (keepDuration == null) {
            keepDuration = 100;
        }

        List<PublishRecord> records = UnpublishInvalidMetadataJob.values(context, keepDuration, 0);
        Collections.sort(records, new Comparator<PublishRecord>() {
            @Override
            public int compare(PublishRecord o1, PublishRecord o2) {
                int changeDateComparisonVal = o2.getChangedate().compareTo(o1.getChangedate());
                if (changeDateComparisonVal == 0) {
                    return o2.getChangetime().compareTo(o1.getChangetime());
                }
                return changeDateComparisonVal;
            }
        });

        Element report = new Element("report");

        Element autoUnpublishedToday = new Element(AUTO+"Unpublish");
        Element manualUnpublishedToday = new Element(MANUAL+"Unpublish");
        Element all = new Element(ALL+"Elements");

        if (includeSet.isEmpty() || includeSet.contains(AUTO)) {
            report.addContent(autoUnpublishedToday);
        }
        if (includeSet.isEmpty() || includeSet.contains(MANUAL)) {
            report.addContent(manualUnpublishedToday);
        }
        if (includeSet.isEmpty() || includeSet.contains(ALL)) {
            report.addContent(all);
        }

        for(PublishRecord todayRecord : records) {
            all.addContent(todayRecord.asXml());

            if(UnpublishInvalidMetadataJob.AUTOMATED_ENTITY.equals(todayRecord.getEntity())) {
                autoUnpublishedToday.addContent(todayRecord.asXml());
            } else {
                manualUnpublishedToday.addContent(todayRecord.asXml());
            }
        }

        Map<String, String> sourceAndGroupToLogo = Maps.newHashMap();

        @SuppressWarnings("unchecked")
        List groupsAndSources = Xml.selectNodes(all, "*//record|record");

        Map<String, String> nameMap = Maps.newHashMap();
        Resources resources = context.getBean(Resources.class);

        for (Object obj : groupsAndSources) {
            Element el = (Element) obj;
            String groupOwner = el.getChildText("groupOwner");

            String logoUUID = null;
            String logoUrl = null;
            String name = null;
            if (groupOwner != null) {
                logoUrl = sourceAndGroupToLogo.get(groupOwner);
                if (logoUrl == null) {
                    GroupRepository groupRepository = context.getBean(GroupRepository.class);
                    final Group group = groupRepository.getOne(Integer.parseInt(groupOwner));
                    logoUUID = group.getLogo();
                    nameMap.put(groupOwner, group.getName());
                }
                name = nameMap.get(groupOwner);
            }

            String source = el.getChildText("source");
            if (logoUUID == null && logoUrl == null) {
                if (source != null) {
                    logoUrl = sourceAndGroupToLogo.get(source);
                }
            }

            if (name == null && source != null) {
                name = "Site: " + source;
            }

            if (logoUrl == null && logoUUID != null) {
                final Path logosDir = resources.locateHarvesterLogosDir(context);
                final Path logoPath = logosDir.resolve(logoUUID);
                if (Files.exists(logoPath)) {
                    logoUrl = "/images/harvesting/" + logoUUID;
                    sourceAndGroupToLogo.put(logoUUID, logoUrl);
                }
            }

            if (logoUrl != null) {
                el.addContent(new Element("logo").setText(logoUrl));
            }

            if (name != null) {
                el.addContent(new Element("sourceName").setText(name));
            }
        }


        return report;
    }

}
