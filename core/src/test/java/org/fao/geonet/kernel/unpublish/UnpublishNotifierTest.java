package org.fao.geonet.kernel.unpublish;

import org.fao.geonet.Assert;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.UserRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnpublishNotifierTest {
    UserRepository mockUserRepository;
    SettingManager mockSettingManager;

    @Test
    public void nominalNotifyOwners() {
        mockUserRepository = Mockito.mock(UserRepository.class);
        declareUser("user-one@test.org", "John", "Doe",1);
        declareUser("user-two@test.org", "John", "Doe",2);
        declareUser("user-three@test.org", "John", "Doe",3);
        mockSettingManager = Mockito.mock(SettingManager.class);

        final UnpublishNotifier unpublishNotifierDelegateMock = Mockito.mock(UnpublishNotifier.class);
        UnpublishNotifier toTest = new UnpublishNotifier(mockUserRepository, mockSettingManager)  {
            public void notifyOwner(User owner, List<String> uuids) {
                unpublishNotifierDelegateMock.notifyOwner(owner, uuids);
            }
        };
        List<Metadata> testData = new ArrayList<>();
        addMetadata(testData, "uuid1", 3);
        addMetadata(testData, "uuid2", 3);
        addMetadata(testData, "uuid3", 2);
        addMetadata(testData, "uuid4", 2);
        addMetadata(testData, "uuid5", 1);
        addMetadata(testData, "uuid6", 3);

        toTest.notifyOwners(testData);

        ArgumentCaptor<User> ownerCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<List> uuidCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(unpublishNotifierDelegateMock, Mockito.times(3)).notifyOwner(ownerCaptor.capture(), uuidCaptor.capture());
        Assert.assertEquals("user-one@test.org", ownerCaptor.getAllValues().get(0).getEmail());
        Assert.assertEquals("user-two@test.org", ownerCaptor.getAllValues().get(1).getEmail());
        Assert.assertEquals("user-three@test.org", ownerCaptor.getAllValues().get(2).getEmail());
        Assert.assertEquals("uuid5", uuidCaptor.getAllValues().get(0).get(0));
        Assert.assertEquals("uuid3", uuidCaptor.getAllValues().get(1).get(0));
        Assert.assertEquals("uuid4", uuidCaptor.getAllValues().get(1).get(1));
        Assert.assertEquals("uuid1", uuidCaptor.getAllValues().get(2).get(0));
        Assert.assertEquals("uuid2", uuidCaptor.getAllValues().get(2).get(1));
        Assert.assertEquals("uuid6", uuidCaptor.getAllValues().get(2).get(2));
    }

    @Test
    public void nominalGenerateEmail() {
        mockUserRepository = Mockito.mock(UserRepository.class);
        mockSettingManager = Mockito.mock(SettingManager.class);

        UnpublishNotifier toTest = new UnpublishNotifier(mockUserRepository, mockSettingManager);

        User user = declareUser("john@test.com", "John", "Doe", 101);
        List<String> uuids = Arrays.asList("uuid-invalid-0001", "uuid-invalid-0002", "uuid-invalid-0003", "uuid-invalid-0004");
        String emailBodyToTest = toTest.generateEmailBody(user, uuids);

        Assert.assertEquals(
                "Hi john-doe,<br>" +
                "<br>" +
                "At least one of your metadata records on Geocat.ch were automatically unpublished<br>" +
                "because they were found to be invalid.<br>" +
                "<br>" +
                "The following records were affected:<br>" +
                "- <a href=\"https://www.geocat.ch/geonetwork/metadata/uuid-invalid-0001\">uuid-invalid-0001</a><br>" +
                "- <a href=\"https://www.geocat.ch/geonetwork/metadata/uuid-invalid-0002\">uuid-invalid-0002</a><br>" +
                "- <a href=\"https://www.geocat.ch/geonetwork/metadata/uuid-invalid-0003\">uuid-invalid-0003</a><br>" +
                "- <a href=\"https://www.geocat.ch/geonetwork/metadata/uuid-invalid-0004\">uuid-invalid-0004</a><br>",
                emailBodyToTest);
    }

    private User declareUser(String mail, String firstName, String lastName, int id) {
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getEmail()).thenReturn(mail);
        Mockito.when(mockUser.getName()).thenReturn(firstName);
        Mockito.when(mockUser.getSurname()).thenReturn(lastName);
        Mockito.when(mockUser.getUsername()).thenReturn(String.format("%s-%s", firstName, lastName).toLowerCase());
        Mockito.when(mockUserRepository.findOne(id)).thenReturn(mockUser);
        return mockUser;
    }

    private void addMetadata(List<Metadata> testData, String uuid, int owner) {
        Metadata md = new Metadata();
        md.setUuid(uuid);
        md.getSourceInfo().setOwner(owner);
        testData.add(md);
    }
}
