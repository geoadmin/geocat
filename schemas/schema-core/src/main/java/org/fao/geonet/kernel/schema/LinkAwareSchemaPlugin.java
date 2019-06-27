package org.fao.geonet.kernel.schema;

import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;

public interface LinkAwareSchemaPlugin {

    <L, M> RawLinkPatternStreamer<L, M> create(ILinkBuilder<L, M> linkbuilder);
}
