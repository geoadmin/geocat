package org.fao.geonet.services.subtemplate;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.facet.Dimension;
import org.fao.geonet.kernel.search.facet.ItemConfig;
import org.fao.geonet.kernel.search.facet.SummaryType;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 *
 * @author fgravin on 02/20/2017.
 */
@Controller("subtamplates/updateprivileges")
public class UpdatePrivileges {

    private int GROUP_ID = 99999;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    MetadataRepository metadataRepository;

    @RequestMapping(value = "/{lang}/subtamplates/updateprivileges", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Map<String, String> update() {

        Group subtemplateGroup = groupRepository.findOne(GROUP_ID);

        // Set all users as Reviewer in SUBTEMPLATES group
        for(User user : userRepository.findAll()) {
            UserGroup userGroup = new UserGroup();
            userGroup.setUser(user);
            userGroup.setGroup(subtemplateGroup);
            userGroup.setProfile(Profile.Reviewer);
            userGroupRepository.save(userGroup);
        }

        // Set all subtemplates opening/editing privileges for SUBTEMPLATES group
        final Specification<Metadata> spec = where(MetadataSpecs.isType(MetadataType.SUB_TEMPLATE));
        for(Integer id : metadataRepository.findAllIdsBy(spec)) {
            Optional<OperationAllowed> opAllowedEditing = Optional.of(new OperationAllowed(new OperationAllowedId().setGroupId(GROUP_ID).setMetadataId(id).setOperationId(0)));
            Optional<OperationAllowed> opAllowedView = Optional.of(new OperationAllowed(new OperationAllowedId().setGroupId(GROUP_ID).setMetadataId(id).setOperationId(2)));
            operationAllowedRepository.save(opAllowedEditing.get());
            operationAllowedRepository.save(opAllowedView.get());
        }
        Map<String, String> results = Maps.newLinkedHashMap();
        results.put("success", "true");

        return results;
    }

}