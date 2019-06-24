package org.fao.geonet.kernel.schema.LinkPatternStreamer;

public interface IMetadataLink<L extends ILink, M> {
    IMetadataLink<L, M> setId(M metadata, L link);
}
