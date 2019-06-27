package org.fao.geonet.kernel.schema.LinkPatternStreamer;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RawLinkPatternStreamer <L, M> {
    private static final Pattern SEARCH_URL_IN_STRING_REGEX = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?", Pattern.CASE_INSENSITIVE);

    private Pattern pattern;
    private ILinkBuilder<L, M> linkBuilder;
    private List<Namespace> namespaces;
    private String rawTextXPath;

    public RawLinkPatternStreamer(ILinkBuilder linkBuilder)
    {
        this.pattern = SEARCH_URL_IN_STRING_REGEX;
        this.linkBuilder = linkBuilder;
    }

    public void setRawTextXPath(String rawTextXPath) {
        this.rawTextXPath = rawTextXPath;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public void processAllRawText(Element metadata, M ref) throws JDOMException {
        List<Element> encounteredLinks = (List<Element>) Xml.selectNodes(metadata, rawTextXPath, namespaces);

        encounteredLinks.stream().forEach(rawTextElem -> processOneRawText(rawTextElem, ref));

    }

    private void processOneRawText(Element rawTextElem, M ref) {
        for (Matcher m = this.pattern.matcher(rawTextElem.getValue()); m.find(); ) {
            L link = linkBuilder.build();
            linkBuilder.setUrl(link, m.toMatchResult().group());
            linkBuilder.persist(link, ref);
        }
    }
}

