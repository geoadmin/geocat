package org.fao.geonet.kernel;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.repository.BatchUpdateQuery;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.fao.geonet.utils.Log;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Set;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 * Copy of BatchOpsMetadataReindexer but updates the changedate.
 *
 * @author Jesse on 4/29/2015.
 */
public class UpdateReferencedMetadata implements Runnable {

    private final String uuid;
    private final DataManager dm;
    private final SharedObjectStrategy strategy;

    public UpdateReferencedMetadata(String uuid, DataManager dm, SharedObjectStrategy strategy) {
        this.uuid = uuid;
        this.dm = dm;
        this.strategy = strategy;
    }

    public void call() throws Exception {
        final ServiceContext context = dm.getServiceContext();
        context.setAsThreadLocal();

        final Set<Integer> mdIds = Sets.newHashSet();
        TransactionManager.runInTransaction("Update Referenced Metadata Task", context.getApplicationContext(),
                TransactionManager.TransactionRequirement.CREATE_NEW,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false, new TransactionTask<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus transaction) throws Throwable {

                        List<String> luceneFields = Lists.newArrayList(strategy.getValidXlinkLuceneField(),
                                strategy.getInvalidXlinkLuceneField());

                        Set<MetadataRecord> referencingMetadata = Utils.getReferencingMetadata(context, strategy, luceneFields,
                                uuid, null, false, Functions.<String>identity());
                        if (!referencingMetadata.isEmpty()) {
                            for (MetadataRecord metadataRecord : referencingMetadata) {
                                mdIds.add(metadataRecord.id);
                            }

                            MetadataRepository repository = context.getBean(MetadataRepository.class);
                            BatchUpdateQuery<Metadata> query = repository.createBatchUpdateQuery(new PathSpec<Metadata, ISODate>() {
                                @Override
                                public Path<ISODate> getPath(Root<Metadata> root) {
                                    return root.get(Metadata_.dataInfo).get(MetadataDataInfo_.changeDate);
                                }
                            }, new ISODate());
                            query.setSpecification(MetadataSpecs.hasMetadataIdIn(mdIds));
                            query.execute();
                        }
                        return null;
                    }
                });
        dm.flush();
        TransactionManager.runInTransaction("IndexingTask", context.getApplicationContext(),
                TransactionManager.TransactionRequirement.CREATE_NEW,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false, new TransactionTask<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus transaction) throws Throwable {

                        for (Integer id : mdIds) {
                            dm.indexMetadata(id + "", false);
                        }
                        return null;
                    }
                });
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Error indexing metadata", e);
        }
    }
}
