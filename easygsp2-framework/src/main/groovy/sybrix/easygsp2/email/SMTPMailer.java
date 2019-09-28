/*
 * Copyright 2012. the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package sybrix.easygsp2.email;

import sybrix.easygsp2.exceptions.SMTPMailerException;
import sybrix.easygsp2.exceptions.SendEmailException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SMTPMailer {
        private static final Logger logger = Logger.getLogger(SMTPMailer.class.getName());

        private String host;
        private int port;
        private String from;
        private String username;
        private String password;
        private boolean authenticationRequired;
        private boolean secure;
        private boolean isTls;
        private boolean isSsl;

        public String getHost() {
                return host;
        }

        public void setHost(String host) {
                this.host = host;
        }

        public int getPort() {
                return port;
        }

        public void setPort(int port) {
                this.port = port;
        }

        public String getFrom() {
                return from;
        }

        public void setFrom(String from) {
                this.from = from;
        }

        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public boolean isAuthenticationRequired() {
                return authenticationRequired;
        }

        public void setAuthenticationRequired(boolean authenticationRequired) {
                this.authenticationRequired = authenticationRequired;
        }

        public boolean isSecure() {
                return secure;
        }

        public void setSecure(boolean secure) {
                this.secure = secure;
        }

        public boolean isTls() {
                return isTls;
        }

        public void setTls(boolean tls) {
                isTls = tls;
        }

        public boolean isSsl() {
                return isSsl;
        }

        public void setSsl(boolean ssl) {
                isSsl = ssl;
        }

        public void send(final Email email) throws SMTPMailerException {
                Transport transport = null;
                try {
                        Session session = null;
                        Properties props = null;

                        if (isTls) { //tls
                                props = new Properties();
                                props.put("mail.transport.protocol", "smtp");
//                                props.put("mail.host", host);
//                                props.put("mail.smtp.auth", "true");
//                                props.put("mail.smtp.port", port);
//                                props.put("mail.smtp.socketFactory.port", port);
//                                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//                                props.put("mail.smtp.socketFactory.fallback", "false");
//                                props.put("mail.smtp.quitwait", "false");

                                props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
                                props.put("mail.smtp.port", "587"); //TLS Port
                                props.put("mail.smtp.auth", "true"); //enable authentication
                                props.put("mail.smtp.starttls.enable", "true"); //enable

                                session = Session.getInstance(props,
                                        new javax.mail.Authenticator() {
                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                        return new PasswordAuthentication(username, password);
                                                }
                                        });
                        } else if (isSsl) {
                                props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
                                props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
                                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
                                props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
                                props.put("mail.smtp.port", "465"); //SMTP Port

                                session = Session.getInstance(props,
                                        new javax.mail.Authenticator() {
                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                        return new PasswordAuthentication(username, password);
                                                }
                                        });

                        } else {
                                props = System.getProperties();
                                if (authenticationRequired) {
                                        props.put("mail.smtp.auth", "true");

                                        session = Session.getInstance(props,
                                                new javax.mail.Authenticator() {
                                                        protected PasswordAuthentication getPasswordAuthentication() {
                                                                return new PasswordAuthentication(username, password);
                                                        }
                                                });
                                } else {
                                        props.put("mail.smtp.auth", "false");
                                        session = Session.getInstance(props, null);
                                }
                        }

                        MimeMessage message = new MimeMessage(session);

                        message.setFrom(new InternetAddress(email.getFrom()));

                        List<String> recipients = email.getRecipients();
                        List<String> bccRecipients = email.getBcc();
                        List<String> ccRecipients = email.getCc();

                        Address[] to_recipients = parseRecipients(recipients);
                        Address[] cc_recipients = parseRecipients(ccRecipients);
                        Address[] bcc_recipients = parseRecipients(bccRecipients);

                        message.addRecipients(Message.RecipientType.TO, to_recipients);
                        message.addRecipients(Message.RecipientType.CC, cc_recipients);
                        message.addRecipients(Message.RecipientType.BCC, bcc_recipients);

                        message.setSubject(email.getSubject());

                        Multipart multipart = new MimeMultipart("alternative");

                        // create plain text
                        if (email.getBody() != null) {
                                BodyPart plainText = new MimeBodyPart();
                                plainText.setText(email.getBody());
                                multipart.addBodyPart(plainText);
                        }

                        // create html content
                        if (email.getHtmlBody() != null) {
                                BodyPart htmlContent = new MimeBodyPart();
                                htmlContent.setText(email.getHtmlBody());
                                htmlContent.setContent(email.getHtmlBody(), "text/html");
                                multipart.addBodyPart(htmlContent);
                        }

                        for (String key : email.getAttachments().keySet()) {
                                // add attachments
                                BodyPart attachment = new MimeBodyPart();
                                Object data = email.getAttachments().get(key);
                                DataSource source = null;
                                if (data instanceof File) {
                                        source = new FileDataSource((File) data);
                                } else if (data instanceof String) {
                                        source = new FileDataSource(data.toString());
                                } else if (data instanceof ByteArrayDataSource) {
                                        source = (ByteArrayDataSource) data;
                                } else {
                                        continue;
                                }

                                attachment.setDataHandler(new DataHandler(source));
                                attachment.setFileName(key);
                                multipart.addBodyPart(attachment);
                        }

                        message.setContent(multipart);
                        message.saveChanges();

                        transport = session.getTransport("smtp");
                        transport.connect(host, port, username, password);
                        transport.send(message, message.getAllRecipients());


                } catch (SendEmailException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        throw e;
                } catch (Throwable e) {
                        throw new SendEmailException(e);
                } finally {
                        try {
                                if (transport != null)
                                        transport.close();
                        } catch (Exception e) {
                                // do nothing
                        }
                }
        }

//        public static void sendGmail(final Email email) throws SMTPMailerException {
//                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//                try {
//                        Properties props = new Properties();
//                        props.setProperty("mail.transport.protocol", "smtp");
//                        props.setProperty("mail.host", email.getHost());
//                        props.put("mail.smtp.auth", "true");
//                        props.put("mail.smtp.port", "465");
//                        props.put("mail.smtp.socketFactory.port", "465");
//                        props.put("mail.smtp.socketFactory.class",
//                                "javax.net.ssl.SSLSocketFactory");
//                        props.put("mail.smtp.socketFactory.fallback", "false");
//                        props.setProperty("mail.smtp.quitwait", "false");
//
//                        Session session = Session.getInstance(props,
//                                new javax.mail.Authenticator() {
//                                        protected PasswordAuthentication getPasswordAuthentication() {
//                                                return new PasswordAuthentication(email.getUsername(), email.getPassword());
//                                        }
//                                });
//
//                        MimeMessage message = new MimeMessage(session);
//
//                        message.setSender(new InternetAddress(email.getFrom()));
//                        message.setSubject(email.getSubject());
//                        if (email.isHtml()) {
//                                message.setContent(email.getBody(), "text/html; charset=\"us-ascii\"");
//                        } else {
//                                message.setText(email.getBody());
//                        }
////                               if (recipients.indexOf(',') > 0)
////                                                       message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
////                               else
////                                                       message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
//
//                        List<String> recipients = email.getRecipients();
//                        List<String> bccRecipients = email.getBcc();
//                        List<String> ccRecipients = email.getCc();
//
//                        Address[] to_recipients = parseRecipients(recipients);
//                        Address[] cc_recipients = parseRecipients(ccRecipients);
//                        Address[] bcc_recipients = parseRecipients(bccRecipients);
//
//
//                        message.addRecipients(Message.RecipientType.TO, to_recipients);
//                        message.addRecipients(Message.RecipientType.CC, cc_recipients);
//                        message.addRecipients(Message.RecipientType.BCC, bcc_recipients);
//                        message.saveChanges();
//
//                        Transport.send(message);
//                } catch (Exception e) {
//
//                        throw new SMTPMailerException(e);
//                }
//
//        }

        private static Address[] parseRecipients(List<String> list) throws AddressException {
                Address rec[] = new InternetAddress[list.size()];
                int x = 0;
                for (Object to : list) {
                        rec[x++] = new InternetAddress((String) to);
                }
                return rec;
        }


//

}
