package org.fao.geonet.geocat.kernel.reusable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jeeves.server.UserSession;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.AbstractThesaurusBasedTest;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.geocat.kernel.reusable.KeywordsStrategy.GEOCAT_THESAURUS_NAME;
import static org.fao.geonet.geocat.kernel.reusable.KeywordsStrategy.NON_VALID_THESAURUS_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 2/15/2015.
 */
public class KeywordsStrategyTest extends AbstractThesaurusBasedTest {

    public static final int GEOCAT_THES_WORDS = 100;
    private Thesaurus gcThesaurus;
    private Thesaurus nonValidThesaurusName;
    private KeywordsStrategy strategy;
    private UserSession session;

    public KeywordsStrategyTest() {
        super(true);
    }

    @Before
    public void setUp() throws Exception {
        final String className = AbstractThesaurusBasedTest.class.getSimpleName() + ".class";
        Path directory = IO.toPath(AbstractThesaurusBasedTest.class.getResource(className).toURI()).getParent();

        this.gcThesaurus = createNewThesaurus(directory, GEOCAT_THESAURUS_NAME);
        this.nonValidThesaurusName = createNewThesaurus(directory, KeywordsStrategy.NON_VALID_THESAURUS_NAME);

        final ThesaurusFinder thesaurusManager = new ThesaurusFinder() {
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
        this.strategy = new KeywordsStrategy(isoLangMapper, thesaurusManager, basePath,
                "http://localhost:8080/geonetwork", "eng");

        this.session = new UserSession();


    }

    @Test
    public void testSearch() throws Exception {
        doSearch(strategy, session, "99", true, 10);
        doSearch(strategy, session, "999", true, 1);
        doSearch(strategy, session, "99_" + KeywordsStrategy.GEOCAT_THESAURUS_NAME, true, 1);
        doSearch(strategy, session, "99_" + KeywordsStrategy.NON_VALID_THESAURUS_NAME, false, 1);
    }

    @Test
    public void testList() throws Exception {
        Element list = this.strategy.list(session, SharedObjectStrategy.LUCENE_EXTRA_NON_VALIDATED, "eng", 3000);
        assertEquals(GEOCAT_THES_WORDS, list.getContentSize());
        for (Object o : list.getChildren()) {
            Element element = (Element) o;

            assertTrue(element.getChildText("id").contains(NON_VALID_THESAURUS_NAME));
            assertTrue(element.getChildText("validated").contains("false"));
        }

        list = this.strategy.list(session, SharedObjectStrategy.LUCENE_EXTRA_VALIDATED, "eng", 3000);
        assertEquals(GEOCAT_THES_WORDS, list.getContentSize());
        for (Object o : list.getChildren()) {
            Element element = (Element) o;

            assertTrue(element.getChildText("id").contains(GEOCAT_THESAURUS_NAME));
            assertTrue(element.getChildText("validated").contains("true"));
        }

        list = this.strategy.list(session, GEOCAT_THESAURUS_NAME, "eng", 3000);
        assertEquals(GEOCAT_THES_WORDS, list.getContentSize());

        list = this.strategy.list(session, NON_VALID_THESAURUS_NAME, "eng", 3000);
        assertEquals(GEOCAT_THES_WORDS, list.getContentSize());

        list = this.strategy.list(session, null, "eng", 3000);
        assertEquals(2 * GEOCAT_THES_WORDS + keywords, list.getContentSize());

        list = this.strategy.list(session, null, "eng", 10);
        assertEquals(10, list.getContentSize());
    }

    @Test
    public void testMarkAsValidated() throws Exception {
        final Element found = this.strategy.search(session, null, "9" + NON_VALID_THESAURUS_NAME, "eng", 100);
        List<String> ids = Lists.newArrayList();
        for (Object o : found.getContent()) {
            Element e = (Element) o;
            ids.add(e.getChildText("id"));
        }
        this.strategy.markAsValidated(ids.toArray(new String[ids.size()]), session);

        assertEquals(GEOCAT_THES_WORDS - ids.size(), this.strategy.list(session, GEOCAT_THESAURUS_NAME, "eng", 3000).getContentSize());
        assertEquals(GEOCAT_THES_WORDS + ids.size(), this.strategy.list(session, NON_VALID_THESAURUS_NAME, "eng", 3000).getContentSize());
    }

    @Test
    public void testDelete() throws Exception {
        final Element found = this.strategy.search(session, null, "9" + NON_VALID_THESAURUS_NAME, "eng", 100);
        List<String> ids = Lists.newArrayList();
        for (Object o : found.getContent()) {
            Element e = (Element) o;
            ids.add(e.getChildText("id"));
        }
        this.strategy.performDelete(ids.toArray(new String[ids.size()]), session, GEOCAT_THESAURUS_NAME);

        assertEquals(GEOCAT_THES_WORDS - ids.size(), this.strategy.list(session, GEOCAT_THESAURUS_NAME, "eng", 3000).getContentSize());
        assertEquals(GEOCAT_THES_WORDS, this.strategy.list(session, NON_VALID_THESAURUS_NAME, "eng", 3000).getContentSize());
    }

    private void doSearch(KeywordsStrategy strategy, UserSession session, String searchTerm, boolean validated, int expected) throws Exception {
        final Element search = strategy.search(session, null, searchTerm, "eng", 10);

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
        populateThesaurus(gcThesaurus, GEOCAT_THES_WORDS, gcThesaurus.getDefaultNamespace(), thesaurusName, thesaurusName, "eng", "fre", "ger", "ita");

        return gcThesaurus;
    }
}
