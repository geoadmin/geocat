package org.fao.geonet.geocat.kernel.reusable;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 2/19/2015.
 */
public class FormatDescDescriberTest {

    @Test
    public void testFormatDescDescriber() throws Exception {
        final FormatsStrategy.FormatDescFunction describer = new FormatsStrategy.FormatDescFunction();
        Document doc = new Document();

        AbstractSubtemplateStrategy.DescData descData = new AbstractSubtemplateStrategy.DescData("eng", "uuid", "eng", doc);
        assertEquals("(uuid)", describer.apply(descData));

        Document gerDoc = new Document();
        gerDoc.add(new StringField("name", "name", Field.Store.YES));
        descData.langToDoc.put("ger", gerDoc);
        assertEquals("name", describer.apply(descData));

        doc.add(new StringField("version", "version", Field.Store.YES));
        assertEquals("name (version)", describer.apply(descData));
    }
}
