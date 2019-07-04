package org.fao.geonet.kernel.url;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.LinkStatusRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkPresenterTest extends AbstractCoreIntegrationTest {


    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    protected LinkStatusRepository linkStatusRepository;

    @Test
    public void errorThenUnknownThenOk() {

        Link linkOk = new Link().setUrl("a i am ok, but ko at the very end");
        linkRepository.save(linkOk);
        Link linkKo = new Link().setUrl("i am ko");
        linkRepository.save(linkKo);
        Link linkProbable = new Link().setUrl("i am probable");
        linkRepository.save(linkProbable);
        entityManager.flush();

        ISODate yesterday = new ISODate();
        yesterday.setDateAndTime("1980-06-03T01:02:03");

        ISODate today = new ISODate();
        today.setDateAndTime("2020-06-03T01:02:03");

        ISODate tomorrow = new ISODate();
        tomorrow.setDateAndTime("2025-06-03T01:02:03");

        LinkStatus statusOk = new LinkStatus()
                .setFailing(false)
                .setStatusValue("200")
                .setStatusInfo("OK")
                .setLinkId(linkOk.getId())
                .setcheckDate(today);


        linkOk.getLinkStatus().add(statusOk);

        LinkStatus statusKo = new LinkStatus()
                .setFailing(true)
                .setStatusValue("400")
                .setStatusInfo("KO")
                .setLinkId(linkKo.getId());
        linkKo.getLinkStatus().add(statusKo);

        linkStatusRepository.save(statusKo);
        linkStatusRepository.save(statusOk);
        entityManager.flush();

        List<Link> allLink = linkRepository.getLinks();

        assertEquals(3, allLink.size());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(1).getLinkStatus().isEmpty());
        assertFalse(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());

        LinkStatus oldStausOk = new LinkStatus()
                .setFailing(true)
                .setStatusValue("400")
                .setStatusInfo("KO")
                .setLinkId(linkOk.getId())
                .setcheckDate(yesterday);
        linkOk.getLinkStatus().add(oldStausOk);

        linkStatusRepository.save(oldStausOk);
        entityManager.flush();
        allLink = linkRepository.getLinks();

        assertEquals(3, allLink.size());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(1).getLinkStatus().isEmpty());
        assertFalse(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[1].isFailing());

        LinkStatus koAtTheEnd = new LinkStatus()
                .setFailing(true)
                .setStatusValue("400")
                .setStatusInfo("KO")
                .setLinkId(linkOk.getId())
                .setcheckDate(tomorrow);
        linkOk.getLinkStatus().add(koAtTheEnd);

        linkStatusRepository.save(koAtTheEnd);
        entityManager.flush();

        allLink = linkRepository.getLinks();

        assertEquals(3, allLink.size());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertFalse(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[1].isFailing());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[2].isFailing());
        assertTrue(allLink.get(1).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(2).getLinkStatus().isEmpty());
    }
}
