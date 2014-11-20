package org.fao.geonet.wro4j;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeonetLessCssImportTest {

    public static final GeonetLessCssImport IMPORT = new GeonetLessCssImport();

    @Test
    public void testRemoveBlockComment() throws Exception {
        String comment = "/** comment */";
        final String expected = "\n" +
                                ".gn-top-bar {\n"
                                + "  .visible-lg {\n"
                                + "    display: none !important;\n"
                                + "  }\n" +
                                "*/";
        String text = "/* An update to 3.2 could provide responsive class\n"
                      + "  for block or inline layout.\n"
                      + "  http://getbootstrap.com/css/#responsive-utilities\n"
                      + "\n"
                      + "  Display button label in top tool bar using inline mode.\n"
                      + " */"
                      + "\n//** a non-block comment\n" +
                      ".gn-top-bar {// single line comment\n"
                      + "  .visible-lg {\n"
                      + "    display: none !important;" + comment + "\n"
                      + "  }\n" +
                      "*/";

        final String result = IMPORT.removeBlockComments(text);
        assertEquals(expected, result);
    }
}