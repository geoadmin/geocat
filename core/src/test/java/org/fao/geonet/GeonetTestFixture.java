/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet;

import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.DirectoryFactory;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.geotools.data.DataStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.fao.geonet.constants.Geonet.Config.LANGUAGE_PROFILES_DIR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeonetTestFixture {
    private static final FileSystemPool FILE_SYSTEM_POOL = new FileSystemPool();

    private volatile static FileSystemPool.CreatedFs templateFs;
    private volatile static SchemaManager templateSchemaManager;

    @Autowired
    protected DirectoryFactory _directoryFactory;

    @Autowired
    protected DataStore dataStore;

    @Autowired
    private ConfigurableApplicationContext _applicationContext;

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;

    @Autowired
    private LuceneConfig luceneConfig;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ThesaurusManager thesaurusManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private IsoLanguagesMapper isoLanguagesMapper;

    private FileSystemPool.CreatedFs currentFs;

    public void tearDown() {
        IO.setFileSystemThreadLocal(null);
        if (currentFs != null) {
            FILE_SYSTEM_POOL.release(currentFs);
        }
    }

    public void setup(AbstractCoreIntegrationTest test) throws Exception {
        final Path webappDir = AbstractCoreIntegrationTest.getWebappDir(test.getClass());
        TransformerFactoryFactory.init("de.fzi.dbs.xml.transform.CachingTransformerFactory");

        synchronized (GeonetTestFixture.class) {
            if (templateFs == null) {
                if (templateFs == null) {
                    templateFs = FILE_SYSTEM_POOL.getTemplate();

                    Path templateDataDirectory = templateFs.dataDir;
                    IO.copyDirectoryOrFile(webappDir.resolve("WEB-INF/data"), templateDataDirectory, true, new DirectoryStream
                        .Filter<Path>() {
                        @Override
                        public boolean accept(Path entry) throws IOException {
                            return !entry.toString().contains("schema_plugins") &&
                                !entry.getFileName().toString().startsWith(".") &&
                                !entry.getFileName().toString().endsWith(".iml") &&
                                !entry.toString().contains("metadata_data") &&
                                !entry.toString().contains("removed") &&
                                !entry.toString().contains("metadata_subversion") &&
                                !entry.toString().contains("upload") &&
                                !entry.toString().contains("index") &&
                                !entry.toString().contains("resources");
                        }
                    });
                    Path schemaPluginsDir = templateDataDirectory.resolve("config/schema_plugins");
                    deploySchema(webappDir, schemaPluginsDir);
                    LanguageDetector.init(AbstractCoreIntegrationTest.getWebappDir(test.getClass()).resolve(_applicationContext.getBean
                        (LANGUAGE_PROFILES_DIR, String.class)));

                    geonetworkDataDirectory.init(
                            "geonetwork",
                            webappDir,
                            templateDataDirectory,
                            new ServiceConfig(Collections.emptyList()),
                            null);
                    test.addTestSpecificData(geonetworkDataDirectory);

                    templateSchemaManager = initSchemaManager(webappDir, geonetworkDataDirectory);

                    luceneConfig.configure("WEB-INF/config-lucene.xml");
                    searchManager.init(100);
                    Files.createDirectories(templateDataDirectory.resolve("data/resources/htmlcache"));
                }
            }
            isoLanguagesMapper.reinit();
        }

        final String fsName = test.getClass().getSimpleName().replaceAll("[^a-z0-9A-Z]", "") + UUID.randomUUID().toString();
        currentFs = FILE_SYSTEM_POOL.get(fsName);

        assertTrue(Files.isDirectory(currentFs.dataDir.resolve("config")));
        assertTrue(Files.isDirectory(currentFs.dataDir.resolve("data")));

        System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(true));
        configureNodeId(test);


        final GeonetworkDataDirectory dataDir = configureDataDir(test, webappDir, currentFs.dataDir);
        configureNewSchemaManager(dataDir, webappDir);

        // TODO: I don't know why but this corrupts other tests that will fail depending on the run order:
        //  assertCorrectDataDir();
        // for example, running GeonetworkDataDirectoryMultiNodeServiceConfigOnlySystemDataDirSetTest, then
        // GeonetworkDataDirectoryMultiNodeSystemPropertyOnlySystemDataDirSetTest with that line enabled, the second fails.

        if (test.resetLuceneIndex()) {
            _directoryFactory.resetIndex();
        }

        ServiceContext serviceContext = test.createServiceContext();

        ApplicationContextHolder.set(_applicationContext);
        serviceContext.setAsThreadLocal();
        luceneConfig.configure("WEB-INF/config-lucene.xml");
        try {
            searchManager.end();
        } catch (Exception e) {
        }
        searchManager.initNonStaticData(100);
        dataManager.init(serviceContext, false);
        thesaurusManager.init(true, serviceContext, "WEB-INF/data/config/codelist");


        addSourceUUID(dataDir);

        final DataSource dataSource = _applicationContext.getBean(DataSource.class);
        try (Connection conn = dataSource.getConnection()) {
            ThreadUtils.init(conn.getMetaData().getURL(), _applicationContext.getBean(SettingManager.class));
        }
    }


    protected void configureNewSchemaManager(GeonetworkDataDirectory dataDir, Path webappDir) throws Exception {
        schemaManager.configureFrom(templateSchemaManager, webappDir, dataDir);
        assertRequiredSchemas(schemaManager);
    }

    protected void addSourceUUID(GeonetworkDataDirectory dataDirectory) {
        String siteUuid = dataDirectory.getSystemDataDir().getFileName().toString();
        settingManager.setSiteUuid(siteUuid);
        if (sourceRepository.findAll().isEmpty()) {
            sourceRepository.save(new Source().setType(SourceType.portal).setName("Name").setUuid(siteUuid));
        }
    }

    protected SchemaManager initSchemaManager(Path webappDir, GeonetworkDataDirectory geonetworkDataDirectory) throws Exception {
        final Path schemaPluginsDir = geonetworkDataDirectory.getSchemaPluginsDir();
        final Path resourcePath = geonetworkDataDirectory.getResourcesDir();
        Path schemaPluginsCatalogFile = schemaPluginsDir.resolve("schemaplugin-uri-catalog.xml");

        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCatalogFile);
        schemaManager.configure(_applicationContext, webappDir, resourcePath,
            schemaPluginsCatalogFile, schemaPluginsDir, "eng", "iso19139", true);

        assertRequiredSchemas(schemaManager);
        SchemaManager template = new SchemaManager();
        template.configureFrom(schemaManager, webappDir, geonetworkDataDirectory);
        return template;
    }

    private void assertRequiredSchemas(SchemaManager schemaManager) {
        assertTrue(schemaManager.existsSchema("iso19139"));
        assertTrue(schemaManager.existsSchema("dublin-core"));
    }

    protected GeonetworkDataDirectory configureDataDir(AbstractCoreIntegrationTest test, Path webappDir, Path dataDirectory) throws IOException {
        final ServiceConfig serviceConfig = registerServiceConfigAndInitDatastoreTable(test);
        geonetworkDataDirectory.init("geonetwork", webappDir, dataDirectory.toAbsolutePath(), serviceConfig, null);
        return geonetworkDataDirectory;
    }

    protected void configureNodeId(AbstractCoreIntegrationTest test) throws IOException {
        NodeInfo nodeInfo = _applicationContext.getBean(NodeInfo.class);
        nodeInfo.setId(test.getGeonetworkNodeId());
        nodeInfo.setDefaultNode(test.isDefaultNode());

        if (!test.isDefaultNode()) {
            Path dataDir = currentFs.dataDirContainer.resolve(currentFs.dataDir.getFileName() + "_" + test.getGeonetworkNodeId());
            IO.copyDirectoryOrFile(currentFs.dataDir, dataDir, false);
        }
    }

    protected ServiceConfig registerServiceConfigAndInitDatastoreTable(AbstractCoreIntegrationTest test) throws IOException {
        final ArrayList<Element> params = test.getServiceConfigParameterElements();
        final ServiceConfig serviceConfig = new ServiceConfig(params);
        final String initializedString = "initialized";
        try {
            _applicationContext.getBean(initializedString);
        } catch (NoSuchBeanDefinitionException e) {
            _applicationContext.getBeanFactory().registerSingleton("serviceConfig", serviceConfig);
            _applicationContext.getBeanFactory().registerSingleton(initializedString, initializedString);
            AttributeDescriptor geomDescriptor = new AttributeTypeBuilder()
                    .crs(DefaultGeographicCRS.WGS84)
                    .binding(MultiPolygon.class)
                    .buildDescriptor("the_geom");
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("spatialIndex");
            builder.add(geomDescriptor);
            builder.add(SpatialIndexWriter._IDS_ATTRIBUTE_NAME, String.class);
            this.dataStore.createSchema(builder.buildFeatureType());

        }
        return serviceConfig;
    }

    private void deploySchema(Path srcDataDir, Path schemaPluginPath) throws IOException {
        Files.createDirectories(schemaPluginPath);
        // Copy schema plugin
        final String schemaModulePath = "schemas";
        Path schemaModuleDir = srcDataDir.resolve("../../../../").resolve(schemaModulePath).normalize();
        if (Files.exists(schemaModuleDir)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(schemaModuleDir, IO.DIRECTORIES_FILTER)) {
                for (Path path : paths) {
                    final Path srcSchemaPluginDir = path.resolve("src/main/plugin").resolve(path.getFileName());
                    if (Files.exists(srcSchemaPluginDir)) {
                        Path destPath = schemaPluginPath.resolve(path.getFileName().toString());
                        IO.copyDirectoryOrFile(srcSchemaPluginDir, destPath, true);
                    }
                }
            }
        } else {
            throw new AssertionError("Schemas module not found.  this must be a programming error");
        }
    }

    public Path getDataDirContainer() {
        return this.currentFs.dataDirContainer;
    }

    public void assertCorrectDataDir() throws Exception {
        synchronized (GeonetTestFixture.class) {
            final SchemaManager newSM = initSchemaManager(geonetworkDataDirectory.getWebappDir(), geonetworkDataDirectory);

            Xml.loadFile(templateSchemaManager.getSchemaDir("iso19139").resolve("schematron/criteria-type.xml"));
            Xml.loadFile(newSM.getSchemaDir("iso19139").resolve("schematron/criteria-type.xml"));
            Xml.loadFile(schemaManager.getSchemaDir("iso19139").resolve("schematron/criteria-type.xml"));

            assertEquals("Expected Schemas: " + templateSchemaManager.getSchemas() + "\nActual Schemas: " + newSM.getSchemas(),
                templateSchemaManager.getSchemas().size(), newSM.getSchemas().size());
            assertEquals("Expected Schemas: " + templateSchemaManager.getSchemas() + "\nActual Schemas: " + schemaManager.getSchemas(),
                templateSchemaManager.getSchemas().size(), schemaManager.getSchemas().size());
            for (String templateName : templateSchemaManager.getSchemas()) {
                assertTrue(templateName, newSM.existsSchema(templateName));
                assertTrue(templateName, schemaManager.existsSchema(templateName));
                Path thisContextSchemaDir = schemaManager.getSchemaDir(templateName);
                final Path templateSchemaDir = templateSchemaManager.getSchemaDir(templateName);
                Files.walkFileTree(thisContextSchemaDir, new CompareDataDirectory(thisContextSchemaDir, templateSchemaDir));
                Files.walkFileTree(templateSchemaDir, new CompareDataDirectory(templateSchemaDir, thisContextSchemaDir));
            }

            Files.walkFileTree(templateFs.dataDir, new CompareDataDirectory(templateFs.dataDir, geonetworkDataDirectory.getSystemDataDir()));
            Files.walkFileTree(geonetworkDataDirectory.getSystemDataDir(), new CompareDataDirectory(geonetworkDataDirectory.getSystemDataDir(), templateFs.dataDir));
        }
    }

    private static final class CompareDataDirectory extends SimpleFileVisitor<Path> {
        private final Path _dataDirectory;
        private final Path templateDataDirectory;

        public CompareDataDirectory(Path templateDataDirectory, Path dataDirectory) {
            this._dataDirectory = dataDirectory;
            this.templateDataDirectory = templateDataDirectory;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path path = IO.relativeFile(templateDataDirectory, file, _dataDirectory);
            assertTrue(file.toString(), Files.exists(path));
            if (file.getFileName().toString().equals("schemaplugin-uri-catalog.xml")) {
                // this file has different paths so it is naturally different.  check it is non-null and move on
                assertTrue(Files.size(file) > 0);
            } else {
                assertEquals(file.toString(), Files.size(file), Files.size(path));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!dir.getFileName().toString().contains("upload")) {
                final Path path = IO.relativeFile(templateDataDirectory, dir, _dataDirectory);
                assertTrue(dir.toString() + " does not exist as expected: " + path, Files.exists(path));
                return FileVisitResult.CONTINUE;
            } else {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
    }
}
