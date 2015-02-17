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

package org.fao.geonet.geocat.services.reusable;

import com.google.common.base.Function;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.reusable.ContactsStrategy;
import org.fao.geonet.geocat.kernel.reusable.DeletedObjects;
import org.fao.geonet.geocat.kernel.reusable.ExtentsStrategy;
import org.fao.geonet.geocat.kernel.reusable.FormatsStrategy;
import org.fao.geonet.geocat.kernel.reusable.KeywordsStrategy;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.geocat.RejectedSharedObjectRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Deletes the objects from deleted reusable object table and unpublishes the
 * referencing metadata
 *
 * @author jeichar
 */
public class DeleteUnused implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        Path appPath = context.getAppPath();
        String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), context.getBean(SettingManager.class));
        String language = context.getLanguage();
        try {
            final IsoLanguagesMapper isoLanguagesMapper = context.getBean(IsoLanguagesMapper.class);
            final ThesaurusManager thesaurusMan = context.getBean(ThesaurusManager.class);

            process(new ContactsStrategy(context.getApplicationContext()), context);
            process(new ExtentsStrategy(appPath, context.getBean(ExtentManager.class), language), context);
            process(new FormatsStrategy(context.getApplicationContext()), context);
            process(new KeywordsStrategy(isoLanguagesMapper, thesaurusMan, appPath, baseUrl, language), context);
            processDeleted(context);

            return new Element("status").setText("true");
        } catch (Throwable e) {
            return new Element("status").setText("false");
        }
    }

    private void process(SharedObjectStrategy strategy, ServiceContext context) throws Exception {
        UserSession userSession = context.getUserSession();
        @SuppressWarnings("unchecked")
        List<Element> nonValidated = strategy.list(userSession, SharedObjectStrategy.LUCENE_EXTRA_NON_VALIDATED, context.getLanguage()).getChildren();
        List<String> toDelete = new ArrayList<String>();
        final Function<String, String> idConverter = strategy.numericIdToConcreteId(userSession);

        List<String> luceneFields = new LinkedList<String>();
        luceneFields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));

        for (Element element : nonValidated) {
            String objId = element.getChildTextTrim(SharedObjectStrategy.REPORT_ID);

            Set<MetadataRecord> md = Utils.getReferencingMetadata(context, strategy, luceneFields, objId, false, false, idConverter);
            if (md.isEmpty()) {
                toDelete.add(objId);
            }
        }
        Log.info(Geocat.Module.REUSABLE, "Deleting Reusable objects " + toDelete);
        if (toDelete.size() > 0)
            strategy.performDelete(toDelete.toArray(new String[toDelete.size()]), userSession, null);
    }

    private void processDeleted(ServiceContext context) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> nonValidated = DeletedObjects.list(context.getBean(RejectedSharedObjectRepository.class)).getChildren();
        List<Integer> toDelete = new ArrayList<Integer>();
        final Function<String, String> idConverter = SharedObjectStrategy.ID_FUNC;

        List<String> fields = Arrays.asList(DeletedObjects.getLuceneIndexField());

        for (Element element : nonValidated) {
            String objId = element.getChildTextTrim(SharedObjectStrategy.REPORT_ID);

            Set<MetadataRecord> md = Utils.getReferencingMetadata(context, DeletedObjects.createFindMetadataReferences(), fields, objId,
                    false, false, idConverter);
            if (md.isEmpty()) {
                toDelete.add(Integer.parseInt(objId));
            }
        }
        if (toDelete.size() > 0)
            DeletedObjects.delete(context, toDelete.toArray(new Integer[toDelete.size()]));
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

}
