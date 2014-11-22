package org.fao.geonet.geocat.services.selection;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Select a list of elements stored in session For all the MD stored in the
 * selectionManager, the service will send an email to the owner of the MD. If
 * the owner hasn't any mail addresse, the email will be sent to ADMIN_MAIL
 * Returns status
 * 
 * @author fgravin
 */

public class NotifyByMail implements Service {

    private final String ADMIN_MAIL = "geocat@swisstopo.ch";

    private static final Pattern rfc2822 = Pattern
            .compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

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

    @SuppressWarnings("unchecked")
    public Element exec(Element params, ServiceContext context) throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        String messageBody = Util.getParam(params, "body");
        String messageBodyError = Util.getParam(params, "bodyError");
        String messageSubject = Util.getParam(params, "subject");

        UserSession us = context.getUserSession();

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(us);

        Element ret = new Element("response");

        Collection<String> selection = sm.getSelection("metadata");
        synchronized (selection) {
            selection = new LinkedList<String>(selection);
        }

        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        final UserRepository userRepository = context.getBean(UserRepository.class);
        Specifications<Metadata> uuidSpec = null;
        for (String uuid : selection) {
            final Specification<Metadata> hasUuid = MetadataSpecs.hasMetadataUuid(uuid);
            if (uuidSpec == null) {
                uuidSpec = Specifications.where(hasUuid);
            } else {
                uuidSpec = uuidSpec.and(hasUuid);
            }
        }
        final List<Metadata> metadatas = metadataRepository.findAll(uuidSpec);
        for (Metadata metadata : metadatas) {
            String uuid = metadata.getUuid();
            final User user = userRepository.findOne(metadata.getSourceInfo().getOwner());
            String emailAddress = user.getEmail();

            if (emailAddress == null || "".equals(emailAddress) || !rfc2822.matcher(emailAddress).matches()) {
                emailAddress = ADMIN_MAIL;
            }

            context.info("Send notification email to " + emailAddress + " for MD uuid : " + uuid);

            Element retchildserv = new Element("sendMail");
            retchildserv.setAttribute("email", emailAddress);
            retchildserv.setAttribute("uuid", uuid);

            String body = MessageFormat.format(messageBody, uuid);

            try {
                gc.getEmail().send(emailAddress, messageSubject, body, false);
            } catch (Exception e) {
                if (!emailAddress.equals(ADMIN_MAIL)) {
                    gc.getEmail().sendToAdmin(messageSubject, MessageFormat.format(messageBodyError, body), false);
                    retchildserv.setText("error");
                }
            } finally {
                ret.addContent(retchildserv);
            }
        }

        return ret;
    }
}

// =============================================================================

