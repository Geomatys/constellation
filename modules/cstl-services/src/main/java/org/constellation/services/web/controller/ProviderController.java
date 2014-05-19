/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.services.web.controller;

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
