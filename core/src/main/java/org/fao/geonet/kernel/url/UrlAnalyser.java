package org.fao.geonet.kernel.url;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.LinkAwareSchemaPlugin;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class UrlAnalyser {

    @Autowired
    protected SchemaManager schemaManager;

    @Autowired
    protected MetadataRepository metadataRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    public void processMetadata(Element element, AbstractMetadata md) throws org.jdom.JDOMException {
        SchemaPlugin schemaPlugin = schemaManager.getSchema(md.getDataInfo().getSchemaId()).getSchemaPlugin();
        if (schemaPlugin instanceof LinkAwareSchemaPlugin) {

            ((LinkAwareSchemaPlugin) schemaPlugin).create(new ILinkBuilder<Link, AbstractMetadata>() {
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
                    MetadataLink metadataLink = new MetadataLink();
                    entityManager.persist(link);
                    metadataLink.setId(metadataRepository.findOne(metadata.getId()), link);
                    entityManager.persist(metadataLink);
                }
            }).processAllRawText(element, md);
        }
    }
}
