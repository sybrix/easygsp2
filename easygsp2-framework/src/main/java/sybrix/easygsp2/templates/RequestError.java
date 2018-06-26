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

package sybrix.easygsp2.templates;

import sybrix.easygsp2.framework.ThreadBag;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * RequestError <br/>
 *
 * @author David Lee
 */
public class RequestError {
        private Throwable exception;
        private int lineNumber;
        private String errorMessage;
        private String stackTraceString;
        private String source;
        private String scriptPath;
        //private String templatePath;
        private String exceptionName;
        private String lineNumberMessage;
        private int errorCode;

        public Throwable getException() {
                return exception;
        }

        public void setException(Throwable exception, String appPath, String appPath2) {
                this.exception = exception;
                if (exception == null)
                        return;

                exceptionName = exception.getClass().getName();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);

                if (ThreadBag.get().getTemplateInfo().getUniqueTemplateScriptName() != null)
                        stackTraceString = sw.toString().replaceAll("\n", "<br/>").replace(appPath, "").replace(appPath2, "")
                                .replaceAll(ThreadBag.get().getTemplateInfo().getUniqueTemplateScriptName(), ThreadBag.get().getTemplateInfo().getRequestUri());
                else
                        stackTraceString = sw.toString().replaceAll("\n", "<br/>").replace(appPath, "").replace(appPath2, "");
        }

        public int getLineNumber() {
                return lineNumber;
        }
        public void setLineNumber(int lineNumber) {
                this.lineNumber = lineNumber;
        }
        public String getErrorMessage() {
                return errorMessage;
        }
        public void setErrorMessage(String errorMessage) {
                if (ThreadBag.get().getTemplateInfo().getUniqueTemplateScriptName() != null)
                        this.errorMessage = errorMessage.replaceAll(ThreadBag.get().getTemplateInfo().getUniqueTemplateScriptName(), ThreadBag.get().getTemplateInfo().getRequestUri());
                else
                        this.errorMessage = errorMessage;
        }
        public String getStackTraceString() {
                return stackTraceString;
        }
        public void setStackTraceString(String stackTraceString) {
                this.stackTraceString = stackTraceString;
        }
        public String getSource() {
                return source;
        }
        public void setSource(String source) {
                this.source = source;
        }

        public String getScriptPath() {
                return scriptPath;
        }
        public void setScriptPath(String scriptPath) {
                this.scriptPath = scriptPath;
        }
//        public String getTemplatePath() {
//                return templatePath;
//        }
//        public void setTemplatePath(String templatePath) {
//                this.templatePath = templatePath;
        //        }
        public void setExceptionName(String exceptionName) {
                this.exceptionName = exceptionName;
        }

        public String getExceptionName() {
                return exceptionName;
        }
        public void setLineNumberMessage(String lineNumberMessage) {
                this.lineNumberMessage = lineNumberMessage;
        }
        public String getLineNumberMessage() {
                return lineNumberMessage;
        }

        public int getErrorCode() {
                return errorCode;
        }
        public void setErrorCode(int errorCode) {
                this.errorCode = errorCode;
        }
}
