package org.fao.geonet.geocat.kernel.reusable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import jeeves.server.context.ServiceContext;

import org.apache.lucene.document.Document;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.jdom.Element;

/**
 * Result from {@link Utils#getReferencingMetadata(jeeves.server.context.ServiceContext, java.util.List, String, boolean, com.google.common.base.Function)}
 * 
 * @author jeichar
 */
public class MetadataRecord
{
    /**
     * Metadata id
     */
    public final Integer id;
    /**
     * Owner of metadata
     */
    public final Integer ownerId;

    /**
     * xml. May be null if loadMetadata in
     * {@link Utils#getReferencingMetadata(jeeves.server.context.ServiceContext, java.util.List, String, boolean, com.google.common.base.Function)} is false
     */
    public final Element xml;

    public final Collection<String> xlinks;
    private String ownerEmail = null;
    private String ownerName = null;
    private XmlSerializer xmlSerializer;

    public MetadataRecord(ServiceContext context, Document element, Collection<String> xlinks, boolean loadMetadata) throws Exception
    {
        this.xmlSerializer = context.getBean(XmlSerializer.class);
        id = Integer.parseInt(element.get("_id"));
        ownerId = Integer.parseInt(element.get("_owner"));
        this.xlinks = Collections.unmodifiableCollection(xlinks);
        if(loadMetadata) {
            Metadata metadata = context.getBean(MetadataRepository.class).findOne(id);
            if (metadata == null) {
                throw new IllegalArgumentException("No metadata found with id: "+id);
            }
            xml = metadata.getXmlData(false);
        } else {
            xml = null;
        }
    }

    public void commit(ServiceContext srvContext) throws Exception
    {
        xmlSerializer.update(""+id, xml, new ISODate().toString(), true, null, srvContext);

        srvContext.getBean(DataManager.class).indexMetadata(""+id, true, true, false, false);
    }

    public String email(UserRepository userRepository) throws SQLException {
        loadOwnerInfo(userRepository);
        return ownerEmail;
    }

    public String name(UserRepository userRepository) throws SQLException {
        loadOwnerInfo(userRepository);
        return ownerName;
    }

    private void loadOwnerInfo(UserRepository userRepository) throws SQLException {
        if(ownerEmail == null) {
            User user = userRepository.findOne(ownerId);
            if(user == null) {
                ownerEmail = "";
                ownerName = "";
            } else {
                ownerEmail = user.getEmail();
                ownerName = user.getName() + " " + user.getSurname();
            }
        }
    }
}
