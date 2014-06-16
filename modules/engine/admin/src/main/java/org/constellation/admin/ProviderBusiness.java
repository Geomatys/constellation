package org.constellation.admin;

import javax.inject.Inject;

import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.stereotype.Component;

@Component
public class ProviderBusiness {
	
	@Inject
	ProviderRepository providerRepository;
	
	

}
