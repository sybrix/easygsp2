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

import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet-specific binding extension to lazy load the writer or the output
 * stream from the response.
 * <p/>
 * <p>
 * <h3>Default variables bound</h3>
 * <ul>
 * <li><tt>"request"</tt> : the HttpServletRequest object</li>
 * <li><tt>"response"</tt> : the HttpServletResponse object</li>
 * <li><tt>"context"</tt> : the ServletContext object </li>
 * <li><tt>"application"</tt> : same as context</li>
 * <li><tt>"session"</tt> : convenient for <code>request.getSession(<b>false</b>)</code> - can be null!</li>
 * <li><tt>"params"</tt> : map of all form parameters - can be empty</li>
 * <li><tt>"headers"</tt> : map of all <b>request</b> header fields</li>
 * </ul>
 * <p/>
 * <p>
 * <h3>Implicite bound variables</h3>
 * <ul>
 * <li><tt>"out"</tt> : response.getWriter() </li>
 * <li><tt>"sout"</tt> : response.getOutputStream() </li>
 * <li><tt>"html"</tt> : new MarkupBuilder(response.getWriter()) </li>
 * </ul>
 * </p>
 *
 * @author Guillaume Laforge
 * @author Christian Stein
 */
@SuppressWarnings("unchecked")
public class CustomServletBinding extends Binding {

        private final Binding binding;

        private final ServletContext context;

        private final HttpServletRequest request;

        private final HttpServletResponse response;

        private MarkupBuilder html;

        /**
         * Initializes a servlet binding.
         */
        public CustomServletBinding(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
                this.binding = new Binding();
                this.request = request;
                this.response = response;
                this.context = context;

                /*
                * Bind the default variables.
                */
                binding.setVariable("request", request);
                binding.setVariable("response", response);
                binding.setVariable("context", context);
                binding.setVariable("application", context);
                binding.setVariable("app", context);
                binding.setVariable("errors", new HashMap());

                /*
                * Bind the HTTP session object - if there is one.
                * Note: we don't create one here!
                */
                if (request != null) {
                        binding.setVariable("session", request.getSession(false));

                        /*
                        * Bind form parameter key-url hash map.
                        *
                        * If there are multiple, they are passed as an array.
                        */
                        populateParameters(request);
                        bindRequestAttributes(request);
                }


                /*
                * Bind request header key-url hash map.
                */
//                Map headers = new HashMap();
//                for (Enumeration names = request.getHeaderNames(); names.hasMoreElements();) {
//                        String headerName = (String) names.nextElement();
//                        String headerValue = request.getHeader(headerName);
//
//                        if (headerName.startsWith("HTTP_")){
//                                headers.put(headerName.substring(5), headerValue);
//                        } else {
//                                headers.put(headerName, headerValue);
//                        }
//                }
        }



        protected Map populateParameters(HttpServletRequest request) {
                Map<String, Object> params = new HashMap<String, Object>();
                        for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements(); ) {
                        String name = (String) names.nextElement();
                        if (!binding.getVariables().containsKey(name)) {
                                String[] values = request.getParameterValues(name);

                                if (values == null) {
                                        params.put(name, "");
                                } else if (values.length == 1) {
                                        params.put(name, values[0]);
                                } else {
                                        params.put(name, values);
                                }
                        }
                }

                binding.setVariable("params", params);

                return params;
        }

        protected void bindRequestAttributes(HttpServletRequest request) {

                for (Enumeration names = request.getAttributeNames(); names.hasMoreElements(); ) {
                        String name = (String) names.nextElement();
                        if (!binding.getVariables().containsKey(name)) {
                                Object value = request.getAttribute(name);
                                binding.setVariable(name, value);
                        }
                }
        }


        class NoNullMap extends HashMap<String, Object> {
                @Override
                public Object get(Object key) {
                        if (super.get(key) == null) {
                                return "";
                        } else {
                                return super.get(key);
                        }
                }
        }

        public void setVariable(String name, Object value) {
                /*
                * Check sanity.
                */
                if (name == null) {
                        throw new IllegalArgumentException("Can't bind variable to null key.");
                }
                if (name.length() == 0) {
                        throw new IllegalArgumentException("Can't bind variable to blank key name. [length=0]");
                }
                /*
                * Check implicite key names. See getVariable(String)!
                */
                if ("out".equals(name)) {
                        throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
                }
                if ("sout".equals(name)) {
                        throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
                }
                if ("html".equals(name)) {
                        throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
                }
                /*
                * TODO Check default key names. See constructor(s).
                */

                /*
                * All checks passed, set the variable.
                */
                binding.setVariable(name, value);
        }

        public Map<String, Object> getVariables() {
                return binding.getVariables();
        }

        /**
         * @return a writer, an output stream, a markup builder or another requested object
         */
        public Object getVariable(String name) {
                /*
                * Check sanity.
                */
                if (name == null) {
                        throw new IllegalArgumentException("No variable with null key name.");
                }
                if (name.length() == 0) {
                        throw new IllegalArgumentException("No variable with blank key name. [length=0]");
                }
                /*
                * Check implicite key names. See setVariable(String, Object)!
                */
                try {
//                        if ("out".equals(name)) {
//                                return response.getWriter();
//                        }
//                        if ("sout".equals(name)) {
//                                return response.getOutputStream();
//                        }

                        if ("html".equals(name)) {
                                if (html == null) {
                                        html = new MarkupBuilder(response.getWriter());
                                }
                                return html;
                        }
                } catch (IOException e) {
                        String message = "Failed to get writer or output stream from response.";
                        context.log(message, e);
                        throw new RuntimeException(message, e);
                }
                /*
                * Still here? Delegate to the binding object.
                */

                return binding.getVariable(name);
        }


}
