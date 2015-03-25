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
package org.constellation.services.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

import org.constellation.auth.transfer.TokenTransfer;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.services.component.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Optional;

@Controller
@Profile("standard")
public class AuthController {

    static class Login {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        private String password;
    }

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private UserDetailsService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authManager;

    /**
     * Authenticates a user and creates an authentication token.
     * 
     * @param username
     *            The name of the user.
     * @param password
     *            The password of the user.
     * @return A transfer containing the authentication token.
     */
    @RequestMapping(value="/login", method=RequestMethod.POST)
    public ResponseEntity<TokenTransfer> login(HttpServletRequest request, @RequestBody Login login) {

        if (authManager == null) {
            return new ResponseEntity<TokenTransfer>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getUsername(),
                login.getPassword());

        try {
            Authentication authentication = this.authManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {

            return new ResponseEntity<TokenTransfer>(HttpStatus.FORBIDDEN);
        }

        /*
         * Reload user as password of authentication principal will be null
         * after authorization and password is needed for token generation
         */
        UserDetails userDetails = this.userService.loadUserByUsername(login.getUsername());

        String createToken = tokenService.createToken(userDetails.getUsername());

        Optional<CstlUser> findOne = userRepository.findOne(userDetails.getUsername());

        int id = findOne.get().getId();
        Domain defaultDomain = domainRepository.findDefaultByUserId(id);

        if (defaultDomain == null) {
            // No domain associated.
            return new ResponseEntity<TokenTransfer>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<TokenTransfer>(new TokenTransfer(createToken, id, defaultDomain.getId()), HttpStatus.OK);
    }

   

   

    public static UserDetails extractUserDetail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof String && ((String) principal).equals("anonymousUser")) {
            throw new WebApplicationException(401);
        }
        UserDetails userDetails = (UserDetails) principal;
        return userDetails;
    }
}
