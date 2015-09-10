package org.fao.geonet.geocat.kernel;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UnpublishInvalidMetadataReport implements Service {

    private static final String MANUAL = "manual";
    private static final String AUTO = "auto";
    private static final String ALL = "all";
    private static final Set<String> INCLUDE_OPTIONS = new HashSet<String>(Arrays.asList(ALL,AUTO,MANUAL));

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        List includes = params.getChildren("include");
        HashSet<String> includeSet = new HashSet<String>();
        Iterator iter = includes.iterator();
        while(iter.hasNext()) {
            Element next = (Element) iter.next();
            String text = next.getTextTrim().toLowerCase();
            includeSet.add(text);
            if(!INCLUDE_OPTIONS.contains(text)) {
                throw new IllegalArgumentException("The legal values for the includes parameter are: "+INCLUDE_OPTIONS);
            } 
        }
        Integer keepDuration = context.getBean(SettingManager.class).getValueAsInt("system/publish_tracking_duration");
        if (keepDuration == null) {
            keepDuration = 100;
        }

        List<PublishRecord> records = UnpublishInvalidMetadataJob.values(context, keepDuration, 0);
        Collections.sort(records, new Comparator<PublishRecord>() {
            @Override
            public int compare(PublishRecord o1, PublishRecord o2) {
                int changeDateComparisonVal = o2.getChangedate().compareTo(o1.getChangedate());
                if (changeDateComparisonVal == 0) {
                    return o2.getChangetime().compareTo(o1.getChangetime());
                }
                return changeDateComparisonVal;
            }
        });
        
        Element report = new Element("report");
        
        Element autoUnpublishedToday = new Element(AUTO+"Unpublish");
        Element manualUnpublishedToday = new Element(MANUAL+"Unpublish");
        Element all = new Element(ALL+"Elements");
        
        if (includeSet.isEmpty() || includeSet.contains(AUTO)) {
            report.addContent(autoUnpublishedToday);
        }
        if (includeSet.isEmpty() || includeSet.contains(MANUAL)) {
            report.addContent(manualUnpublishedToday);
        }
        if (includeSet.isEmpty() || includeSet.contains(ALL)) {
            report.addContent(all);
        }
        
        for(PublishRecord todayRecord : records) {
            all.addContent(todayRecord.asXml());
            
            if(UnpublishInvalidMetadataJob.AUTOMATED_ENTITY.equals(todayRecord.getEntity())) {
                autoUnpublishedToday.addContent(todayRecord.asXml());
            } else {
                manualUnpublishedToday.addContent(todayRecord.asXml());
            }  
        }
        
        return report;
    }

}
