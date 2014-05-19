/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
