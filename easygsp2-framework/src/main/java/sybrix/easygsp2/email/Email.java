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

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Email <br/>
 *
 * @author David Lee
 */
public class Email {
        private String host;
        private int port;
        private String from;
        private List<String> recipients = new ArrayList<String>();
        private List<String> bcc= new ArrayList<String>();;
        private List<String> cc= new ArrayList<String>();;
        private String contextType;
        private String body;
        private String htmlBody;
        private String subject;
        private String username;
        private String password;
        private boolean authenticationRequired;
        private boolean secure;
        private Map attachments = new HashMap();
       // private ServletContext app;

        public String getHtmlBody() {
                return htmlBody;
        }
        public void setHtmlBody(String htmlBody) {
                this.htmlBody = htmlBody;
        }
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

        public List<String> getRecipients() {
                return recipients;
        }
        public void setRecipients(List<String> recipients) {
                this.recipients = recipients;
        }
        public List<String> getBcc() {
                return bcc;
        }
        public void setBcc(List<String> bcc) {
                this.bcc = bcc;
        }
        public List<String> getCc() {
                return cc;
        }
        public void setCc(List<String> cc) {
                this.cc = cc;
        }
        public String getContextType() {
                return contextType;
        }
        public void setContextType(String contextType) {
                this.contextType = contextType;
        }
        public String getBody() {
                return body;
        }
        public void setBody(String body) {
                this.body = body;
        }
        public String getSubject() {
                return subject;
        }
        public void setSubject(String subject) {
                this.subject = subject;
        }

        public boolean isAuthenticationRequired() {
                return authenticationRequired;
        }
        public void setAuthenticationRequired(boolean authenticationRequired) {
                this.authenticationRequired = authenticationRequired;
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

        public boolean isSecure() {
                return secure;
        }

        public void setSecure(boolean secure) {
                this.secure = secure;
        }

        public Map<String, Object> getAttachments() {
                return attachments;
        }

        public void setAttachments(Map attachments) {
                this.attachments = attachments;
        }

        public void send(){
                EmailService.addEmail(this);
        }


        @Override
        public String toString() {
                return "Email{" +
                        "host='" + host + '\'' +
                        ", port=" + port +
                        ", from='" + from + '\'' +
                        ", recipients=" + recipients +
                        ", bcc=" + bcc +
                        ", cc=" + cc +
                        ", contextType='" + contextType + '\'' +
                        ", body='" + body + '\'' +
                        ", htmlBody='" + htmlBody + '\'' +
                        ", subject='" + subject + '\'' +
                        ", username='" + username + '\''+ 
                        ", password='" + password + '\'' +
                        ", authenticationRequired=" + authenticationRequired +
                        ", secure=" + secure +
                        ", attachments=" + attachments +
                        '}';
        }
}
