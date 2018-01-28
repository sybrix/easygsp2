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


import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sybrix.easygsp2.EasyGsp2Filter;
import sybrix.easygsp2.routing.Route;
import sybrix.easygsp2.routing.Routes;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;



public class RoutingCategory {
        private static final Logger logger = LoggerFactory.getLogger(RoutingCategory.class);

//        public static void add(GroovyObject self, String method, String path, String controller, String controllerMethod) {
//                add(self, method, path, controller, controllerMethod, new ArrayList<String>());
//        }
//
//        public static void add(GroovyObject self, String method, String path, String controller, String controllerMethod, List<String> parameters) {
//                List<String> paths = new ArrayList<String>();
//                paths.add(path);
//
//                List<String> method = new ArrayList<String>();
//                method.add(method);
//
//
//                add(self, method, paths, controller, controllerMethod, parameters);
//        }
//
//        public static void add(GroovyObject self, String method, List<String> paths, String controller, String controllerMethod) {
//                add(self, method, paths, controller, controllerMethod, new ArrayList<String>());
//        }
//
//        public static void add(GroovyObject self, String method, List<String> paths, String controller, String controllerMethod, List<String> parameters) {
//                List<String> method = new ArrayList<String>();
//                method.add(method);
//
//                add(self, method, paths, controller, controllerMethod, parameters);
//        }
//
//        public static void add(GroovyObject self, List<String> method, String path, String controller, String controllerMethod, List<String> parameters) {
//                List<String> paths = new ArrayList<String>();
//                paths.add(path);
//
//                add(self, method, paths, controller, controllerMethod, parameters);
//        }
//
//        public static void add(GroovyObject self, List<String> method, String path, String controller, String controllerMethod) {
//                add(self, method, path, controller, controllerMethod, new ArrayList<String>());
//        }
//
//        public static void add(GroovyObject self, List<String> httpMethods, List<String> paths, String controller, String controllerMethod, ) {
//                add(self, httpMethods, paths, controller, controllerMethod, new ArrayList<String>(),);
//        }

        public static void add(GroovyObject self, List<String> httpMethods, List<String> paths, String controller, String controllerMethod, List<String> parameters, List<String> roles, String[] accepts, String[] returns) {
                Binding binding = (Binding) self.getProperty("binding");
                Map<String, Route> routes = (Map) binding.getProperty("rroutes");
                GroovyClassLoader groovyClassLoader = (GroovyClassLoader) binding.getProperty("groovyClassLoader");

                Class[] parameterClasses = null;

                try {
                        Class cls = Class.forName(controller, true, groovyClassLoader);
                        parameterClasses = parseParameters(parameters, groovyClassLoader);

                        Method method = cls.getMethod(controllerMethod, parameterClasses);

                        for (String p : paths) {
                                logger.debug("mapping " + p + " to method " + method);

                                String[] r = new String[roles.size()];

                                for (String httpMethod : httpMethods) {
                                        Routes.add( method, p, httpMethod, parameterClasses, roles.toArray(r), accepts, returns);
                                }
                        }

                        return;
                } catch (ClassNotFoundException e) {
                        logger.error("adding route for path: '" + paths + "' failed (ClassNotFound). ", e);
                } catch (NoSuchMethodException e) {
                        logger.error("adding route for path: '" + paths + "' failed (NoSuchMethodException). ", e);
                }

//                for (String p : paths) {
//                        try {
//                                Class cls = Class.forName(controller, true, groovyClassLoader);
//                                for (Method m : cls.getMethods()) {
//                                        if (m.getName().equals(controllerMethod)) {
//                                                logger.finest("mapping " + p + " to method " + m);
//                                                for (String httpMethod : httpMethods) {
//                                                        Route.addRoute(routes, m, p, httpMethod, parameterClasses);
//                                                }
//                                                break;
//                                        }
//                                }
//
//                        } catch (ClassNotFoundException e) {
//                                logger.log(Level.SEVERE, "adding route for path: '" + p + "' failed (ClassNotFound). ", e);
//                        }
//                }
        }

        private static Class[] parseParameters(List<String> parameters, ClassLoader classLoader) {
                Class[] cls = new Class[parameters.size()];
                int i = 0;
                for (String parameter : parameters) {
                        if (parameter.equalsIgnoreCase("int") || parameter.equalsIgnoreCase("integer") || parameter.equalsIgnoreCase("java.lang.integer")) {
                                cls[i++] = java.lang.Integer.class;
                        } else if (parameter.equalsIgnoreCase("long") || parameter.equalsIgnoreCase("java.lang.Long")) {
                                cls[i++] = java.lang.Long.class;
                        } else if (parameter.equalsIgnoreCase("string") || parameter.equalsIgnoreCase("java.lang.String")) {
                                cls[i++] = java.lang.String.class;
                        } else if (parameter.equalsIgnoreCase("bool") || parameter.equalsIgnoreCase("java.lang.Boolean")) {
                                cls[i++] = java.lang.Boolean.class;
                        } else if (parameter.equalsIgnoreCase("dbl") || parameter.equalsIgnoreCase("java.lang.Double")) {
                                cls[i++] = java.lang.Double.class;
                        } else if (parameter.equalsIgnoreCase("short") || parameter.equalsIgnoreCase("java.lang.Short")) {
                                cls[i++] = java.lang.Short.class;
                        } else if (parameter.equalsIgnoreCase("byte") || parameter.equalsIgnoreCase("java.lang.byte")) {
                                cls[i++] = java.lang.Byte.class;
                        } else if (parameter.equalsIgnoreCase("float") || parameter.equalsIgnoreCase("java.lang.float")) {
                                cls[i++] = java.lang.Float.class;
                        } else if (parameter.equalsIgnoreCase("date") || parameter.equalsIgnoreCase("java.util.Date")) {
                                cls[i++] = java.util.Date.class;
                        } else if (parameter.equalsIgnoreCase("BigDecimal") || parameter.equalsIgnoreCase("java.math.BigDecimal")) {
                                cls[i++] = java.math.BigDecimal.class;
                        } else if (parameter.equalsIgnoreCase("BigInteger") || parameter.equalsIgnoreCase("java.math.BigInteger")) {
                                cls[i++] = java.math.BigInteger.class;
                        } else if (parameter.equalsIgnoreCase("request") || parameter.equalsIgnoreCase("javax.servlet.http.HttpServletRequest")) {
                                cls[i++] = javax.servlet.http.HttpServletRequest.class;
                        } else if (parameter.equalsIgnoreCase("response") || parameter.equalsIgnoreCase("javax.servlet.http.HttpServletResponse")) {
                                cls[i++] = javax.servlet.http.HttpServletResponse.class;
                        } else if (parameter.equalsIgnoreCase("app") || parameter.equalsIgnoreCase("javax.servlet.ServletContext")) {
                                cls[i++] = ServletContext.class;
                        } else {
                                try {
                                        cls[i++] = Class.forName(parameter, false, classLoader);
                                } catch (ClassNotFoundException e) {
                                        logger.error("Converting className to parameter failed for : " + parameter, e);
                                }
                        }
                }

                return cls;
        }
}
