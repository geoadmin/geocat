package org.fao.geonet.geocat.services.selection;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.geocat.PublishRecordRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Select a list of elements stored in session For all the MD stored in the
 * selectionManager, the service will send an email to the owner of the MD. If
 * the owner hasn't any mail addresse, the email will be sent to ADMIN_MAIL
 * Returns status
 * 
 * @author fgravin
 */

public class Unpublish implements Service {

    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig config) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {

        DataManager dm = context.getBean(DataManager.class);
        UserSession us = context.getUserSession();

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(us);

        Element ret = new Element("response");

        Collection<String> selection = sm.getSelection("metadata");
        synchronized (selection) {
            selection = new LinkedList<String>(selection);
        }
        OperationAllowedRepository operationAllowedRepo = context.getBean(OperationAllowedRepository.class);
        for (Iterator<String> iter = selection.iterator(); iter.hasNext();) {

            String uuid = iter.next();
            String id = dm.getMetadataId(uuid);

            final Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasMetadataId(id);
            final Specification<OperationAllowed> isAllGroup = OperationAllowedSpecs.hasGroupId(ReservedGroup.all.getId());
            final Specification<OperationAllowed> isGuestGroup = OperationAllowedSpecs.hasGroupId(ReservedGroup.guest.getId());
            operationAllowedRepo.deleteAll(Specifications.where(hasMetadataId).and(isAllGroup).and(isGuestGroup));

            final PublishRecord record = new PublishRecord();
            record.setEntity(context.getUserSession().getUsername());
            record.setFailurereasons("");
            record.setFailurerule("manual unpublish by administrator");
            record.setPublished(false);
            record.setUuid(uuid);
            record.setValidated(PublishRecord.Validity.VALID);
            final PublishRecordRepository publishRecordRepository = context.getBean(PublishRecordRepository.class);
            publishRecordRepository.save(record);

            Element retchildserv = new Element("unpublished");
            retchildserv.setAttribute("uuid", uuid);
            ret.addContent(retchildserv);
        }

        return ret;
    }
}

// =============================================================================

