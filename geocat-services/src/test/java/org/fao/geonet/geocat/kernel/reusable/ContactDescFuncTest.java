package org.fao.geonet.geocat.kernel.reusable;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.junit.Test;

import static org.fao.geonet.geocat.kernel.reusable.ContactsStrategy.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 2/19/2015.
 */
public class ContactDescFuncTest {

    @Test
    public void testContactDescFuncTest() throws Exception {
        assertCorrectLabel("(uuid)");

        assertCorrectLabel("firstname", td("eng", LUCENE_FIRST_NAME, "firstname"));
        assertCorrectLabel("firstname", td("ger", LUCENE_FIRST_NAME, "firstname"));

        assertCorrectLabel("lastname", td("eng", LUCENE_LAST_NAME, "lastname"));
        assertCorrectLabel("lastname", td("ger", LUCENE_LAST_NAME, "lastname"));

        assertCorrectLabel("first lastname", td("eng", LUCENE_LAST_NAME, "lastname"), td("eng", LUCENE_FIRST_NAME, "first"));
        assertCorrectLabel("first lastname", td("eng", LUCENE_LAST_NAME, "lastname"), td("ger", LUCENE_FIRST_NAME, "first"));
        assertCorrectLabel("first lastname", td("ger", LUCENE_LAST_NAME, "lastname"), td("eng", LUCENE_FIRST_NAME, "first"));

        assertCorrectLabel("lastname (org)", td("eng", LUCENE_LAST_NAME, "lastname"), td("eng", LUCENE_ORG_NAME, "org"));
        assertCorrectLabel("lastname (org)", td("ger", LUCENE_LAST_NAME, "lastname"), td("ger", LUCENE_ORG_NAME, "org"));
        assertCorrectLabel("lastname (org)", td("eng", LUCENE_LAST_NAME, "lastname"), td("ger", LUCENE_ORG_NAME, "org de"),
                td("eng", LUCENE_ORG_NAME, "org"));
        assertCorrectLabel("firstname (org)", td("ger", LUCENE_FIRST_NAME, "firstname"), td("ger", LUCENE_ORG_NAME, "org de"),
                td("eng", LUCENE_ORG_NAME, "org"));
        assertCorrectLabel("first lastname (org)", td("eng", LUCENE_FIRST_NAME, "first"), td("ger", LUCENE_LAST_NAME, "lastname"),
                td("ger", LUCENE_ORG_NAME, "org de"), td("eng", LUCENE_ORG_NAME, "org"));

        assertCorrectLabel("first lastname (email)", td("eng", LUCENE_FIRST_NAME, "first"), td("ger", LUCENE_LAST_NAME, "lastname"),
                td("ger", LUCENE_ORG_NAME, "org de"), td("eng", LUCENE_ORG_NAME, "org"), td("eng", LUCENE_EMAIL, "email"));
        assertCorrectLabel("first lastname (email)", td("eng", LUCENE_FIRST_NAME, "first"), td("ger", LUCENE_LAST_NAME, "lastname"),
                td("ger", LUCENE_ORG_NAME, "org de"), td("eng", LUCENE_ORG_NAME, "org"), td("ger", LUCENE_EMAIL, "email"));

        assertCorrectLabel("org (email)", td("ger", LUCENE_ORG_NAME, "org de"), td("eng", LUCENE_ORG_NAME, "org"),
                td("ger", LUCENE_EMAIL, "email"));

        assertCorrectLabel("email", td("ger", LUCENE_EMAIL, "email"));

        assertCorrectLabel("org", td("ger", LUCENE_ORG_NAME, "org de"), td("eng", LUCENE_ORG_NAME, "org"));

    }

    private void assertCorrectLabel(String expectedValue, TestData... testData) {
        final ContactsStrategy.ContactDescFunc describer = new ContactsStrategy.ContactDescFunc();
        AbstractSubtemplateStrategy.DescData descData = new AbstractSubtemplateStrategy.DescData("eng", "uuid");

        for (TestData data : testData) {
            Document doc = descData.langToDoc.get(data.lang);
            if (doc == null) {
                doc = new Document();
                descData.langToDoc.put(data.lang, doc);
            }

            doc.add(new StringField(data.field, data.value, Field.Store.YES));
        }
        assertEquals(expectedValue, describer.apply(descData));
    }

    TestData td(String lang, String field, String value) {
        TestData td = new TestData();
        td.lang = lang;
        td.field = field;
        td.value = value;

        return td;
    }
    private static class TestData {
        String lang, field, value;
    }
}
