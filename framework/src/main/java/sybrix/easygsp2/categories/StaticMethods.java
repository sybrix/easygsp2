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

package sybrix.easygsp2.categories;


import groovy.lang.Closure;
import groovy.sql.Sql;
import org.codehaus.groovy.runtime.GStringImpl;
import sybrix.easygsp2.db.CurrentSQLInstance;
import sybrix.easygsp2.email.Email;
import sybrix.easygsp2.email.EmailService;
import sybrix.easygsp2.exceptions.SendEmailException;
import sybrix.easygsp2.util.*;

import javax.servlet.ServletContext;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author David Lee
 */
public class StaticMethods {
        private static final Logger logger = Logger.getLogger(StaticMethods.class.getName());

        static SimpleDateFormat sdf_short = new SimpleDateFormat("MM/dd/yyyy");
        static SimpleDateFormat sdf_long = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

        public static Timestamp getNow() {
                return new Timestamp(System.currentTimeMillis());
        }




        private static void callMethod(Object ds, String methodName, Object parameterValue) {

                try {
                        Method method = null;

                        Method[] methods = ds.getClass().getMethods();
                        for (Method m : methods) {
                                if (m.getName().equals(methodName) && m.getParameterTypes().length == 1) {
                                        method = m;
                                }
                        }

                        Class cls = method.getParameterTypes()[0];
                        if (cls.getName().contains("boolean")) {
                                cls = Boolean.class;
                        } else if (cls.getName().contains("int")) {
                                cls = Integer.class;
                        } else if (cls.getName().contains("long")) {
                                cls = Long.class;
                        } else if (cls.getName().contains("double")) {
                                cls = Double.class;
                        }

                        Constructor constructor = cls.getConstructor(String.class);
                        Object val = constructor.newInstance(parameterValue.toString());

                        Method m = ds.getClass().getMethod(methodName, method.getParameterTypes()[0]);
                        m.invoke(ds, val);
                } catch (Throwable e) {
                        throw new RuntimeException("Error setting DataSource property. datasource=" + ds.toString() + ", methodName=" + methodName + ", " +
                                "value=" + parameterValue, e);
                }
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




//        public static String formatMoney(String val) {
//                BigDecimal v = toBD(val);
//                return formatMoney(v);
//        }

//        public static String formatMoney(Number val) {
//                return formatMoney(val, Locale.getDefault());
//        }

//        public static String formatMoney(Number val, Locale locale) {
//                if (val == null) {
//                        return "";
//                }
//                String prefix = (String) RequestThreadInfo.get().getApplication().getAttribute("negative.money.prefix");
//                String suffix = (String) RequestThreadInfo.get().getApplication().getAttribute("negative.money.suffix");
//
//                DecimalFormat moneyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
//                if (prefix != null) {
//                        moneyFormatter.setNegativePrefix(prefix);
//                }
//                if (suffix != null) {
//                        moneyFormatter.setNegativeSuffix(suffix);
//                }
//
//                if (val instanceof Double) {
//                        return moneyFormatter.format(val);
//                } else {
//                        return moneyFormatter.format(new Double(val.toString()));
//                }
//        }

        public static String format(Number val, String pattern) {
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                return decimalFormat.format(val.doubleValue());

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

        public static Date toDate(Object dt, String format) throws ParseException {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(dt.toString());
        }

//        public static Object withTransaction(Closure closure) {
//                return Model.withTransaction(closure);
//        }



        //        public static BigDecimal round(Object obj, int scale) {
//                BigDecimal d = new BigDecimal(obj.toString());
//                return d.setScale(scale, BigDecimal.ROUND_HALF_UP);
//        }
        public static String toString(Throwable e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                return sw.toString();
        }


//        public static int datepart(Date dt, DateParts datePart) {
//                if (DateParts.Year == datePart){
//                        return new GregorianCalendar() .getInstance().get(Calendar.YEAR);
//                } else if (DateParts.Year == datePart){
//
//                }
//        }

}
