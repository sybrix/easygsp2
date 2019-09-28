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

import javax.activation.DataSource;
import java.io.*;

public class ByteArrayDataSource implements DataSource {
        private ByteArrayInputStream stream;
        private String contentType;
        private String name;
        private ByteArrayOutputStream outstream;


        public ByteArrayDataSource(String name, byte[] content, String contentType) {
                stream = new ByteArrayInputStream(content);
                outstream = new ByteArrayOutputStream();
                this.contentType = contentType;
                this.name = name;

                try {
                        outstream.write(content);
                } catch (IOException e) {

                }
        }

        public void setStream(ByteArrayInputStream stream) {
                this.stream = stream;
        }

        public void setContentType(String contentType) {
                this.contentType = contentType;
        }

        public void setName(String name) {
                this.name = name;
        }


        public String getContentType() {
                return contentType;
        }

        public InputStream getInputStream() {
                stream.reset();
                return stream;
        }

        public String getName() {
                return name;
        }

        public OutputStream getOutputStream() {
                return outstream;
        }
}