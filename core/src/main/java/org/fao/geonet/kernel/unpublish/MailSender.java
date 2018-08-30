package org.fao.geonet.kernel.unpublish;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.MailUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MailSender {

    protected UserRepository userRepository;
    protected SettingManager settingManager;

    public void notifyOwners(List<Metadata> unpublishedRecords) {
        Map<Integer, List<String>> groupedRecords = unpublishedRecords
                .stream()
                .collect(Collectors.groupingBy(
                        x-> ((Metadata)x).getSourceInfo().getOwner(),
                        Collectors.mapping(Metadata::getUuid, Collectors.toList())));

        groupedRecords.forEach((ownerId, uuidList) -> {
            User owner = userRepository.findOne(ownerId);
            notifyOwner(owner, uuidList);
        });
    }

    public void notifyOwner(User owner, List<String> uuids) {
        List<String> toAddress = Collections.singletonList(owner.getEmail());

        String subject = "Geocat.ch notification of unpublished records";

        String htmlMessage =
                "Hi " + owner.getUsername() + ",<br>" +
                "<br>" +
                "At least one of your metadata records on Geocat.ch were automatically unpublished<br>" +
                "because they were found to be invalid.<br>" +
                "<br>" +
                "The following records were affected:<br>" +
                " - <br>" +
                " - <br>" +
                "";

        MailUtil.sendHtmlMail(toAddress, subject, htmlMessage, settingManager);
    }
}
