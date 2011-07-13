/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.metadata.utils;

import com.sun.mail.smtp.SMTPTransport;

import java.io.File;
import java.io.FileInputStream;
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
import javax.crypto.KeyGenerator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.util.logging.Logging;

/**
 * Utility class to send mails
 *
 * @author Johann Sorel (Geomatys)
 */
public final class MailSendingUtilities {

    private static final Logger LOGGER = Logging.getLogger(MailSendingUtilities.class.getName());
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
        final Properties configProps = new Properties();

        try {
            final File f = new File(ConfigDirectory.getConfigDirectory(), "mailing.properties");
            configProps.load(new FileInputStream(f));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        FROM        = configProps.getProperty("from");
        MAILHOST    = configProps.getProperty("mailhost");
        MAILER      = configProps.getProperty("mailer");
        USER        = configProps.getProperty("user");
        PASSWORD    = configProps.getProperty("password");
        AUTH        = configProps.getProperty("auth");
        PROTOCOLE   = configProps.getProperty("protocole");
        PORT        = Integer.valueOf(configProps.getProperty("port"));


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

        } catch (NoSuchProviderException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
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

