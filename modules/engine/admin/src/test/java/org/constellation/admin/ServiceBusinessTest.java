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
package org.constellation.admin;

import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.dto.Service;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class ServiceBusinessTest {

    @Autowired
    private ServiceBusiness serviceBusiness;

    @Test
    public void createService() throws ConfigurationException {
       /* ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setId(0);
        serviceDTO.setConfig("config");
        serviceDTO.setDate(new Date());
        serviceDTO.setIdentifier();
        serviceDTO.setDescription("description test");
        serviceDTO.setOwner("admin");
        serviceDTO.setStatus("STARTED");
        serviceDTO.setTitle("title test");
        serviceDTO.setType();*/
        Object conf = serviceBusiness.create("WMS", "test", new LayerContext(), new Service());
        Assert.assertTrue(serviceBusiness.getServiceIdentifiers("WMS").contains("test"));
        
    }

}
