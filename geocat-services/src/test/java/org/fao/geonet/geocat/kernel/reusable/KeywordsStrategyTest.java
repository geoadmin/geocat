package org.fao.geonet.geocat.kernel.reusable;

import com.google.common.collect.Maps;
import jeeves.server.UserSession;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.AbstractThesaurusBasedTest;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.fao.geonet.geocat.kernel.reusable.KeywordsStrategy.GEOCAT_THESAURUS_NAME;
import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 2/15/2015.
 */
public class KeywordsStrategyTest extends AbstractThesaurusBasedTest {

    public KeywordsStrategyTest() {
        super(true);
    }

    @Test
    public void testSearch() throws Exception {
        final String className = AbstractThesaurusBasedTest.class.getSimpleName() + ".class";
        Path directory = IO.toPath(AbstractThesaurusBasedTest.class.getResource(className).toURI()).getParent();

        final Thesaurus gcThesaurus = createNewThesaurus(directory, GEOCAT_THESAURUS_NAME);
        final Thesaurus nonValidThesaurusName = createNewThesaurus(directory, KeywordsStrategy.NON_VALID_THESAURUS_NAME);

        ThesaurusFinder thesaurusManager = new ThesaurusFinder() {
            Map<String, Thesaurus> thesauri = Maps.newHashMap();

            {
                thesauri.put(GEOCAT_THESAURUS_NAME, gcThesaurus);
                thesauri.put(KeywordsStrategy.NON_VALID_THESAURUS_NAME, nonValidThesaurusName);
                thesauri.put(thesaurus.getKey(), thesaurus);
            }

            @Override
            public boolean existsThesaurus(String name) {
                return thesauri.containsKey(name);
            }

            @Override
            public Thesaurus getThesaurusByName(String thesaurusName) {
                return this.thesauri.get(thesaurusName);
            }

            @Override
            public Thesaurus getThesaurusByConceptScheme(String conceptSchemeUri) {
                return null;
            }

            @Override
            public Map<String, Thesaurus> getThesauriMap() {
                return this.thesauri;
            }
        };

        Path basePath = AbstractCoreIntegrationTest.getWebappDir(KeywordsStrategyTest.class);
        KeywordsStrategy strategy = new KeywordsStrategy(isoLangMapper, thesaurusManager, basePath,
                "http://localhost:8080/geonetwork", "eng");

        UserSession session = new UserSession();
        doSearch(strategy, session, "99", true, 10);
        doSearch(strategy, session, "999", true, 1);
        doSearch(strategy, session, "99_" + KeywordsStrategy.GEOCAT_THESAURUS_NAME, true, 1);
        doSearch(strategy, session, "99_" + KeywordsStrategy.NON_VALID_THESAURUS_NAME, false, 1);
    }

    private void doSearch(KeywordsStrategy strategy, UserSession session, String searchTerm, boolean validated, int expected) throws Exception {
        final Element search = strategy.search(session, searchTerm, "eng", 10);

        assertEquals(expected, search.getContentSize());

        for (Object o : search.getChildren()) {
            Element e = (Element) o;
            e.getChildText(SharedObjectStrategy.REPORT_DESC).contains(searchTerm);
            e.getChildText(SharedObjectStrategy.REPORT_VALIDATED).equalsIgnoreCase("" + validated);
        }
    }

    private Thesaurus createNewThesaurus(Path directory, String thesaurusName) throws Exception {
        String[] nameParts = thesaurusName.split("\\.", 3);
        Path gcThesaurusFile = directory.resolve(nameParts[2] + ".rdf");
        final Thesaurus gcThesaurus = new Thesaurus(isoLangMapper, gcThesaurusFile.getFileName().toString(), null, null, nameParts[0],
                nameParts[1], gcThesaurusFile, "http://ch.geocat", true);
        Files.deleteIfExists(gcThesaurusFile);
        gcThesaurus.initRepository();
        populateThesaurus(gcThesaurus, 100, gcThesaurus.getDefaultNamespace(), thesaurusName, thesaurusName, "eng", "fre", "ger", "ita");

        return gcThesaurus;
    }
}
