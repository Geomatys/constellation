package org.constellation.services.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;

import org.constellation.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class CstlAdminTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CstlAdminTokenController.class);

    @Autowired
    private TokenService tokenService;
    
    @RequestMapping(value="/auth/extendToken", method=RequestMethod.GET)
    public @ResponseBody String extendToken() {
        UserDetails userDetails = AuthController.extractUserDetail();

        return tokenService.createToken(userDetails.getUsername());

    }
    
    
    @RequestMapping(value="/auth/logout", method=RequestMethod.DELETE)
    public void logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            LOGGER.warn("Session ?");
            session.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    
}
