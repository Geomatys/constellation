package org.constellation.admin.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class AccountResource {

	private final Logger log = LoggerFactory.getLogger(AccountResource.class);

	/**
	 * GET /rest/authenticate -> check if the user is authenticated.
	 */
	@RequestMapping(value = "/rest/authenticate", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public String isAuthenticated() {
		log.debug("REST request to check if the current user is authenticated");
		return "OK";
	}

}
