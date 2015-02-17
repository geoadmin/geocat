package org.fao.geonet.geocat.services.reusable;

import com.google.common.collect.Lists;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

/**
 * Get the categories of each Shared object type. IE gn:kantoneBB for extents.
 * Some types do not have categories beyond validated and nonvalidated (IE format)
 * @author Jesse on 2/17/2015.
 */
@Controller
public class Categories {
    @Autowired
    private ExtentManager extentManager;
    @Autowired
    private ThesaurusManager thesaurusManager;

    @RequestMapping(value = "/{lang}/reusable.object.categories/{type}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Collection<String> list(@PathVariable String type) {
        switch (type) {
            case "extents":
                return extentManager.getSource().getTypeDefinitions().keySet();
            case "keywords":
                return thesaurusManager.getThesauriMap().keySet();
            default :
                return Lists.newArrayList();
        }
    }

}
