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

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.constellation.configuration.AppProperty;
import org.constellation.configuration.Application;
import org.constellation.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: laurent
 * Date: 29/04/15
 * Time: 16:00
 * Geomatys
 */
@Component
@DependsOn({"database-initer"})
public class MailServiceImpl implements MailService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String FROM_KEY = "email.smtp.from";
    private static final String HOST_KEY = "email.smtp.host";
    private static final String PORT_KEY = "email.smtp.port";
    private static final String USERNAME_KEY = "email.smtp.username";
    private static final String PASSWORD_KEY = "email.smtp.password";
    private static final String USE_SSL_KEY = "email.smtp.ssl";
    private final static List<String> SMTP_PROPS = Arrays.asList(FROM_KEY, HOST_KEY, PORT_KEY, USERNAME_KEY, PASSWORD_KEY, USE_SSL_KEY);

    private volatile Map<String, Object> emailConfiguration = null;

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
        final String mailEnabled = Application.getProperty(AppProperty.CSTL_MAIL_ENABLE, "true");
        if(!Boolean.valueOf(mailEnabled)) {
            LOGGER.info("Mail service is disabled, run the server with option -Dcstl.mail.enabled=true to enable it.");
            return;
        }

        // Build recipients internet addresses.
        List<InternetAddress> addresses = new ArrayList<>();
        for (String recipient : recipients) {
            try {
                InternetAddress address = new InternetAddress(recipient);

                //throw exception if invalid
                address.validate();

                addresses.add(address);
            } catch (AddressException ex) {
                LOGGER.warn("Recipient ignored due to previous error(s) " + recipient, ex);
            }
        }

        if (addresses.isEmpty()) {
            return;
        }

        try {
            // Send HTML email.
            HtmlEmail htmlEmail = createHtmlEmail();
            htmlEmail.setTo(addresses);
            htmlEmail.setSubject(subject);
            htmlEmail.setHtmlMsg(htmlMsg);
            htmlEmail.setCharset("UTF-8");

            //append attachment
            if (attachment != null) {
                try {
                    EmailAttachment emailAttachment = new EmailAttachment();
                    emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                    emailAttachment.setDescription(attachment.getName());
                    emailAttachment.setName(attachment.getName());
                    emailAttachment.setURL(attachment.toURI().toURL());
                    htmlEmail.attach(emailAttachment);
                } catch (MalformedURLException e) {
                    throw new EmailException("Unable to get attachment", e);
                }
            }

            htmlEmail.send();
        } catch (ConfigurationException ex) {
            throw new EmailException("Unable to create a new mail from configuration.", ex);
        } catch (EmailException ex) {
            LOGGER.warn("Unable to send email : "+ex.getMessage(), ex);
            throw ex;
        }
    }

    private HtmlEmail createHtmlEmail() throws EmailException, ConfigurationException {

        //load properties from database
        loadEmailConfiguration();

        HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setFrom((String) emailConfiguration.get(FROM_KEY));
        htmlEmail.setHostName((String) emailConfiguration.get(HOST_KEY));
        htmlEmail.setSmtpPort((int) emailConfiguration.get(PORT_KEY));
        htmlEmail.setAuthentication((String) emailConfiguration.get(USERNAME_KEY), (String) emailConfiguration.get(PASSWORD_KEY));
        htmlEmail.setSSL((Boolean) emailConfiguration.get(USE_SSL_KEY));
        return htmlEmail;
    }

    /**
     * Lazy loading configuration.
     *
     * @throws ConfigurationException
     */
    private synchronized void loadEmailConfiguration() throws ConfigurationException {
        if (emailConfiguration == null) {

            emailConfiguration = new HashMap<>();

            emailConfiguration.put(USERNAME_KEY, Application.getProperty(AppProperty.CSTL_MAIL_SMTP_USER));
            emailConfiguration.put(PASSWORD_KEY, Application.getProperty(AppProperty.CSTL_MAIL_SMTP_PASSWD));
            emailConfiguration.put(FROM_KEY, Application.getProperty(AppProperty.CSTL_MAIL_SMTP_FROM));
            emailConfiguration.put(HOST_KEY, Application.getProperty(AppProperty.CSTL_MAIL_SMTP_HOST));
            emailConfiguration.put(PORT_KEY, Integer.valueOf(Application.getProperty(AppProperty.CSTL_MAIL_SMTP_PORT)));
            emailConfiguration.put(USE_SSL_KEY, Boolean.valueOf(Application.getProperty(AppProperty.CSTL_MAIL_SMTP_USE_SSL, "false")));

            //test non missing parameters
            for (String expectedKey : SMTP_PROPS) {
                if (emailConfiguration.get(expectedKey) == null)
                    throw new ConfigurationException("Missing \""+expectedKey+"\" property.");
            }
        }
    }
}
