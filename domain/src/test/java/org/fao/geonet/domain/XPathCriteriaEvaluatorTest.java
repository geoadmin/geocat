package org.fao.geonet.domain;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test xpath evaluator.
 *
 * Created by Jesse on 2/6/14.
 */
public class XPathCriteriaEvaluatorTest {
    static final Element testMetadata;
    static {
        try {
            testMetadata = Xml.loadString("<geonet>\n"
                           + "    <general>\n"
                           + "        <profiles>../../../web/geonetwork/WEB-INF/user-profiles.xml</profiles>\n"
                           + "        <uploadDir>../../../data/tmp</uploadDir>\n"
                           + "        <maxUploadSize>100</maxUploadSize> <!-- Size must be in megabyte (integer) -->\n"
                           + "        <debug>true</debug>\n"
                           + "    </general>\n"
                           + "\n"
                           + "    <default>\n"
                           + "        <service>main.home</service>\n"
                           + "        <language></language>\n"
                           + "        <localized>true</localized>\n"
                           + "        <contentType>text/html; charset=UTF-8</contentType>\n"
                           + "    </default>\n"
                           + "</geonet>", false);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private static final List<Namespace> NAMESPACES = Collections.emptyList();

    @Test
    public void testAcceptsXPathBoolean() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug/text() = 'true'", testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug/text() = 'false'", testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//missing/text() = 'false'", testMetadata, NAMESPACES));
    }
    @Test
    public void testAcceptsXPathString() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug/text()", testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//language/text()", testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//missing/text()", testMetadata, NAMESPACES));
    }
    @Test
    public void testAcceptsXPathElement() throws Exception {
        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//debug", testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//missing", testMetadata, NAMESPACES));

        assertTrue(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//service[text() = 'main.home']", testMetadata, NAMESPACES));
        assertFalse(XPathCriteriaEvaluator.INSTANCE.accepts(null, "*//service[text() = 'xyz']", testMetadata, NAMESPACES));
    }
    @Test
    public void testAcceptsXPathError() throws Exception {
        final XPathCriteriaEvaluator xPathCriteriaEvaluator = new XPathCriteriaEvaluator() {
            @Override
            protected void warn(String value, Throwable e) {
                // do nothing so that the console stays clean
            }
        };
        assertFalse(xPathCriteriaEvaluator.accepts(null, "*//debug[d = 'da", testMetadata, NAMESPACES));
    }
    @Test
    public void testAcceptsORXPath() throws Exception {
        SchematronCriteria oneGoodOrXPath = XPathCriteriaEvaluator.createOrCriteria("*//language/text()", "*//debug/text()");
        assertTrue(oneGoodOrXPath.accepts(null, testMetadata, NAMESPACES));
        SchematronCriteria twoGoodOrXPath = XPathCriteriaEvaluator.createOrCriteria("*//language", "*//debug/text()");
        assertTrue(twoGoodOrXPath.accepts(null, testMetadata, NAMESPACES));
        SchematronCriteria oneGoodOrXPath2 = XPathCriteriaEvaluator.createOrCriteria("*//debug/text() = 'true'", "*//debug/text()");
        assertTrue(oneGoodOrXPath2.accepts(null, testMetadata, NAMESPACES));

        SchematronCriteria noGoodOrXPath = XPathCriteriaEvaluator.createOrCriteria("*//language/text()", "*//service[text() = 'xyz']");
        assertFalse(noGoodOrXPath.accepts(null, testMetadata, NAMESPACES));
    }
    @Test
    public void testAcceptsANDXPath() throws Exception {
        SchematronCriteria noGoodAndXPath = XPathCriteriaEvaluator.createAndCriteria("*//language/text()", "*//service[text() = 'xyz']");
        assertFalse(noGoodAndXPath.accepts(null, testMetadata, NAMESPACES));

        SchematronCriteria oneGoodAndXPath = XPathCriteriaEvaluator.createAndCriteria("*//debug/text()", "*//service[text() = 'xyz']");
        assertFalse(oneGoodAndXPath.accepts(null, testMetadata, NAMESPACES));

        SchematronCriteria twoGoodAndXPath = XPathCriteriaEvaluator.createAndCriteria("*//debug/text()", "*//service[text() = 'main" +
                                                                                                         ".home']");
        assertTrue(twoGoodAndXPath.accepts(null, testMetadata, NAMESPACES));
    }


    @Test
    public void testAcceptsCreateFromTextANDXPath() throws Exception {
        final Text debugText = (Text) Xml.selectNodes(testMetadata, "*//debug/text()").get(0);
        final Text maxUploadSizeText = (Text) Xml.selectNodes(testMetadata, "*//maxUploadSize/text()").get(0);
        SchematronCriteria criteria = XPathCriteriaEvaluator.createAndCriteria(debugText, maxUploadSizeText);
        assertTrue(criteria.accepts(null, testMetadata, NAMESPACES));

        Element missingDebugMetadata = (Element) testMetadata.clone();
        ((Text) Xml.selectNodes(missingDebugMetadata, "*//debug/text()").get(0)).detach();
        assertFalse(criteria.accepts(null, missingDebugMetadata, NAMESPACES));
    }

    @Test
    public void testAcceptsCreateFromTextOrXPath() throws Exception {
        final Text debugText = (Text) Xml.selectNodes(testMetadata, "*//debug/text()").get(0);
        final Text maxUploadSizeText = (Text) Xml.selectNodes(testMetadata, "*//maxUploadSize/text()").get(0);
        SchematronCriteria criteria = XPathCriteriaEvaluator.createOrCriteria(debugText, maxUploadSizeText);
        assertTrue(criteria.accepts(null, testMetadata, NAMESPACES));

        Element missingDebugMetadata = (Element) testMetadata.clone();
        ((Text) Xml.selectNodes(missingDebugMetadata, "*//debug/text()").get(0)).detach();
        assertTrue(criteria.accepts(null, missingDebugMetadata, NAMESPACES));

        ((Text) Xml.selectNodes(missingDebugMetadata, "*//maxUploadSize/text()").get(0)).detach();
        assertFalse(criteria.accepts(null, missingDebugMetadata, NAMESPACES));
    }


}
