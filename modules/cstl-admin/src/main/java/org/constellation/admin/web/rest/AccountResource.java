package org.constellation.admin.web.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.constellation.admin.repository.UserRepository;
import org.constellation.admin.security.SecurityUtils;
import org.constellation.admin.service.UserService;
import org.constellation.engine.register.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    /**
     * GET  /rest/authenticate -> check if the user is authenticated.
     */
    @RequestMapping(value = "/rest/authenticate",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public String isAuthenticated() {
        log.debug("REST request to check if the current user is authenticated");
        return "OK";
    }

    /**
     * GET  /rest/account -> get the current user.
     */
    @RequestMapping(value = "/rest/account",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public UserDTO getAccount(HttpServletResponse response) {
        UserDTO user = userRepository.findOne(SecurityUtils.getCurrentLogin());
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return user;
    }

    /**
     * POST  /rest/account -> update the current user information.
     */
    @RequestMapping(value = "/rest/account",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public void saveAccount(@RequestBody UserDTO user) throws IOException {
        userService.updateUserInformation(user);
    }

    /**
     * POST  /rest/change_password -> changes the current user's password
     */
    @RequestMapping(value = "/rest/account/change_password",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public void changePassword(@RequestBody String password, HttpServletResponse response) throws IOException {
        if (password == null || password.equals("")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Password should not be empty");
        } else {
            userService.changePassword(password);
        }
    }

  }
