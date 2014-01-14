package org.fao.geonet;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.geocat.kernel.reusable.ProcessParams;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Test implementation of ReusableObjManager.
 *
 * Created by Jesse on 1/14/14.
 */
public class ReusableObjManagerTestImpl extends ReusableObjManager {
    @Override
    public void init(List<Element> reusableConfigIter) {
        super.init(reusableConfigIter);
    }

    @Override
    public int process(ServiceContext context, Set<String> elements, DataManager dm, boolean sendEmail, boolean idIsUuid, boolean ignoreErrors) throws Exception {
        return 0;
    }

    @Override
    public List<Element> process(ProcessParams parameterObject) throws Exception, SQLException {
        return Collections.emptyList();
    }

    @Override
    public Collection<Element> updateXlink(Element xlink, ProcessParams params) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public String createAsNeeded(String href, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidated(String href, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException();
    }
}
