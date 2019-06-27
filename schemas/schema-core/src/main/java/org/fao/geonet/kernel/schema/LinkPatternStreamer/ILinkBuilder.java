package org.fao.geonet.kernel.schema.LinkPatternStreamer;

public interface ILinkBuilder<L, M> {

    L build();

    void setUrl(L link, String url);

    void persist(L link, M ref);
}
