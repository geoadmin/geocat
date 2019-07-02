package org.fao.geonet.kernel.url;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.domain.Link_;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.domain.MetadataLink_;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.LinkAwareSchemaPlugin;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.LinkStatusRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

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

    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected LinkStatusRepository linkStatusRepository;

    @Autowired
    protected MetadataLinkRepository metadataLinkRepository;

    public void init() {
        urlChecker= new UrlChecker();
    }

    public void processMetadata(Element element, AbstractMetadata md) throws org.jdom.JDOMException {
        SchemaPlugin schemaPlugin = schemaManager.getSchema(md.getDataInfo().getSchemaId()).getSchemaPlugin();
        if (schemaPlugin instanceof LinkAwareSchemaPlugin) {

            metadataLinkRepository
                    .findAll(metadatalinksTargetting(md))
                    .stream()
                    .forEach(mdl -> {mdl.getLink().getRecords().remove(mdl); entityManager.persist(mdl);});

            ((LinkAwareSchemaPlugin) schemaPlugin).createLinkStreamer(new ILinkBuilder<Link, AbstractMetadata>() {

                @Override
                public Link found(String url) {
                    Link link = linkRepository.findOneByUrl(url);
                    if (link != null) {
                        return link;
                    } else {
                        link = new Link();
                        link.setUrl(url);
                        linkRepository.save(link);
                        return link;
                    }
                }

                @Override
                public void persist(Link link, AbstractMetadata metadata) {
                    MetadataLink metadataLink = new MetadataLink();
                    metadataLink.setMetadataId(new Integer(metadata.getId()));
                    metadataLink.setMetadataUuid(metadata.getUuid());
                    metadataLink.setLink(link);
                    link.getRecords().add(metadataLink);
                    linkRepository.save(link);
                }
            }).processAllRawText(element, md);
            entityManager.flush();
        }
    }

    public void purgeMetataLink(Link link) {
        metadataLinkRepository
                .findAll(metadatalinksTargetting(link))
                .stream()
                .filter(metadatalink -> isReferencingAnUnknownMetadata((MetadataLink)metadatalink))
                .forEach(metadataLinkRepository::delete);
        entityManager.flush();
    }

    public void deleteAll() {
        linkRepository.deleteAll();
        entityManager.flush();
    }

    public void testLink(Link link) {
        LinkStatus linkStatus = urlChecker.getUrlStatus(link.getUrl());
        linkStatus.setLinkId(link.getId());
        linkStatusRepository.save(linkStatus);
    }

    private Specification<MetadataLink> metadatalinksTargetting(Link link) {
        return new Specification<MetadataLink>() {
            @Override
            public Predicate toPredicate(Root<MetadataLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(MetadataLink_.link).get(Link_.id), link.getId());
            }
        };
    }

    private Specification<MetadataLink> metadatalinksTargetting(AbstractMetadata md) {
        return new Specification<MetadataLink>() {
            @Override
            public Predicate toPredicate(Root<MetadataLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(MetadataLink_.metadataId), md.getId());
            }
        };
    }

    private boolean isReferencingAnUnknownMetadata(MetadataLink metadatalink) {
        return isNull(metadataRepository.findOne(metadatalink.getMetadataId()));
    }


}
