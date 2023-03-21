package org.fao.geonet.kernel;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public abstract class AbstractIntegrationTestWithMockedSingletons extends AbstractCoreIntegrationTest {

    private static SpringLocalServiceInvoker mockInvoker;

    public SpringLocalServiceInvoker resetAndGetMockInvoker() {
        synchronized (AbstractIntegrationTestWithMockedSingletons.class) {
            if (mockInvoker == null) {
                mockInvoker = mock(SpringLocalServiceInvoker.class);
            }
            try {
                _applicationContext.getBean(SpringLocalServiceInvoker.class);
            }
            catch (NoSuchBeanDefinitionException e) {
                _applicationContext.getBeanFactory().registerSingleton(SpringLocalServiceInvoker.class.getCanonicalName(), mockInvoker);
            }
        }
        reset(mockInvoker);
        return mockInvoker;
    }
}
