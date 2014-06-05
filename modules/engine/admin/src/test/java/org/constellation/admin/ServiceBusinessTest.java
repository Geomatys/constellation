package org.constellation.admin;

import java.util.Date;

import org.constellation.admin.dto.ServiceDTO;
import org.junit.Assert;
import org.junit.BeforeClass;
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
    public void createService() {
		ServiceDTO serviceDTO = new ServiceDTO();
		serviceDTO.setId(0);
		serviceDTO.setConfig("config");
		serviceDTO.setDate(new Date());
		serviceDTO.setIdentifier("test");
		serviceDTO.setDescription("description test");
		serviceDTO.setOwner("admin");
		serviceDTO.setStatus("STARTED");
		serviceDTO.setTitle("title test");
		serviceDTO.setType("WMS");
		serviceDTO = serviceBusiness.create(serviceDTO);
		Assert.assertTrue(serviceDTO.getId()>0);
	}

}
