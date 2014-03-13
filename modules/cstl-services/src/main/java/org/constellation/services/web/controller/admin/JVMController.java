package org.constellation.services.web.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/admin/jvm")
public class JVMController {

	@RequestMapping("/rungc")
	@ResponseStatus(value=HttpStatus.NO_CONTENT)
	public void runGC() {
		System.gc();
	}
	
}
