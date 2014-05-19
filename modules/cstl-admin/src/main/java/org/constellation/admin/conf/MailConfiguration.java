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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.inject.Inject;
import java.util.Properties;

@Configuration
public class MailConfiguration {

    private final Logger log = LoggerFactory.getLogger(MailConfiguration.class);

    @Inject
    private Environment env;

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        log.debug("Configuring mail server");
        String host = env.getProperty("mail.host");
        int port = 0;
        if (env.getProperty("mail.port")!= null && !env.getProperty("mail.port").equals("")) {
            port = env.getProperty("mail.port", Integer.class);
        }
        String user = env.getProperty("mail.user");
        String password = env.getProperty("mail.password");
        String tls = env.getProperty("mail.tls");

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        if (host != null && !host.equals("")) {
            log.warn("Warning! Your SMTP server is not configured. We will try to use one on localhost.");
            log.debug("Did you configure your SMTP settings in your pom.xml ?");
            sender.setHost("127.0.0.1");
        } else {
            sender.setHost(host);
        }
        sender.setPort(port);
        sender.setUsername(user);
        sender.setPassword(password);
        if (tls != null && !tls.equals("")) {
            Properties sendProperties = new Properties();
            sendProperties.setProperty("mail.smtp.starttls.enable", "true");
            sender.setJavaMailProperties(sendProperties);
        }
        return sender;
    }
}
