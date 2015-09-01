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


import java.io.UnsupportedEncodingException;

/**
 * StringUtil <br/>
 *
 * @author David Lee
 */
public class StringUtil {
        public static boolean isEmpty(Object value) {
                if (value == null) {
                        return true;
                } else if (value.toString().trim().length() == 0) {
                        return true;
                }
                return false;
        }




        public static String escapeXML(String s) {
                return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
        public static String capFirstLetter(String s) {
                return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        public static String urlDecode(String s) throws UnsupportedEncodingException {
                if (s == null) {
                        return "";
                }
                return java.net.URLDecoder.decode(s, "UTF-8");
        }

        public static String urlEncode(String s) throws UnsupportedEncodingException {
                if (s == null) {
                        return "";
                }
                return java.net.URLEncoder.encode(s, "UTF-8");
        }

        public static String htmlEncode(String s) {
                StringBuffer encodedString = new StringBuffer("");
                char[] chars = s.toCharArray();
                for (char c : chars) {
                        if (c == '<') {
                                encodedString.append("&lt;");
                        } else if (c == '>') {
                                encodedString.append("&gt;");
                        } else if (c == '\'') {
                                encodedString.append("&apos;");
                        } else if (c == '"') {
                                encodedString.append("&quot;");
                        } else if (c == '&') {
                                encodedString.append("&amp;");
                        } else {
                                encodedString.append(c);
                        }
                }
                return encodedString.toString();
        }

        public static String camelCase(String column) {
                StringBuffer newColumn = new StringBuffer();
                boolean underScoreFound = false;
                int index = -1;
                int currentPosition = 0;
                while ((index = column.indexOf('_', currentPosition)) > -1) {
                        newColumn.append(column.substring(currentPosition, index).toLowerCase());
                        newColumn.append(column.substring(index + 1, index + 2).toUpperCase());

                        currentPosition = index + 2;
                        underScoreFound = true;
                }

                if (underScoreFound == false) {
                        return column;
                } else {
                        newColumn.append(column.substring(currentPosition, column.length()).toLowerCase());
                }

                return newColumn.toString();


        }

        public static String unCamelCase(String column) {
                StringBuffer newColumn = new StringBuffer();
                for (int i = 0; i < column.length(); i++) {
                        if (Character.isLetter(column.charAt(i)) && Character.isUpperCase(column.charAt(i))) {
                                if (i > 0) {
                                        newColumn.append("_")    ;
                                }

                                newColumn.append(Character.toLowerCase(column.charAt(i)));
                        } else {
                                newColumn.append(column.charAt(i));
                        }
                }

                return newColumn.toString();
        }
}
