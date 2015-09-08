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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.mail.EmailException;
import org.constellation.admin.mail.MailService;
import org.constellation.auth.transfer.TokenTransfer;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.repository.UserRepository;
import org.constellation.services.component.TokenService;
import org.geotoolkit.util.StringUtilities;
import org.slf4j.LoggerFactory;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Optional;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.ResourceBundle;

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

    static class ForgotPassword {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    static class ResetPassword{
        private String password;
        private String uuid;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private UserDetailsService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

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
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
            LoggerFactory.getLogger(AuthController.class).warn(ex.getMessage(), ex);
        }

        /*
         * Reload user as password of authentication principal will be null
         * after authorization and password is needed for token generation
         */
        UserDetails userDetails = this.userService.loadUserByUsername(login.getUsername());

        String createToken = tokenService.createToken(userDetails.getUsername());

        Optional<CstlUser> findOne = userRepository.findOne(userDetails.getUsername());

        int id = findOne.get().getId();

        return new ResponseEntity<TokenTransfer>(new TokenTransfer(createToken, id), HttpStatus.OK);
    }

    @RequestMapping(value="/forgotPassword", method=RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity forgotPassword(HttpServletRequest request, @RequestBody final ForgotPassword forgotPassword) throws EmailException {
        final String email = forgotPassword.getEmail();
        String uuid = DigestUtils.sha256Hex(email + System.currentTimeMillis());
        Optional<CstlUser> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            CstlUser user = userOptional.get();
            user.setForgotPasswordUuid(uuid);
            userRepository.update(user);

            String baseUrl = "http://" + request.getHeader("host") + request.getContextPath();
            String resetPasswordUrl = baseUrl + "/reset-password.html?uuid=" + uuid;

            ResourceBundle bundle = ResourceBundle.getBundle("org/constellation/admin/mail/mail", LocaleUtils.toLocale(user.getLocale()));
            Object[] args = {user.getFirstname(), user.getLastname(), resetPasswordUrl};

            mailService.send(bundle.getString("account.password.reset.subject"),
                    MessageFormat.format(bundle.getString("account.password.reset.body"), args),
                    Collections.singletonList(email));
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/resetPassword", method=RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity resetPassword(@RequestBody final ResetPassword resetPassword){
        String newPassword = resetPassword.getPassword(), uuid = resetPassword.getUuid();

        if(newPassword != null && uuid != null && !newPassword.isEmpty() && !uuid.isEmpty()){
            Optional<CstlUser> userOptional = userRepository.findByForgotPasswordUuid(uuid);
            if (userOptional.isPresent()){
                CstlUser cstlUser = userOptional.get();
                cstlUser.setPassword(StringUtilities.MD5encode(newPassword));
                cstlUser.setForgotPasswordUuid(null);
                userRepository.update(cstlUser);
                return new ResponseEntity(HttpStatus.OK);
            }
        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
