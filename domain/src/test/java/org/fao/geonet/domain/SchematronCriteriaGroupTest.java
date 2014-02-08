package org.fao.geonet.domain;

import static org.junit.Assert.*;

import org.jdom.Element;
import org.junit.Test;

import java.util.List;

/**
 * Test schematron criteria
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaGroupTest {

    @Test
    public void testAsXml() throws Exception {
        Schematron schematron = new Schematron();
        schematron.setFile("file");
        schematron.setId(1);
        schematron.setSchemaName("schemaname");

        SchematronCriteria criteria = new SchematronCriteria();
        criteria.setType(SchematronCriteriaType.ALWAYS_ACCEPT);
        criteria.setValue("value");
        criteria.setId(2);

        SchematronCriteriaGroup group = new SchematronCriteriaGroup();
        group.setName("Name")
                .setRequirement(SchematronRequirement.REQUIRED)
                .setSchematron(schematron)
                .addCriteria(criteria);

        Element xml = group.asXml();

        assertNotNull(xml);

        Element criterialist = xml.getChild("criteria");
        assertNotNull(criterialist);
        assertEquals(1, criterialist.getContentSize());
        Element criteriaEl = criterialist.getChild("criteria");
        assertEquals(""+criteria.getId(), criteriaEl.getChildText("id"));
        assertEquals(criteria.getType().name(), criteriaEl.getChildText("type"));
        assertEquals(criteria.getValue(), criteriaEl.getChildText("value"));

        assertEquals(group.getName(), xml.getChildText("name"));
        assertEquals(group.getRequirement().name(), xml.getChildText("requirement"));

        Element schematronEl = xml.getChild("schematron");

        assertEquals(""+schematron.getId(), schematronEl.getChildText("id"));
        assertEquals(""+schematron.getFile(), schematronEl.getChildText("file"));
        assertEquals(""+schematron.getSchemaName(), schematronEl.getChildText("schemaname"));
    }
}
