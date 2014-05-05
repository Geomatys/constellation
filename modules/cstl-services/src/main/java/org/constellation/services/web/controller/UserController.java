package org.constellation.services.web.controller;

import java.util.List;

import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/user", produces="application/json")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    List<? extends User> all() {
        return userRepository.all();
    }
    
    @RequestMapping(value="/{login}", method = RequestMethod.GET)
    public @ResponseBody
    User oneWithRole(@PathVariable("login") String login) {
        return userRepository.findOneWithRolesAndDomains(login);
    }

    // @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    List<? extends User> post(@RequestBody User user) {
        userRepository.insert(user);
        return all();
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public @ResponseBody
    List<? extends User> put(@RequestBody User user) {
        userRepository.insert(user);
        return all();
    }

    // @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public @ResponseBody
    List<? extends User> delete(@PathVariable("id") String id) {
        userRepository.delete(id);
        return all();
    }

}
