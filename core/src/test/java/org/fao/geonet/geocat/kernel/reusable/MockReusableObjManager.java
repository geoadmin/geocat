package org.fao.geonet.geocat.kernel.reusable;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.geocat.kernel.reusable.ProcessParams;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Jesse on 11/5/2014.
 */
public class MockReusableObjManager extends ReusableObjManager {
    @Override
    public int process(ServiceContext context, Set<String> elements, DataManager dm, boolean sendEmail, boolean idIsUuid, boolean
            ignoreErrors) throws Exception {
        return 0;
    }

    @Override
    public List<Element> process(ProcessParams parameterObject) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public Collection<Element> updateXlink(Element xlink, ProcessParams params) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public String createAsNeeded(String href, ServiceContext context) throws Exception {
        return href;
    }

    @Override
    public boolean isValidated(String href, ServiceContext context) throws Exception {
        return false;
    }
}
