package org.fao.geonet.geocat;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.util.Assert;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

import static org.quartz.TriggerBuilder.newTrigger;

public class ArchiveAllMetadataJob extends QuartzJobBean implements Service {

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private ServiceManager serviceManager;

    static final String BACKUP_FILENAME = "geocat_backup";
    static final String BACKUP_DIR = "geocat_backups";
    public static final String BACKUP_LOG = Geonet.GEONETWORK + ".backup";
    private Path stylePath;
    private AtomicBoolean backupIsRunning = new AtomicBoolean(false);

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.stylePath = appPath.resolve(Geonet.Path.SCHEMAS);
    }

    @Override
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        ServiceContext serviceContext = serviceManager.createServiceContext("unpublishMetadata", context);
        serviceContext.setLanguage("ger");
        serviceContext.setAsThreadLocal();

        ApplicationContextHolder.set(this.context);
        try {
            createBackup(serviceContext);
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Error running " + ArchiveAllMetadataJob.class.getSimpleName(), e);
        }
    }

    @Override
    public Element exec(Element params, ServiceContext context)
            throws Exception {
        final Trigger trigger = newTrigger().forJob("archiveAllMetadata", "geocatBackgroundTasks").startNow().build();
        context.getApplicationContext().getBean("geocatBackgroundJobScheduler", Scheduler.class).scheduleJob(trigger);

        return new Element("ok");
    }


    private void createBackup(ServiceContext serviceContext) throws Exception {
        if (!backupIsRunning.compareAndSet(false, true)) {
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            Log.info(BACKUP_LOG, "Starting backup of all metadata");
            System.out.println("Starting backup of all metadata");

            final MetadataRepository metadataRepository = serviceContext.getBean(MetadataRepository.class);

            loginAsAdmin(serviceContext);
            final Specification<Metadata> harvested = Specifications.where(MetadataSpecs.isHarvested(false)).
                    and(Specifications.not(MetadataSpecs.hasType(MetadataType.SUB_TEMPLATE)));
            List<String> uuids = Lists.transform(metadataRepository.findAll(harvested), new Function<Metadata,
                    String>() {
                @Nullable
                @Override
                public String apply(@Nullable Metadata input) {
                    return input.getUuid();
                }
            });

            Log.info(BACKUP_LOG, "Backing up " + uuids.size() + " metadata");

            String format = "full";
            boolean resolveXlink = true;
            boolean removeXlinkAttribute = false;
            boolean skipOnError = true;
            Path srcFile = MEFLib.doMEF2Export(serviceContext, new HashSet<>(uuids), format, false, stylePath,
                    resolveXlink, removeXlinkAttribute, skipOnError);

            Path datadir = IO.toPath(System.getProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY));
            Path backupDir = datadir.resolve(BACKUP_DIR);
            String today = new SimpleDateFormat("-yyyy-MM-dd").format(new Date());
            Path destFile = backupDir.resolve(BACKUP_FILENAME + today + ".zip");
            IO.deleteFileOrDirectory(backupDir);
            Files.createDirectories(destFile.getParent());
            Files.move(srcFile, destFile);
            if (!Files.exists(destFile)) {
                throw new Exception("Moving backup file failed!");
            }
            long timeMinutes = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
            Log.info(BACKUP_LOG, "Backup finished. Backup time: " + timeMinutes + "  Backup file: " + destFile);
        } catch (Throwable t) {
            Log.error(BACKUP_LOG, "Failed to create a back up of metadata", t);
        } finally {
            backupIsRunning.set(false);
        }
    }

    private void loginAsAdmin(ServiceContext serviceContext) {
        final User adminUser = serviceContext.getBean(UserRepository.class).findAll(UserSpecs.hasProfile(Profile.Administrator), new
                PageRequest(0, 1)).getContent().get(0);
        Assert.isTrue(adminUser != null, "The system does not have an admin user");
        UserSession session = new UserSession();
        session.loginAs(adminUser);
        serviceContext.setUserSession(session);
    }

}
