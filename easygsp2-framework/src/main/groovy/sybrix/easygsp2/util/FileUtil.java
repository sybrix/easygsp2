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

package sybrix.easygsp2.util;

import java.io.*;

/**
 * FileUtil <br/>
 *
 * @author David Lee
 */
public class FileUtil {
        public static void copy(String sourceLocation, String targetLocation) throws IOException {
                copyDirectory(new File(sourceLocation), new File(targetLocation));

        }

        public static boolean deleteDirectory(String path) {
                return deleteDirectory(new File(path));
        }

        public static boolean deleteDirectory(File path) {
                if (path.exists()) {
                        File[] files = path.listFiles();
                        for (int i = 0; i < files.length; i++) {
                                if (files[i].isDirectory()) {
                                        deleteDirectory(files[i]);
                                } else {
                                        files[i].delete();
                                }
                        }
                }
                return (path.delete());
        }

        public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

                if (sourceLocation.isDirectory()) {
                        if (!targetLocation.exists()) {
                                targetLocation.mkdir();
                        }

                        String[] children = sourceLocation.list();
                        for (int i = 0; i < children.length; i++) {
                                copyDirectory(new File(sourceLocation, children[i]),
                                        new File(targetLocation, children[i]));
                        }
                } else {
                        InputStream in = new FileInputStream(sourceLocation);
                        OutputStream out = new FileOutputStream(targetLocation);

                        // Copy the bits from instream to outstream
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                }
        }
}
