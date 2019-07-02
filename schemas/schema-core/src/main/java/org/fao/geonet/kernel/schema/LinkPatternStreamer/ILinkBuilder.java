package org.fao.geonet.kernel.schema.LinkPatternStreamer;

public interface ILinkBuilder<L, M> {

    L found(String url);

    void persist(L link, M ref);
}
