package org.fao.geonet.domain;

import static org.fao.geonet.domain.SchematronRequirement.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jesse on 2/6/14.
 */
public class SchematronRequirementTest {
    @Test
    public void testHighestRequirement() throws Exception {
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(DISABLED));
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(REPORT));
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(REQUIRED));

        Assert.assertEquals(REQUIRED, REPORT.highestRequirement(REQUIRED));
        Assert.assertEquals(REPORT, REPORT.highestRequirement(DISABLED));
        Assert.assertEquals(REPORT, REPORT.highestRequirement(REPORT));

        Assert.assertEquals(DISABLED, DISABLED.highestRequirement(DISABLED));
        Assert.assertEquals(REPORT, DISABLED.highestRequirement(REPORT));
        Assert.assertEquals(REQUIRED, DISABLED.highestRequirement(REQUIRED));
    }
}
