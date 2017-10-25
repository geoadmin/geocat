package org.fao.geonet.repository.geocat.specification;

import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.geocat.PublishRecordRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 11/15/13
 * Time: 4:18 PM
 */
public class PublishRecordSpecsTest extends AbstractSpringDataTest {

    @Autowired
    PublishRecordRepository recordRepository;

    AtomicInteger _inc = new AtomicInteger();
    @Test
    public void testSinceDate() throws Exception {
        PublishRecord record1 = newRecord(_inc);
        record1.setChangedate(getCalendar(-1).getTime());
        record1 = recordRepository.save(record1);

        PublishRecord record2 = newRecord(_inc);
        record2.setChangedate(getCalendar(-2).getTime());
        recordRepository.save(record2);

        assertEquals(2, recordRepository.count());

        List<PublishRecord> found = recordRepository.findAll(PublishRecordSpecs.newerThanDate(getCalendar(-1).getTime()));
        assertEquals(0, found.size());

        found = recordRepository.findAll(PublishRecordSpecs.newerThanDate(getCalendar(0).getTime()));
        assertEquals(0, found.size());

        found = recordRepository.findAll(PublishRecordSpecs.newerThanDate(getCalendar(-2).getTime()));
        assertEquals(1, found.size());
        assertEquals(record1.getId(), found.get(0).getId());

        found = recordRepository.findAll(PublishRecordSpecs.newerThanDate(getCalendar(-3).getTime()));
        assertEquals(2, found.size());
    }
    @Test
    public void testDaysOld() throws Exception {
        PublishRecord record1 = newRecord(_inc);
        record1.setChangedate(getCalendar(-1).getTime());
        record1 = recordRepository.save(record1);

        PublishRecord record2 = newRecord(_inc);
        record2.setChangedate(getCalendar(-2).getTime());
        recordRepository.save(record2);

        assertEquals(2, recordRepository.count());

        List<PublishRecord> found = recordRepository.findAll(PublishRecordSpecs.daysOldOrNewer(1));
        assertEquals(0, found.size());

        found = recordRepository.findAll(PublishRecordSpecs.daysOldOrNewer(0));
        assertEquals(0, found.size());

        found = recordRepository.findAll(PublishRecordSpecs.daysOldOrNewer(2));
        assertEquals(1, found.size());
        assertEquals(record1.getId(), found.get(0).getId());

        found = recordRepository.findAll(PublishRecordSpecs.daysOldOrNewer(3));
        assertEquals(2, found.size());
    }
    @Test
    public void testDaysOldOrOlder() throws Exception {
        PublishRecord record1 = newRecord(_inc);
        record1.setChangedate(getCalendar(-1).getTime());
        record1 = recordRepository.save(record1);

        PublishRecord record2 = newRecord(_inc);
        record2.setChangedate(getCalendar(-2).getTime());
        record2 = recordRepository.save(record2);

        assertEquals(2, recordRepository.count());

        List<PublishRecord> found = recordRepository.findAll(PublishRecordSpecs.daysOldOrOlder(3));
        assertEquals(0, found.size());

        found = recordRepository.findAll(PublishRecordSpecs.daysOldOrOlder(2));
        assertEquals(1, found.size());
        assertEquals(record2.getId(), found.get(0).getId());

        found = recordRepository.findAll(PublishRecordSpecs.daysOldOrOlder(1));
        assertEquals(2, found.size());

        found = recordRepository.findAll(PublishRecordSpecs.daysOldOrOlder(0));
        assertEquals(2, found.size());
    }

    private Calendar getCalendar(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, i);
        return calendar;
    }

    private PublishRecord newRecord(AtomicInteger inc) {
        int i = inc.incrementAndGet();
        final PublishRecord record = new PublishRecord();
        record.setChangedate(new Date());
        record.setChangetime(new Date());
        record.setEntity("entity"+i);
        record.setFailurereasons("failure.reasons:"+i);
        record.setFailurerule("failure.rule"+i);
        record.setPublished(i % 2 == 0);
        record.setUuid("uuid"+i);
        record.setValidated(PublishRecord.Validity.values()[i % 3]);

        return record;
    }
}
