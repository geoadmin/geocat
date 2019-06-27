package org.fao.geonet.kernel.schema;

import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;

public interface LinkAwareSchemaPlugin {

    <L> RawLinkPatternStreamer<L> create(ILinkBuilder<L> linkbuilder);
}
