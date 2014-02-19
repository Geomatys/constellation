package org.constellation.admin.service;

import javax.inject.Inject;

import org.constellation.admin.repository.UserRepository;
import org.constellation.admin.test.ApplicationTestConfiguration;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
        @ContextConfiguration(
                name = "root",
                classes = ApplicationTestConfiguration.class)
})
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class UserServiceTest {

    private final Logger log = LoggerFactory.getLogger(UserServiceTest.class);


    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;


}
