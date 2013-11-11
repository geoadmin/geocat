package org.fao.geonet.component.csw;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.GeonetworkExtension;
import org.jdom.Element;

/**
 * An extension point to allow plugins to transform the metadata returned by {@link GetRecordById}
 *
 * User: Jesse
 * Date: 11/7/13
 * Time: 3:23 PM
 */
public interface GetRecordByIdMetadataTransformer extends GeonetworkExtension {
    public boolean isApplicable(ServiceContext context, Element metadata, OutputSchema outputSchema);
    public Element apply(ServiceContext context, Element md, OutputSchema outputSchema) throws CatalogException;
}
