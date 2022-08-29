package org.fao.geonet.kernel.unpublish;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.MailUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UnpublishNotifier {

    private UserRepository userRepository;
    private SettingManager settingManager;
    private ResourceBundle messages = ResourceBundle.getBundle("geocat.Messages", Locale.getDefault());

    public UnpublishNotifier(UserRepository userRepository, SettingManager settingManager) {
        this.userRepository = userRepository;
        this.settingManager = settingManager;
    }

    public void notifyOwners(List<Metadata> unpublishedRecords) {
        Map<Integer, List<String>> groupedRecords = unpublishedRecords
                .stream()
                .collect(Collectors.groupingBy(
                        x -> x.getSourceInfo().getOwner(),
                        Collectors.mapping(Metadata::getUuid, Collectors.toList())));

        groupedRecords.forEach((ownerId, uuidList) -> {
            User owner = userRepository.getOne(ownerId);
            notifyOwner(owner, uuidList);
        });
    }

    protected void notifyOwner(User owner, List<String> uuids) {
        List<String> toAddress = Collections.singletonList(owner.getEmail());

        String subject = messages.getString("geocat_unpublish_email_subject");
        String htmlMessage = generateEmailBody(owner, uuids);

        MailUtil.sendHtmlMail(toAddress, subject, htmlMessage, settingManager);
    }

    protected String generateEmailBody(User owner, List<String> uuids) {
        String htmlMessage = messages.getString("geocat_unpublish_email_message");

        htmlMessage = htmlMessage.replace("{{userName}}", owner.getUsername());

        // the lines containing metadata UUIDs are repeated for each record included in the notification
        List<String> lines = Arrays.asList(htmlMessage.split("<br>"));
        String repeatedLine = lines.stream().filter(l -> l.indexOf("{{metadataUuid}}") > -1).findFirst().orElse("");

        String newLines = uuids.stream()
                .map(uuid -> repeatedLine.replace("{{metadataUuid}}", uuid))
                .collect(Collectors.joining("<br>"));
        htmlMessage = htmlMessage.replace(repeatedLine, newLines);

        return htmlMessage;
    }
}
