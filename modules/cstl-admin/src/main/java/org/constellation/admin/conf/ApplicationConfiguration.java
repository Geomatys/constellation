package org.constellation.admin.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;

@Configuration
@PropertySource({"classpath:/META-INF/cstl-admin/cstl-admin.properties"})
@ComponentScan(basePackages = {
        "org.constellation.admin.service",
        "org.constellation.admin.security"})
@Import(value = {
        SecurityConfiguration.class,
        AsyncConfiguration.class,
        CacheConfiguration.class,
        DatabaseConfiguration.class,
        MailConfiguration.class})
public class ApplicationConfiguration {

    private final Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Inject
    private Environment env;

    /**
     * Initializes cstl-admin.
     * <p/>
     * Spring profiles can be configured with a system property -Dspring.profiles.active=your-active-profile
     * <p/>
     */
    @PostConstruct
    public void initApplication() throws IOException {
        log.debug("Looking for Spring profiles...");
        if (env.getActiveProfiles().length == 0) {
            log.debug("No Spring profile configured, running with default configuration");
        } else {
            for (String profile : env.getActiveProfiles()) {
                log.debug("Detected Spring profile : {}", profile);
            }
        }
    }
}
