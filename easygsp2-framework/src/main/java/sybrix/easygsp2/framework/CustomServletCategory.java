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

package sybrix.easygsp2.framework;

import groovy.lang.GroovyObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class CustomServletCategory {

        public static Object get(ServletContext context, String key) {
                return context.getAttribute(key);
        }

        public static Object get(HttpSession session, String key) {
                return session.getAttribute(key);
        }

        public static Object get(ServletRequest request, String key) {
                return request.getAttribute(key);
        }

        public static Object getAt(ServletContext context, String key) {
                return context.getAttribute(key);
        }

        public static Object getAt(HttpSession session, String key) {
                return session.getAttribute(key);
        }

        public static Object getAt(ServletRequest request, String key) {
                return request.getAttribute(key);
        }

        public static void set(ServletContext context, String key, Object value) {
                context.setAttribute(key, value);
        }

        public static void set(HttpSession session, String key, Object value) {
                session.setAttribute(key, value);
        }

        public static void set(ServletRequest request, String key, Object value) {
                request.setAttribute(key, value);
        }

        public static void putAt(ServletContext context, String key, Object value) {
                context.setAttribute(key, value);
        }

        public static void putAt(HttpSession session, String key, Object value) {
                session.setAttribute(key, value);
        }

        public static void putAt(ServletRequest request, String key, Object value) {
                request.setAttribute(key, value);
        }

        public static void render(GroovyObject self, Object s) {
                //Binding binding = (Binding) self.getProperty("binding");
                HttpServletResponse response =  ThreadBag.get().getResponse();

                try {
                        response.getWriter().write(s.toString());
                        response.getWriter().flush();
                } catch (IOException e) {
                        throw new RuntimeException("render() failed", e);
                }
        }

        public static void redirect(GroovyObject self, String s) throws IOException {
                HttpServletResponse response =  ThreadBag.get().getResponse();
                HttpServletRequest request =  ThreadBag.get().getRequest();

                request.setAttribute("__redirect__", true); // when true, controllers don't auto forward
                response.sendRedirect(s);
        }

        public static void forward(GroovyObject self, String s) throws IOException, ServletException {
                HttpServletRequest request =  ThreadBag.get().getRequest();
                HttpServletResponse response =  ThreadBag.get().getResponse();
                request.setAttribute("_explicitForward", true);

                request.getRequestDispatcher(s).forward(request,response);
        }

//        public static void bind(GroovyObject self, String name, Object val) throws IOException, ServletException {
//                Binding binding = (Binding) self.getProperty("binding");
//                binding.setVariable(name, val);
//        }
//
//        public static void bind(GroovyObject self, Map<String,Object> values) throws IOException, ServletException {
//                Binding binding = (Binding) self.getProperty("binding");
//                for(String key:values.keySet()){
//                        binding.setVariable(key, values.get(key));
//                }
//        }

        public static Cookie getCookie(GroovyObject self, String cookieName) {
                // ServletContextImpl app = RequestThreadInfo.get().getApplication();

                HttpServletRequest request =  ThreadBag.get().getRequest();
                Cookie[] cookies = request.getCookies();
                for(Cookie c: cookies){
                        if (c.getName().equalsIgnoreCase(cookieName)){
                                return c;
                        }
                }

                return null;
        }

        public static Cookie setCookie(GroovyObject self, Map<String, String> parameters) {
                String cookieName = (String) parameters.get("cookieName");
                String value = (String) parameters.get("url");
                String path = (String) parameters.get("path");
                String domain = (String) parameters.get("domain");
                Object secure = parameters.get("secure");
                Object maxAge = parameters.get("maxAge");

                return setCookie(self, cookieName, value, maxAge, path, domain, secure);
        }



        /**
         * @param self
         * @param cookieName
         * @param args
         */
        public static Cookie setCookie(GroovyObject self, String cookieName, Object... args) {

                HttpServletResponse response =  ThreadBag.get().getResponse();
                Cookie cookie = new Cookie(cookieName, args.length > 0 ? args[0].toString() : "");

                if (args.length > 1)
                        if (args[1] != null)
                                cookie.setMaxAge(Integer.parseInt(args[1].toString()) * 60 * 60 * 24);

                if (args.length > 2)
                        if (args[2] != null && args[2].toString().length() > 0)
                                cookie.setPath(args[2].toString());

                if (args.length > 3)
                        if (args[3] != null && args[3].toString().length() > 0)
                                cookie.setDomain(args[3].toString());

                if (args.length > 4)
                        if (args[4] != null)
                                cookie.setSecure(args[4].toString().equalsIgnoreCase("true"));


                response.addCookie(cookie);

                return cookie;
        }

//        private static Cookie setCookie(GroovyObject self, Cookie cookieName, Object...cookieOptions){
//              // ServletContextImpl app = RequestThreadInfo.get().getApplication();
//                Binding binding = (Binding)self.getProperty("binding");
//                RequestImpl request = (RequestImpl)binding.getVariable("request");
//                Cookie c;
//
//
//                //0 - url
//                //1 - int Age
//                //2 - path
//                //3 - domain
//                //4 - secure
//                return "$Version=" + cookie.getVersion() + "; " + cookie.getName() + "=" +
//                        cookie.getValue() + (cookie.getPath() == null ? "" : "; $Path=" +
//                        cookie.getPath()) + (cookie.getDomain() == null ? "" : "; $Domain=" +
//                        cookie.getDomain());
//
//                Cookie c = new Cookie(cookieName, url)
//                return null;
//        }



}
