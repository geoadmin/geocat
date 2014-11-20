package org.fao.geonet;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Jesse on 10/17/2014.
 */
public class GeonetMockServletContext extends MockServletContext {
    private String resourcePath = "";

    public GeonetMockServletContext() {
        super(new FileSystemResourceLoader());
    }

    public void setTestClass(Class testClass) {
        this.resourcePath = AbstractCoreIntegrationTest.getWebappDir(testClass);
        if (this.resourcePath.endsWith("/") || this.resourcePath.endsWith("\\")) {
            this.resourcePath = this.resourcePath.substring(0, this.resourcePath.length() - 1);
        }
    }

    @Override
    protected String getResourceLocation(String path) {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource != null) {
            try {
                return Paths.get(resource.toURI()).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return this.resourcePath + super.getResourceLocation(path);
    }
}
