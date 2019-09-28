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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoggerThread <br/>
 *
 * @author David Lee
 */
public class EmailServiceImpl {
        private static final Logger logger = Logger.getLogger(EmailThread.class.getName());

        private EmailThread emailThread;
        private SMTPMailer smtpMailer;

        public void start() {
                emailThread = new EmailThread(smtpMailer);
                emailThread.start();
        }

        public void sendEmail(Email email) {
                emailThread.addEmail(email);
        }

        public void stop() {
                emailThread.stopThread();
        }

        private class EmailThread extends Thread {

                private volatile boolean stopped = false;
                private List<Email> emails = Collections.synchronizedList(new ArrayList());
                private SMTPMailer smtpMailer;

                public EmailThread(SMTPMailer smtpMailer){
                       this.smtpMailer = smtpMailer;
                }

                @Override
                public void run() {
                        logger.info("EmailService thread started");
                        while (true) {
                                if (emails.size() == 0) {
                                        synchronized (emails) {
                                                try {
                                                        emails.wait();
                                                } catch (InterruptedException e) {

                                                }
                                        }
                                }

                                if (stopped && emails.size() == 0)
                                        break;

                                send(emails.remove(0));
                        }

                        logger.info("EmailService thread stopped");
                }

                public void send(Email email) {
                        try {
                                logger.fine("sending email: " + email);
                                smtpMailer.send(email);
                        } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                }

                public void addEmail(Email email) {
                        synchronized (emails) {
                                emails.add(email);
                                emails.notifyAll();
                        }
                }

                public int emailsInQueue() {
                        return emails.size();
                }

                public void stopThread() {
                        this.stopped = true;
                        logger.fine("EmailService thread stop requested...");
                        this.interrupt();
                }
        }

}
