package org.constellation.services.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;

import org.constellation.token.TokenExtender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class CstlAdminTokenController {

    @Autowired
    private TokenExtender tokenExtender;


    @RequestMapping(value="/auth/extendToken", method=RequestMethod.GET)
    public @ResponseBody String extendToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        UserDetails userDetails = AuthController.extractUserDetail();

        return tokenExtender.extend(userDetails.getUsername(), httpServletRequest, httpServletResponse);

    }


    @RequestMapping(value="/auth/logout", method=RequestMethod.DELETE)
    public void logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            session.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}
