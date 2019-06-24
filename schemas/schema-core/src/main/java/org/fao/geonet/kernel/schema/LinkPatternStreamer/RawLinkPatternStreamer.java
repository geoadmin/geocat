package org.fao.geonet.kernel.schema.LinkPatternStreamer;

import org.jdom.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public abstract class RawLinkPatternStreamer <L extends ILink, M, LM extends IMetadataLink<L, M>> {


    private Pattern pattern;
    private M metadata;;

    public static final Pattern SEARCH_URL_IN_STRING_REGEX = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?", Pattern.CASE_INSENSITIVE);


    public RawLinkPatternStreamer() {
        this.pattern = SEARCH_URL_IN_STRING_REGEX;
    }

    public void setMetadata(M metadata) {
        this.metadata = metadata;
    }

    public Stream<LM> results(Element input) {
        Stream.Builder<LM> builder = Stream.builder();
        for (Matcher m = this.pattern.matcher(input.getValue()); m.find(); ) {
            LM lm = buildMetadataLink();
            L link = buildLink();
            link.setUrl(m.toMatchResult().group());
            lm.setId(metadata, link);
            builder.add(lm);
        }

        return builder.build();
    }

    protected abstract L buildLink();

    protected abstract LM buildMetadataLink();

}

