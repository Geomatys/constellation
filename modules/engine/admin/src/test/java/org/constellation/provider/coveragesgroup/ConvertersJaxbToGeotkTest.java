/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.provider.coveragesgroup;

import javax.annotation.PostConstruct;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IStyleBusiness;
import org.constellation.provider.coveragesgroup.util.ConvertersJaxbToGeotk;
import org.constellation.provider.coveragesgroup.xml.DataReference;
import org.constellation.provider.coveragesgroup.xml.MapLayer;
import org.constellation.provider.coveragesgroup.xml.StyleReference;
import org.geotoolkit.map.MapItem;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 * @author Cédric Briançon (Geomatys)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard" })
@Ignore //ignore this test due to compilation lock in jenkins, this test should be rewrited with mock objects see issue CSTL-1163
public class ConvertersJaxbToGeotkTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Autowired
    private IStyleBusiness styleBusiness;
    
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
    }

    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testConvertsMapLayer() {
        SpringHelper.injectDependencies(this);
        final StyleReference styleRef = new StyleReference("myStyle");
        final DataReference dataRef =
                new DataReference("${providerLayerType|myProvider|myLayer}");
        final MapLayer mapLayer = new MapLayer(dataRef, styleRef);

        final MapItem item = ConvertersJaxbToGeotk.convertsMapLayer(mapLayer, null, null, styleBusiness);
        assertNotNull(item);
    }

    @Test
    public void testConvertsMapLayerWithDataReferenceStyle() {
        SpringHelper.injectDependencies(this);
        final StyleReference styleRef =
                new StyleReference("${providerLayerType|sld|myStyle}");
        final DataReference dataRef =
                new DataReference("${providerLayerType|myProvider|myLayer}");
        final MapLayer mapLayer = new MapLayer(dataRef, styleRef);

        final MapItem item = ConvertersJaxbToGeotk.convertsMapLayer(mapLayer, null, null, styleBusiness);
        assertNotNull(item);
    }
}
