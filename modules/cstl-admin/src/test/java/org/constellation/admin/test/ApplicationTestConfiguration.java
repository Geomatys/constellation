package org.constellation.admin.test;

import org.constellation.admin.conf.*;
import org.constellation.gui.admin.conf.CstlConfig;
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
        AsyncConfiguration.class,
        CacheConfiguration.class,
        DatabaseConfiguration.class,
        MailConfiguration.class})
public class ApplicationTestConfiguration {

    private final Logger log = LoggerFactory.getLogger(ApplicationTestConfiguration.class);

    @Inject
    private Environment env;

    @Bean
    public CstlConfig getCstlConfig() {
        CstlConfig conf = new CstlConfig();
        conf.setUrl(env.getProperty("cstl.url", "http://localhost:8180/constellation/"));
        conf.setLogin(env.getProperty("cstl.login", "admin"));
        conf.setPassword(env.getProperty("cstl.password", "admin"));
        return conf;
    }
    
    /**
     * Initializes cstl-admin test context.
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
