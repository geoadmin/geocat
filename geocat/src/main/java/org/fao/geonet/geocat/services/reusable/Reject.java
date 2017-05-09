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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.geocat.kernel.reusable.ContactsStrategy;
import org.fao.geonet.geocat.kernel.reusable.DeletedObjects;
import org.fao.geonet.geocat.kernel.reusable.ExtentsStrategy;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.fao.geonet.geocat.kernel.reusable.SendEmailParameter;
import org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.geocat.kernel.reusable.Utils.FindXLinks;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.geocat.RejectedSharedObjectRepository;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.transaction.TransactionStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;

/**
 * Makes a list of all the non-validated elements
 *
 * @author jeichar
 */
public class Reject implements Service {

    private DataManager dataManager;
    private MetadataRepository metadataRepository;
    private XmlSerializer xmlSerializer;

    public Element exec(Element params, ServiceContext context) throws Exception {
        String page = Util.getParamText(params, "type");
        String[] ids = Util.getParamText(params, "id").split(",");
        String description = Util.getParam(params, "description", "");
        String msg = Util.getParamText(params, "msg");
        boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));
        boolean isValidObject = Boolean.parseBoolean(Util.getParam(params, "isValidObject", "false"));

        this.dataManager = context.getBean(DataManager.class);
        this.metadataRepository = context.getBean(MetadataRepository.class);
        this.xmlSerializer = context.getBean(XmlSerializer.class);

        String specificData = null;
        if (isValidObject && ReusableTypes.extents.toString().equals(page)) {
            specificData = ExtentsStrategy.XLINK_TYPE;
        }

        return reject(context, ReusableTypes.valueOf(page), ids, msg, description, specificData, isValidObject, testing);
    }

    public Element reject(ServiceContext context, ReusableTypes reusableType, String[] ids, String msg, String description,
                          String strategySpecificData, boolean isValidObject, boolean testing) throws Exception {
        Log.debug(Geocat.Module.REUSABLE, "Starting to reject following reusable objects: \n"
                                          + reusableType + " (" + Arrays.toString(ids) + ")\nRejection message is:\n" + msg);
        UserSession session = context.getUserSession();
        String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), context.getBean(SettingManager.class));
        SharedObjectStrategy strategy = Utils.strategy(reusableType, context);

        Element results = new Element("results");
        if (strategy != null) {
            results.addContent(performReject(ids, strategy, context, session, baseUrl, msg, description,
                    strategySpecificData, isValidObject, testing));
        }
        Log.info(Geocat.Module.REUSABLE, "Successfully rejected following reusable objects: \n"
                                         + reusableType + " (" + Arrays.toString(ids) + ")\nRejection message is:\n" + msg);

        return results;
    }

    private List<Element> performReject(String[] ids, final SharedObjectStrategy strategy, ServiceContext context,
                                        final UserSession session, String baseURL, String rejectionMessage, String desc,
                                        String strategySpecificData, boolean isValidObject, boolean testing) throws Exception {

        final Function<String, String> idConverter = strategy.numericIdToConcreteId(session);

        List<String> luceneFields = new LinkedList<String>();
        if (isValidObject) {
            luceneFields.addAll(Arrays.asList(strategy.getValidXlinkLuceneField()));
        } else {
            luceneFields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));
        }

        Multimap<Integer/* ownerid */, Integer/* metadataid */> emailInfo = HashMultimap.create();
        List<Element> result = new ArrayList<Element>();
        List<String> allAffectedMdIds = new ArrayList<String>();
        for (String id : ids) {
            Set<MetadataRecord> results = Utils.getReferencingMetadata(context, strategy, luceneFields, id, isValidObject, true,
                    idConverter);

            // compile a list of email addresses for notifications
            for (MetadataRecord record : results) {
                emailInfo.put(record.ownerId, record.id);
            }

            Element newIds = updateHrefs(context, desc, rejectionMessage, results);

            for (MetadataRecord metadataRecord : results) {
                String mdId = Integer.toString(metadataRecord.id);
                allAffectedMdIds.add(mdId);

                // Remove validated shared contact from MDs
                if(strategy instanceof ContactsStrategy && isValidObject){

                    // Validated contacts should be deleted from MD
                    Element el = this.dataManager.getGeocatMetadata(context, mdId, false, true);

                    List namespaces = Arrays.asList(
                            Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"),
                            Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink")
                    );

                    // Delete 'gmd:pointOfContact' with 'xlink:href' containing xml.reusable.deleted
                    List<Element> res = Xml.selectNodes(el, "*//gmd:pointOfContact[contains(@xlink:href,'xml.reusable.deleted')]", namespaces);
                    for(Element re : res)
                        re.detach();

                    boolean updateDateStamp = false;
                    this.xmlSerializer.update(mdId, el, null, updateDateStamp, metadataRecord.uuid, context);
                }
            }

            Element e = new Element("idMap").addContent(new Element("oldId").setText(id)).addContent(newIds);
            result.add(e);
        }

        strategy.performDelete(ids, session, strategySpecificData);

        final DataManager dataManager = context.getBean(DataManager.class);
        dataManager.indexMetadata(allAffectedMdIds, true, false);
        context.getBean(SearchManager.class).forceIndexChanges();

        if (!emailInfo.isEmpty()) {
            emailNotifications(strategy, context, session, rejectionMessage, emailInfo, baseURL, strategySpecificData, testing);
        }

        return result;

    }

    private Element updateHrefs(final ServiceContext context, final String oldDesc, final String msg,
                                Set<MetadataRecord> results) throws Exception {
        final Element newIds = new Element("newIds");
        // Move the reusable object to the DeletedObjects table and update
        // the xlink attribute information so that the objects are obtained from that table
        final Map<String/* oldHref */, String/* newHref */> updatedHrefs = new HashMap<String, String>();
        for (final MetadataRecord metadataRecord : results) {
            TransactionManager.runInTransaction("Updating HREFS", context.getApplicationContext(), CREATE_NEW, ALWAYS_COMMIT, false,
                    new TransactionTask<Void>() {
                @Override
                public Void doInTransaction(TransactionStatus transaction) throws Throwable {
                    for (String href : metadataRecord.xlinks) {
                        @SuppressWarnings("unchecked")
                        Iterator<Element> xlinks = metadataRecord.xml.getDescendants(new FindXLinks(href));
                        while (xlinks.hasNext()) {
                            Element xlink = xlinks.next();
                            String oldHRef = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
                            String newHref;
                            if (!updatedHrefs.containsKey(oldHRef)) {
                                Element fragment = Processor.resolveXLink(oldHRef, context);

                                updateChildren(fragment);
                                // update xlink service
                                int newId = DeletedObjects.insert(
                                        context.getBean(RejectedSharedObjectRepository.class),
                                        Xml.getString(fragment), oldDesc + " - " + href, msg);
                                newIds.addContent(new Element("id").setText(String.valueOf(newId)));
                                newHref = DeletedObjects.href(newId);
                                updatedHrefs.put(oldHRef, newHref);
                            } else {
                                newHref = updatedHrefs.get(oldHRef);
                            }

                            // Remove non_validated role value (if necessary) so that
                            // xlink is not editable
                            xlink.removeAttribute(XLink.ROLE, XLink.NAMESPACE_XLINK);
                            xlink.setAttribute(XLink.HREF, newHref, XLink.NAMESPACE_XLINK);
                            xlink.setAttribute(XLink.TITLE, "rejected", XLink.NAMESPACE_XLINK);
                        }
                    }
                    return null;
                }
            });

            metadataRecord.commit(context);
        }

        return newIds;
    }

    private void updateChildren(Element fragment) {
        @SuppressWarnings("unchecked")
        Iterator<Content> iter = fragment.getDescendants();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof Element) {
                Element e = (Element) next;
                e.removeAttribute("href", XLink.NAMESPACE_XLINK);
                e.removeAttribute("show", XLink.NAMESPACE_XLINK);
                e.removeAttribute("role", XLink.NAMESPACE_XLINK);
            }
        }
    }

    private void emailNotifications(final SharedObjectStrategy strategy, ServiceContext context,
                                    final UserSession session, String msg, Multimap<Integer, Integer> emailInfo, String baseURL,
                                    String strategySpecificData, boolean testing) throws Exception {
        if (msg == null) {
            msg = "";
        }

        String msgHeader = LangUtils.translateAndJoin(context.getApplicationContext(), "geocat", "deletedSharedObject_msg",
                "\n\n");
        String subject = LangUtils.translateAndJoin(context.getApplicationContext(), "geocat", "deletedSharedObject_subject",
                " / ");

        Utils.sendEmail(new SendEmailParameter(context, msg, emailInfo, baseURL, msgHeader, subject, testing));
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

}
