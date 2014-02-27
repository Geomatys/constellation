package org.constellation.service.controller;

import java.util.List;

import javax.inject.Inject;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/admin/provider",produces="application/json" )

public class ProviderController {

	@Inject
	private ProviderRepository providerRepository;
	
	@RequestMapping
	public @ResponseBody List<? extends Provider> all(){
		return providerRepository.findAll();
	}
	
}
