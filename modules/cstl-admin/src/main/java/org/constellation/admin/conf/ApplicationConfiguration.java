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
package org.constellation.admin.conf;

import org.geotoolkit.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        MailConfiguration.class})
public class ApplicationConfiguration {

    private final Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Inject
    private Environment env;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encode(rawPassword).equals(encodedPassword);
            }
            
            @Override
            public String encode(CharSequence rawPassword) {
                return StringUtilities.MD5encode(rawPassword.toString());
            }
        };
    }
    
    @Bean
    public CstlConfig getCstlConfig() {
        CstlConfig conf = new CstlConfig();
        conf.setUrl(env.getProperty("cstl.url", "http://localhost:8180/constellation/"));
        conf.setLogin(env.getProperty("cstl.login", "admin"));
        conf.setPassword(env.getProperty("cstl.password", "admin"));
        return conf;
    }

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
