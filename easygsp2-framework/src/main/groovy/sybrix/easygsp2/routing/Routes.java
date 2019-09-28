package sybrix.easygsp2.routing;

import sybrix.easygsp2.framework.ThreadBag;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

public class Routes {
        private static final Logger logger = Logger.getLogger(Routes.class.getName());

        public static boolean contains(String pattern, String httpMethod) {
                Map<String, Route> routes = ThreadBag.get().getRoutes();

                String key = pattern + "|" + httpMethod.toUpperCase();
                return routes.containsKey(key);
        }

        public static Route addRoute(String httpMethod, String pattern, Class clazz, String classMethod, Class[] parameters, String roles, String[] accepts, String[] returns, boolean secure) {
                Map<String, Route> routes = ThreadBag.get().getRoutes();
                String key = pattern + "|" + httpMethod.toUpperCase();

                String[] r = new String[]{};
                if (roles != null) {
                        r = roles.split(",");
                }

                return add(httpMethod, pattern, clazz, classMethod, parameters, key, r, accepts, returns, secure);
        }

        public static Route add(String httpMethod, String pattern, Class clazz, String classMethod, Class[] parameters, String[] roles, String accepts[], String returns[], boolean secure) {
                String key = pattern + "|" + httpMethod.toUpperCase();

                return add(httpMethod, pattern, clazz, classMethod, parameters, key, roles, accepts, returns, secure);
        }

        private static Route add(String httpMethod, String pattern, Class clazz, String classMethod, Class[] parameters, String key, String[] roles, String[] accepts, String[] returns, boolean secure) {
                Map<String, Route> routes = ThreadBag.get().getRoutes();
                if (routes.containsKey(key)) {
                        Route route = routes.get(key);
                        route.setDuplicate(true);
                        logger.severe("api httpMethod: '" + httpMethod + "', url: '" + pattern + "' already mapped.");
                        return  route;
                } else {
                        try {
                                Method m = clazz.getMethod(classMethod, parameters);
                                Route route = new Route(httpMethod, pattern, m.getDeclaringClass(), m, parameters, roles, accepts, returns, secure);
                                routes.put(key, route);
                                return  route;
                        } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                        }
                }

                return null;
        }

        public static Route add(Method m, String pattern, String httpMethod, Class[] parameters, String[] roles, String[] accepts, String[] returns, boolean secure) {
                Map<String, Route> routes = ThreadBag.get().getRoutes();
                String key = pattern + "|" + httpMethod.toUpperCase();

                if (routes.containsKey(key)) {
                        Route route = routes.get(key);
                        route.setDuplicate(true);
                        logger.severe("api httpMethod: '" + httpMethod + "', url: '" + pattern + "' already mapped.");
                        return  route;
                } else {
                        Route route = new Route(httpMethod, pattern, m.getDeclaringClass(), m, parameters, roles, accepts, returns, secure);
                        routes.put(key, route);
                        return  route;
                }

        }

//        public static void add(Method m, String[] patterns, String httpMethod, Class[] parameters, String[] roles, String[] accepts, String[] returns) {
//                for (String pattern : patterns) {
//                        add(m, pattern, httpMethod, parameters, roles, accepts, returns);
//                }
//        }
}
