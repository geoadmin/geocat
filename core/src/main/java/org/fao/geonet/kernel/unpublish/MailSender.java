package org.fao.geonet.kernel.unpublish;

import org.apache.commons.mail.EmailException;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.MailUtil;

import java.util.*;

public class MailSender {

    protected UserRepository userRepository;
    protected SettingManager settingManager;

    public void notifyOwners(List<Metadata> unpublishedRecords) {
        HashMap<Integer, List<String>> groupedRecords = new HashMap<>();
        unpublishedRecords.forEach(metadata -> {
            int ownerId = metadata.getSourceInfo().getOwner();
            if (!groupedRecords.containsKey(ownerId)) {
                groupedRecords.put(ownerId, new ArrayList<>());
            }
            groupedRecords.get(ownerId).add(metadata.getUuid());
        });

        groupedRecords.forEach((ownerId, uuidList) -> {
            User owner = userRepository.findOne(ownerId);
            notifyOwner(owner, uuidList);
        });
    }

    public void notifyOwner(User owner, List<String> uuids) {
        List<String> toAddress = new ArrayList<>();
        toAddress.add(owner.getEmail());

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
