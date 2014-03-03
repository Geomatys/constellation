package org.constellation.admin.web.controller;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.constellation.gui.admin.conf.CstlConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/conf")
public class ConfigController {
	
	@Inject
	private CstlConfig cstlConfig;

	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody
	Map<Object, Object> get() {
		Properties properties = new Properties();
		properties.put("cstl", cstlConfig.getUrl());
		return properties;
	}

}
