package org.fao.geonet.kernel.schema.LinkPatternStreamer;

public interface ILinkBuilder<L> {

    L build();

    void setUrl(L link, String url);
}
