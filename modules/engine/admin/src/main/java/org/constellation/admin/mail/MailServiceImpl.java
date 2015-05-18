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

package org.constellation.admin.mail;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.constellation.engine.register.jooq.tables.pojos.Property;
import org.constellation.engine.register.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: laurent
 * Date: 29/04/15
 * Time: 16:00
 * Geomatys
 */
@Component
public class MailServiceImpl implements MailService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private PropertyRepository propertyRepository;

    private final static List<String> SMTP_PROPS = Arrays.asList("email.smtp.from", "email.smtp.host", "email.smtp.port", "email.smtp.username", "email.smtp.password");
    private static String FROM;
    private static String HOST;
    private static Integer PORT;
    private static String USERNAME;
    private static String PASSWORD;

    @PostConstruct
    public void init() {
        ImmutableMap<String, ? extends Property> properties = Maps.uniqueIndex(propertyRepository.findIn(SMTP_PROPS), new Function<Property, String>(){
            @Override
            public String apply(Property property) {
                return property.getName();
            }
        });
        System.out.println("lol");
        FROM = properties.get("email.smtp.from").getValue();
        HOST = properties.get("email.smtp.host").getValue();
        PORT = Integer.valueOf(properties.get("email.smtp.port").getValue());
        USERNAME = properties.get("email.smtp.username").getValue();
        PASSWORD = properties.get("email.smtp.password").getValue();
    }

    @Override
    public void send(String subject, String htmlMsg, List<String> recipients) throws EmailException {
        send(subject, htmlMsg, recipients, null);
    }

    /**
     * send HTML message
     * @param subject
     * @param htmlMsg
     * @param recipients
     * @param attachment : file attachment, may be null
     * @throws org.apache.commons.mail.EmailException
     */
    @Override
    public void send(String subject, String htmlMsg, List<String> recipients, File attachment) throws EmailException {

        //For debugging purposes, we can disable mail sender by passing system property.
        final String mailEnabled = System.getProperty("cstl.mail.enabled", "true");
        if(!Boolean.valueOf(mailEnabled)) {
            LOGGER.info("Mail service is disabled, run the server with option -Dcstl.mail.enabled=true to enable it.");
            return;
        }

        // Build recipients internet addresses.
        List<InternetAddress> addresses = Lists.transform(recipients, new Function<String, InternetAddress>() {
            @Override
            public InternetAddress apply(String address) {
                try {
                    return new InternetAddress(address);
                } catch (AddressException ex) {
                    LOGGER.warn("Recipient ignored due to previous error(s)" + address, ex);
                    return null;
                }
            }
        });
        if (addresses.isEmpty()) {
            return;
        }

        // Send HTML email.
        HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setFrom(FROM);
        htmlEmail.setHostName(HOST);
        htmlEmail.setSmtpPort(PORT);
        htmlEmail.setAuthentication(USERNAME, PASSWORD);
        htmlEmail.setTo(addresses);
        htmlEmail.setSubject(subject);
        htmlEmail.setHtmlMsg(htmlMsg);
        htmlEmail.setCharset("UTF-8");

        //append attachment
        if(attachment != null){
            try {
                EmailAttachment emailAttachment = new EmailAttachment();
                emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                emailAttachment.setDescription(attachment.getName());
                emailAttachment.setName(attachment.getName());
                emailAttachment.setURL(attachment.toURI().toURL());
                htmlEmail.attach(emailAttachment);
            } catch (MalformedURLException e) {
                LOGGER.error("Unable to get attachment", e);
            }
        }

        htmlEmail.send();
    }
}
