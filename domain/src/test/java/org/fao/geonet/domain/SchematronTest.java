package org.fao.geonet.domain;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;

/**
 * Created by Jesse on 1/22/14.
 */
public class SchematronTest {
    @Test
    public void testGetRuleName() throws Exception {
        final Schematron schematron = new Schematron();
        schematron.setFile("xyz/bcd/abc.xsl");

        assertEquals("abc", schematron.getRuleName());

        schematron.setFile(schematron.getFile().replace("/", "\\"));
        assertEquals("abc", schematron.getRuleName());

        schematron.setFile("abc.xsl");
        assertEquals("abc", schematron.getRuleName());
    }
}
