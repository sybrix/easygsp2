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


import javax.servlet.ServletContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;

/**
 * StringUtil <br/>
 *
 * @author David Lee
 */
public class StringUtil {

        static SimpleDateFormat sdf_short = new SimpleDateFormat("MM/dd/yyyy");
        static SimpleDateFormat sdf_long = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

        public static String[] split(String value, char delimiter) {
                if (value == null) {
                        return new String[]{};
                }

                String[] val = value.split("\\" + delimiter);
                String[] returnVal = new String[val.length];
                int i = 0;
                for (String v : val) {
                        returnVal[i++] = v.trim();
                }

                return returnVal;
        }


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
                                        newColumn.append("_");
                                }

                                newColumn.append(Character.toLowerCase(column.charAt(i)));
                        } else {
                                newColumn.append(column.charAt(i));
                        }
                }

                return newColumn.toString();
        }

        public static Boolean isDate(String val, String... format) {
                if (val == null) {
                        return false;
                }
                if (format == null) {
                        return Validator.isDate(val, "MM/dd/yyyy");
                } else {
                        return Validator.isDate(val, format[0]);
                }
        }

        public static Object ifNull(Object val, Object defaultVal) {
                if (StringUtil.isEmpty(val)) {
                        return defaultVal;
                } else {
                        return val;
                }
        }

        public static Timestamp getNow() {
                return new Timestamp(System.currentTimeMillis());
        }

        public static String formatDate(java.util.Date val, String format) {

                Date dt;
                if (val instanceof java.util.Date) {
                        dt = new java.util.Date(((Date) val).getTime());
//                } else if (val instanceof java.util.Date) {
//                        dt = (Date) val;
                } else if (val == null) {
                        return "";
                } else {
                        throw new RuntimeException("formatDate requires java.util.Date or java.sql.Date");
                }

                if (format == null) {
                        return sdf_short.format(dt);
                } else if (format.toString().equalsIgnoreCase("short")) {
                        return sdf_short.format(dt);
                } else if (format.toString().equalsIgnoreCase("long")) {
                        return sdf_long.format(dt);
                } else {
                        SimpleDateFormat sdf = new SimpleDateFormat(format.toString());
                        return sdf.format(dt);
                }
        }

        public static void addProperties(ServletContext app, PropertiesFile propFile) {
                Enumeration en = propFile.propertyNames();
                while (en.hasMoreElements()) {
                        String key = (String) en.nextElement();
                        app.setAttribute(key, propFile.get(key));
                }
        }

        public static Date toDate(Object dt) throws ParseException {
                if (StringUtil.isEmpty(dt.toString())) {
                        return null;
                }

                return sdf_short.parse(dt.toString());
        }

        public static String format(Number val, String pattern) {
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                return decimalFormat.format(val.doubleValue());

        }

        public static Date toDate(Object dt, String format) throws ParseException {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(dt.toString());
        }

        public static String toString(Throwable e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                return sw.toString();
        }

        public static boolean contains(String[] ary, String sought) {
                for (String s : ary) {
                        if (s.equals(sought)) {
                                return true;
                        }
                }

                return false;
        }

        public static String randomCode(int len) {
                int leftLimit1 = 97; // letter 'a'
                int rightLimit1 = 122; // letter 'z'

                int leftLimit2 = 65; // letter 'A'
                int rightLimit2 = 90; // letter 'Z'

                int leftLimit3 = 48; // letter 'A'
                int rightLimit3 = 57; // letter 'Z'

                int min = leftLimit3;
                int max = rightLimit1;

                SecureRandom random = new SecureRandom();
                StringBuilder buffer = new StringBuilder(len);

                while (true) {
                        int val = min + (int) (random.nextFloat() * (max - min + 1));

                        if ((val >= leftLimit1 && val <= rightLimit1) ||
                                (val >= leftLimit2 && val <= rightLimit2) ||
                                (val >= leftLimit3 && val <= rightLimit3)
                        ) {
                                buffer.append((char) val);
                                if (buffer.length() >= len) {
                                        break;
                                }
                        } else {
                                continue;
                        }
                }
                return buffer.toString();

        }

        public static String randomNumberString(int len) {


                int min = 0;
                int max = 9;

                SecureRandom random = new SecureRandom();
                StringBuilder buffer = new StringBuilder(len);

                while (true) {
                        Integer val = min + (int) (random.nextFloat() * (max - min + 1));

                        buffer.append(val.toString());
                        if (buffer.length() >= len) {
                                break;
                        }

                }
                return buffer.toString();

        }

        public static String[] combine(String[]... arrays) {
                HashSet<String> data = new HashSet<String>();

                for (String[] ary : arrays) {
                        for (String s : ary) {
                                data.add(s);
                        }
                }

                data.remove("");

                return data.toArray(new String[data.size()]);
        }

        public static String extractFileExtension(String s) {
                int i = s.lastIndexOf(".");
                if (i >= 0) {

                        return s.substring(i);
                }
                return null;
        }
}
