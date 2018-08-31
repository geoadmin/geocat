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
import java.util.List;

public class UnpublishNotifierTest {
    UserRepository mockUserRepository;
    SettingManager mockSettingManager;

    @Test
    public void nominalNotifyOwners() {
        mockUserRepository = Mockito.mock(UserRepository.class);
        declareUser("user-one@test.org", 1);
        declareUser("user-two@test.org", 2);
        declareUser("user-three@test.org", 3);
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

    private void declareUser(String mail, int id) {
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getEmail()).thenReturn(mail);
        Mockito.when(mockUserRepository.findOne(id)).thenReturn(mockUser);
    }

    private void addMetadata(List<Metadata> testData, String uuid1, int owner) {
        Metadata md = new Metadata();
        md.setUuid(uuid1);
        md.getSourceInfo().setOwner(owner);
        testData.add(md);
    }
}
