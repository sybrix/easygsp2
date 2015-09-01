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
import groovy.lang.GroovyObject;
import sybrix.easygsp2.routing.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RoutingCategory {
        private static final Logger logger = Logger.getLogger(RoutingCategory.class.getName());

        public static void add(GroovyObject self, String method, String path, String controller, String controllerMethod) {
                List<String> paths = new ArrayList<String>();
                paths.add(path);

                List<String> methods = new ArrayList<String>();
                methods.add(method);


                add(self, methods, paths, controller, controllerMethod);
        }

        public static void add(GroovyObject self, String method, List<String> paths, String controller, String controllerMethod) {
                List<String> methods = new ArrayList<String>();
                methods.add(method);

                add(self, methods, paths, controller, controllerMethod);
        }

        public static void add(GroovyObject self, List<String> methods, String path, String controller, String controllerMethod) {
                List<String> paths = new ArrayList<String>();
                paths.add(path);

                add(self, methods, paths, controller, controllerMethod);
        }

        public static void add(GroovyObject self, List<String> methods, List<String> paths, String controller, String controllerMethod) {
                Binding binding = (Binding) self.getProperty("binding");
                Map<String, Route> routes = (Map) binding.getProperty("rroutes");

                for (String p : paths) {
                        try {
                                routes.put(p, new Route(methods, p, Class.forName(controller), controllerMethod));

                        } catch (ClassNotFoundException e) {
                                logger.log(Level.FINE, "adding route for " + p + "failed (ClassNotFound). ", e);
                        }
                }
        }
}
