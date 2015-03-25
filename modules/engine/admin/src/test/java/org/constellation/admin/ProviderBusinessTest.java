/*
 * Geotoolkit.org - An Open Source Java GIS Toolkit
 * http://www.geotoolkit.org
 *
 * (C) 2014, Geomatys
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package org.constellation.admin;

import java.net.MalformedURLException;
import java.net.URL;

import org.constellation.business.IProviderBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.geotoolkit.coverage.CoverageStoreFactory;
import org.geotoolkit.coverage.filestore.FileCoverageStoreFactory;
import org.geotoolkit.parameter.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Date: 18/09/14
 * Time: 10:28
 *
 * @author Alexis Manin (Geomatys)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles({"standard", "derby"})
public class ProviderBusinessTest implements ApplicationContextAware {

    @Autowired
    private IProviderBusiness pBusiness;

    @Test
    public void createFromDataStoreFactory() throws ConfigurationException, MalformedURLException {
        final String id = "teeeeeeeeeeeest";
        final CoverageStoreFactory cvgFactory = new FileCoverageStoreFactory();
        final ParameterValueGroup config = cvgFactory.getParametersDescriptor().createValue();
        final URL dataPath = new URL("file:/path/to/anything");
        config.parameter(FileCoverageStoreFactory.PATH.getName().getCode()).setValue(dataPath);
        Provider p = pBusiness.create(1, id, cvgFactory, config);
        // TODO : Re-activate when auto-generated equals will be done.
        //Assert.assertEquals("Created provider must be equal to read one.", p, pBusiness.getProvider(id));

        final DataProvider provider = DataProviders.getInstance().getProvider(id);
        final ParameterValueGroup readConf =
                provider.getSource().groups("choice").get(0).groups(config.getDescriptor().getName().getCode()).get(0);

        // Disabled test because it uses deprecated geotk parameter implementation. We just check registered URL instead.
//        Assert.assertTrue("Written and read configuration must be equal." +
//                "\nExpected : \n"+config +
//                "\nFound : \n"+readConf, CRS.equalsApproximatively(config, readConf));
        Assert.assertEquals("Registered URL must be the same as read one.",
                dataPath, Parameters.value(FileCoverageStoreFactory.PATH, readConf));
    }

    @Test
    public void createFromProviderFactory() throws ConfigurationException, MalformedURLException {
        // Create data store configuration
        final String id = "teeeeeeeeeeeest is back";
        final CoverageStoreFactory cvgFactory = new FileCoverageStoreFactory();
        final ParameterValueGroup config = cvgFactory.getParametersDescriptor().createValue();
        config.parameter(FileCoverageStoreFactory.PATH.getName().getCode()).setValue(new URL("file:/path/to/anything"));

        // Embed data store configuration into provider one.
        final DataProviderFactory factory = DataProviders.getInstance().getFactory(
                ProviderBusiness.SPI_NAMES.COVERAGE_SPI_NAME.name);
        final ParameterValueGroup providerConf = factory.getProviderDescriptor().createValue();
        providerConf.parameter("id").setValue(id);
        providerConf.parameter("providerType").setValue(ProviderBusiness.SPI_NAMES.COVERAGE_SPI_NAME.name);
        final ParameterValueGroup choice =
                providerConf.groups("choice").get(0).addGroup(config.getDescriptor().getName().getCode());
        Parameters.copy(config, choice);

        Provider p = pBusiness.create(1, id, factory.getName(), providerConf);
        Provider read = pBusiness.getProvider(id);
        // TODO : Re-activate when auto-generated equals will be done.
        //Assert.assertEquals("Created provider must be equal to read one.", p, read);

        final DataProvider provider = DataProviders.getInstance().getProvider(id);
        Assert.assertEquals("Written and read configuration must be equal.", providerConf, provider.getSource());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringHelper.setApplicationContext(applicationContext);
    }
}
