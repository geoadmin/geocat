package org.fao.geonet.geocat;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.jdom.Element;

/**
 * Interface for sharedObject maangement which provides.
 *
 * User: Jesse
 * Date: 11/19/13
 * Time: 1:29 PM
 */
public interface SharedObjectApi {
    Element reject(ServiceContext context, ReusableTypes reusableType, String[] ids, String msg,
           String strategySpecificData, boolean isValidObject, boolean testing) throws Exception;
}
