package org.fao.geonet.geocat.services.search;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Source;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SourceRepository;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jesse on 9/30/2014.
 */
public class SearchInfo implements Service {
    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        final List<Group> groups = context.getBean(GroupRepository.class).findAll();
        Element element = new Element("response");
        Element groupsEl = new Element("groups");
        element.addContent(groupsEl);

        for (Group group : groups) {
            if (!group.isReserved()) {
                String name = group.getLabel(context.getLanguage());
                if (name == null) {
                    final Iterator<String> iterator = group.getLabelTranslations().values().iterator();
                    if (iterator.hasNext()) {
                        name = iterator.next();
                    } else {
                        name = group.getName();
                    }
                }

                if (name == null) {
                    name = "No Label";
                }

                groupsEl.addContent(new Element("group").addContent(Arrays.asList(
                                new Element("id").setText(String.valueOf(group.getId())),
                                new Element("name").setText(name))

                ));
            }
        }

        final List<Source> sources = context.getBean(SourceRepository.class).findAll();

        Element sourcesEl = new Element("sources");
        element.addContent(sourcesEl);
        for (Source source : sources) {
            sourcesEl.addContent(new Element("source").addContent(Arrays.asList(
                            new Element("id").setText(source.getUuid()),
                            new Element("name").setText(source.getName()))

            ));
        }
        return element;
    }
}
