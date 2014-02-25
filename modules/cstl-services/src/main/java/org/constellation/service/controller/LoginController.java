package org.constellation.service.controller;

import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "{login}", method = RequestMethod.GET)
    public @ResponseBody
    UserDTO login(@PathVariable("login") String login) {
        User findOne = userRepository.findOne(login);
        if (findOne == null)
            return null;
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin(login);
        userDTO.setLastname(findOne.getLastname());
        userDTO.setFirstname(findOne.getFirstname());
        userDTO.setEmail(findOne.getEmail());
        userDTO.setPassword(findOne.getPassword());
        return userDTO;
    }

}
