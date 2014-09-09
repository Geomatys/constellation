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

import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class ServiceBusinessTest {

    @Autowired
    private IServiceBusiness serviceBusiness;

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
        final Details details = new Details("name", "identifier", Arrays.asList("keyword1", "keyword2"), "description", Arrays.asList("version1"), new Contact(), new AccessConstraint(), true, "FR");
        Object conf = serviceBusiness.create("wms", "test", new LayerContext(), details,null);
        Assert.assertTrue(serviceBusiness.getServiceIdentifiers("wms").contains("test"));
        
    }

}
