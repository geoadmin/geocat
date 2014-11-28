package org.fao.geonet.geocat.component;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.geocat.SharedObjectApi;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.fao.geonet.geocat.services.reusable.Reject;
import org.jdom.Element;
import org.springframework.stereotype.Component;

/**
 * Implementation for {@link SharedObjectApi}
 * User: Jesse
 * Date: 11/20/13
 * Time: 9:03 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class SharedObjectApiImpl implements SharedObjectApi {
    @Override
    public Element reject(ServiceContext context, ReusableTypes reusableType, String[] ids, String msg, String strategySpecificData,
                          boolean isValidObject, boolean testing) throws Exception {
        return new Reject().reject(context, reusableType, ids, msg, strategySpecificData, isValidObject, testing);
    }
}
