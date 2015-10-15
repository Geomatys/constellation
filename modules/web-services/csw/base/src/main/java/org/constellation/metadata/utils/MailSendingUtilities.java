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

package org.constellation.metadata.utils;

import com.sun.mail.smtp.SMTPTransport;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.MailingProperties;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import javax.crypto.KeyGenerator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to send mails
 *
 * @author Johann Sorel (Geomatys)
 */
public final class MailSendingUtilities {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.utils");
    private static final String FROM;
    private static final String MAILHOST;
    private static final String MAILER;
    private static final String USER;
    private static final String PASSWORD;
    private static final String AUTH;
    private static final String PROTOCOLE;
    private static final int PORT;
    private static final Session SESSION;

    private MailSendingUtilities() {}

    static{

        MailingProperties mailProp = null;
        try {
            final File f = new File(ConfigDirectory.getConfigDirectory(), "mailingProperties.xml");
            final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            mailProp = (MailingProperties) u.unmarshal(f);
            GenericDatabaseMarshallerPool.getInstance().recycle(u);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if (mailProp == null) {
            mailProp = new MailingProperties();
        }

        FROM        = mailProp.getFrom();
        MAILHOST    = mailProp.getMailhost();
        MAILER      = mailProp.getMailer();
        USER        = mailProp.getUser();
        PASSWORD    = mailProp.getPassword();
        AUTH        = mailProp.getAuth();
        PROTOCOLE   = mailProp.getProtocol();
        PORT        = mailProp.getPort();


        final Properties mailingProps = System.getProperties();
        final String prefix           = "mail.";
        mailingProps.put(prefix + PROTOCOLE + ".auth", AUTH);
        mailingProps.put(prefix + PROTOCOLE + ".port", PORT);
        mailingProps.put(prefix + PROTOCOLE + ".user", USER);
        mailingProps.put(prefix + PROTOCOLE + ".host", MAILHOST);

        final char[] keyStorePassword = "pass".toCharArray();
        final String keyAlias = "secretKeyAlias";

        try {
            //create a keystore. server application doesnt allow mailing without
            //a trusted keystore.
            final KeyStore store = KeyStore.getInstance("JCEKS");
            store.load(null, keyStorePassword);

            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            final KeyGenerator keygen = KeyGenerator.getInstance("DES");
            keygen.init(random);
            final javax.crypto.SecretKey mySecretKey = keygen.generateKey();
            final KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(mySecretKey);
            store.setEntry(keyAlias, skEntry, new KeyStore.PasswordProtection(keyStorePassword));

            mailingProps.put("javax.net.ssl.keyStore",store);
            mailingProps.put("javax.net.ssl.keyStorePassword", keyStorePassword);
            mailingProps.put("javax.net.ssl.trustStorePassword", store);

        } catch (NoSuchProviderException | IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        SESSION = Session.getInstance(mailingProps, null);
    }

    /**
     * Send a mail.
     *
     * @param emails : adress to send the mail
     * @param title : mail title
     * @param content : main mail text content
     * @param files : attached files, any number but limited to the smtp server limit
     * @throws javax.mail.MessagingException
     * @throws java.io.IOException
     * @throws javax.naming.NamingException
     */
    public static void mail(final String[] emails, final String title,
            final String content, final File ... files)
            throws MessagingException, IOException, NamingException {

        if (title == null || content == null)     throw new IllegalArgumentException("Title and content can not be null.");
        if (emails == null || emails.length == 0) throw new IllegalArgumentException("Mails adresses can not be null or empty");

        //make the message content
        final MimeMultipart mp = new MimeMultipart();
        final MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(content);
        mp.addBodyPart(mbp1);

        //attach the files
        for(final File file : files){
            final MimeBodyPart mbp = new MimeBodyPart();
            mbp.attachFile(file);
            mp.addBodyPart(mbp);
        }

        final InternetAddress[] adresses = new InternetAddress[emails.length];
        for(int i=0;i<emails.length;i++){
            adresses[i] = InternetAddress.parse(emails[i], false)[0];
        }

        // construct the message
        final Message msg = new MimeMessage(SESSION);
        msg.setFrom(new InternetAddress(FROM));
        msg.setRecipients(Message.RecipientType.TO, adresses);
        msg.setSubject(title);
        msg.setHeader("X-Mailer", MAILER);
        msg.setSentDate(new Date());
        msg.setContent(mp);

        //send the message
        final SMTPTransport transport = (SMTPTransport) SESSION.getTransport(PROTOCOLE);
        if (Boolean.valueOf(AUTH)) { transport.connect(MAILHOST, PORT, USER, PASSWORD); }
        else {      transport.connect(); }

        transport.sendMessage(msg, msg.getAllRecipients());
    }

}

