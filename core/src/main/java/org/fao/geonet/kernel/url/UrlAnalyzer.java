package org.fao.geonet.kernel.url;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.domain.MetadataLinkId_;
import org.fao.geonet.domain.MetadataLink_;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.LinkAwareSchemaPlugin;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static java.util.Objects.isNull;

public class UrlAnalyzer {

    @Autowired
    protected SchemaManager schemaManager;

    @Autowired
    protected MetadataRepository metadataRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    protected UrlChecker urlChecker;

    private SimpleJpaRepository metadataLinkRepository;

    public void init() {
        metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        urlChecker= new UrlChecker();
    }

    public void processMetadata(Element element, AbstractMetadata md) throws org.jdom.JDOMException {
        SchemaPlugin schemaPlugin = schemaManager.getSchema(md.getDataInfo().getSchemaId()).getSchemaPlugin();
        if (schemaPlugin instanceof LinkAwareSchemaPlugin) {

            metadataLinkRepository
                    .findAll(metadatalinksTargetting(md))
                    .stream()
                    .forEach(entityManager::remove);

            ((LinkAwareSchemaPlugin) schemaPlugin).createLinkStreamer(new ILinkBuilder<Link, AbstractMetadata>() {
                @Override
                public Link build() {
                    return new Link();
                }

                @Override
                public void setUrl(Link link, String url) {
                    link.setUrl(url);
                }

                @Override
                public void persist(Link link, AbstractMetadata metadata) {
                    entityManager.persist(link);
                    MetadataLink metadataLink = new MetadataLink();
                    metadataLink.setId(metadataRepository.findOne(metadata.getId()), link);
                    entityManager.persist(metadataLink);
                    entityManager.flush();
                }
            }).processAllRawText(element, md);
        }
    }

    public void purgeMetataLink(Link link) {
        entityManager.detach(link);
        metadataLinkRepository
                .findAll(metadatalinksTargetting(link))
                .stream()
                .filter(metadatalink -> isReferencingAnUnknownMetadata((MetadataLink)metadatalink))
                .forEach(entityManager::remove);
    }

    public void testLink(Link link) {
        LinkStatus linkStatus = urlChecker.getUrlStatus(link.getUrl());
        linkStatus.setLinkId(link.getId());
        entityManager.persist(linkStatus);
    }

    private Specification<MetadataLink> metadatalinksTargetting(Link link) {
        return new Specification<MetadataLink>() {
            @Override
            public Predicate toPredicate(Root<MetadataLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(MetadataLink_.id).get(MetadataLinkId_.linkId), link.getId());
            }
        };
    }

    private Specification<MetadataLink> metadatalinksTargetting(AbstractMetadata md) {
        return new Specification<MetadataLink>() {
            @Override
            public Predicate toPredicate(Root<MetadataLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(MetadataLink_.id).get(MetadataLinkId_.metadataId), md.getId());
            }
        };
    }

    private boolean isReferencingAnUnknownMetadata(MetadataLink metadatalink) {
        return isNull(metadataRepository.findOne(metadatalink.getId().getMetadataId()));
    }
}
