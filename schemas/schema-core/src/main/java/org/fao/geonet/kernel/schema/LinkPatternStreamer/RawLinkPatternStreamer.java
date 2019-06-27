package org.fao.geonet.kernel.schema.LinkPatternStreamer;

import org.jdom.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class RawLinkPatternStreamer <L> {


    private Pattern pattern;
    private ILinkBuilder<L> linkBuilder;

    public static final Pattern SEARCH_URL_IN_STRING_REGEX = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?", Pattern.CASE_INSENSITIVE);


    public RawLinkPatternStreamer(ILinkBuilder linkBuilder)
    {
        this.pattern = SEARCH_URL_IN_STRING_REGEX;
        this.linkBuilder = linkBuilder;
    }

    public Stream<L> results(Element input) {
        Stream.Builder<L> builder = Stream.builder();
        for (Matcher m = this.pattern.matcher(input.getValue()); m.find(); ) {
            L link = linkBuilder.build();
            linkBuilder.setUrl(link, m.toMatchResult().group());
            linkBuilder.persist(link);
            builder.add(link);
        }

        return builder.build();
    }
}

