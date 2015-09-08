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
package org.constellation.coverage;

import java.net.MalformedURLException;
import junit.framework.Assert;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.SpringHelper;
import org.constellation.test.utils.SpringTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
public class PyramidCoverageHelperTestCase  implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Test
    public void testSimple() throws MalformedURLException, DataStoreException {
        SpringHelper.setApplicationContext(applicationContext);
        PyramidCoverageHelper helper = PyramidCoverageHelper.builder("name")
                .inputFormat("PNG").fromImage("path/to/a/geo.tiff")
                .toMemoryStore().build();
        Assert.assertNotNull(helper);
        PyramidCoverageHelper.builder("zozo").fromImage("");

    }

}
