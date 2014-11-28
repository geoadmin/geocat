package org.fao.geonet;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.RelatedMetadata;
import org.jdom.Element;

/**
 * A test implementation for related.
 *
 * Created by Jesse on 1/14/14.
 */
public class RelatedMetadataTestImpl implements RelatedMetadata {
    @Override
    public Element getRelated(ServiceContext context, int metadataId, String uuid, String relationType, int from, int to, boolean fast) throws Exception {
        return new Element("related");
    }
}
