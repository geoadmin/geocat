package org.fao.geonet.geocat.cgp;

import com.google.common.base.Function;
import jeeves.server.context.ServiceContext;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.Constants;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * SOAP request wrapper.
 * Note: could not use Jeeves Xmlrequest since we need to set SOAP header as well.
 */
public class SOAPRequest {
    public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://schemas.xmlsoap.org/soap/envelope/");
    private final ServiceContext context;
    private UsernamePasswordCredentials proxyCredentials;

    public SOAPRequest(ServiceContext context, String urlStr) throws MalformedURLException {
        this.context = context;
        url = new URL(urlStr);
    }

    /**
     * Sends a request and obtains an xml response.
     */
    public Document execute() throws SOAPFaultEx, JDOMException, IOException, BadXmlResponseEx, BadSoapResponseEx, URISyntaxException {
        HttpPost httpMethod = createHttpMethod();


        // byte[] data = null;
        Document responseDoc = null;
        try {
            final ClientHttpResponse response = context.getBean(GeonetHttpRequestFactory.class).execute(httpMethod,
                    new Function<HttpClientBuilder, Void>() {
                        @Override
                        public Void apply(HttpClientBuilder key) {
                            if (useProxy) {
                                HttpHost proxy = new HttpHost(proxyHost, proxyPort, url.getProtocol());
                                key.setProxy(proxy);
                            }
                            return null;
                        }
                    });

            // Better not with large queries
            // data = httpMethod.getResponseBody();
            //String s = new String(data);
            SAXBuilder builder = new SAXBuilder();

            // KLUDGE 9.apr.09 Just: ws.geoportal.ch appears to have an encoding that
            // the standard SAX parser fails upon. For now we convert
            // the byte input to a char stream.
            if (url.getHost().indexOf("geoportal.ch") != -1) {
                responseDoc = builder.build(new InputStreamReader(response.getBody(), Constants.ENCODING));
            } else {
                responseDoc = builder.build(response.getBody());
            }
        } catch (JDOMException e) {
            throw new BadXmlResponseEx("Parse error: " + e.getMessage());
        } finally {
            httpMethod.releaseConnection();
        }

        // Check for SOAP errors and Faults
        Element rootElm = responseDoc.getRootElement();
        if (rootElm == null) {
            throw new BadSoapResponseEx(rootElm);
        }

        Element bodyElm = rootElm.getChild("Body", NAMESPACE_ENV);
        if (bodyElm == null) {
            throw new BadSoapResponseEx(rootElm);
        }

        // Valid Body element: check if it contains a Fault elm
        Element faultElm = bodyElm.getChild("Fault", NAMESPACE_ENV);
        if (faultElm != null) {
            throw new SOAPFaultEx(faultElm);
        }

        return responseDoc;
    }

    public String getHost() {
        return this.url.getHost();
    }

    public void setBodyContent(Element bodyContentElm) throws UnsupportedEncodingException {
        this.bodyContentElm = bodyContentElm;
    }

    public void setHeaderContent(Element headerContentElm) throws UnsupportedEncodingException {
        this.headerContentElm = headerContentElm;
    }

    //---------------------------------------------------------------------------

    private HttpPost createHttpMethod() throws UnsupportedEncodingException, URISyntaxException {
        HttpPost postMethod = new HttpPost();
        String postData = Xml.getString(getSOAPDocument());

        postMethod.setEntity(new StringEntity(postData, ContentType.create("application/soap+xml", "UTF-8")));
        postMethod.setURI(url.toURI());

        return postMethod;
    }

    public Document getSOAPDocument() {
        Element envelopeElm = new Element("Envelope", NAMESPACE_ENV);

        if (headerContentElm != null) {
            Element headerElm = new Element("Header", NAMESPACE_ENV);
            headerElm.addContent(headerContentElm);
            envelopeElm.addContent(headerElm);
        }

        if (bodyContentElm != null) {
            Element bodyElm = new Element("Body", NAMESPACE_ENV);
            bodyElm.addContent(bodyContentElm);
            envelopeElm.addContent(bodyElm);
        }

        return new Document(envelopeElm);
    }

    //---------------------------------------------------------------------------

    public void setUseProxy(boolean yesno) {
        useProxy = yesno;
    }

    //---------------------------------------------------------------------------

    public void setProxyHost(String host) {
        proxyHost = host;
    }

    //---------------------------------------------------------------------------

    public void setProxyPort(int port) {
        proxyPort = port;
    }

    //---------------------------------------------------------------------------

    public void setProxyCredentials(String username, String password) {
        if (username == null || username.trim().length() == 0) {
            return;
        }


        this.proxyCredentials = new UsernamePasswordCredentials(username, password);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---------------------------------------------------------------------------
    private URL url;
    private Element bodyContentElm;
    private Element headerContentElm;
    private boolean useProxy;
    private String proxyHost;
    private int proxyPort;
}
