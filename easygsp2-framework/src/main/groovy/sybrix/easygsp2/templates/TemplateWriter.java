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
package sybrix.easygsp2.templates;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import sybrix.easygsp2.exceptions.TemplateException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * A generic servlet for serving (mostly HTML) templates.
 * <p/>
 * <p/>
 * It delegates work to a <code>groovy.text.TemplateEngine</code> implementation
 * processing HTTP requests.
 * <p/>
 * <h4>Usage</h4>
 * <p/>
 * <code>helloworld.html</code> is a headless HTML-like template
 * <pre><code>
 *  &lt;html&gt;
 *    &lt;body&gt;
 *      &lt;% 3.times { %&gt;
 *        Hello World!
 *      &lt;% } %&gt;
 *      &lt;br&gt;
 *    &lt;/body&gt;
 *  &lt;/html&gt;
 * </code></pre>
 * <p/>
 * Minimal <code>web.xml</code> example serving HTML-like templates
 * <pre><code>
 * &lt;web-app&gt;
 *   &lt;servlet&gt;
 *     &lt;servlet-name&gt;template&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;template&lt;/servlet-name&gt;
 *     &lt;url-url&gt;*.html&lt;/url-url&gt;
 *   &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </code></pre>
 * <p/>
 * <h4>Template engine configuration</h4>
 * <p/>
 * <p/>
 * By default, the TemplateServer uses the {@link groovy.text.SimpleTemplateEngine}
 * which interprets JSP-like templates. The init parameter <code>template.engine</code>
 * defines the fully qualified class name of the template to use:
 * <pre>
 *   template.engine = [empty] - equals groovy.text.SimpleTemplateEngine
 *   template.engine = groovy.text.SimpleTemplateEngine
 *   template.engine = groovy.text.GStringTemplateEngine
 *   template.engine = groovy.text.XmlTemplateEngine
 * </pre>
 * <p/>
 * <h4>Logging and extra-output options</h4>
 * <p/>
 * <p/>
 * This implementation provides a verbosity flag switching log statements.
 * The servlet init parameter name is:
 * <pre>
 *   generated.by = true(default) | false
 * </pre>
 *
 * @author Christian Stein
 * @author Guillaume Laforge
 * @version 2.0
 */

public class TemplateWriter {
        private static final Logger logger = Logger.getLogger(TemplateWriter.class.getName());
        public static final java.lang.String CONTENT_TYPE_TEXT_HTML = "text/html";
        protected String encoding = "UTF-8";

        /**
         * Simple file name to template cache map.
         */
        private Map cache;
        private Map<String, List<String>> dependencyCache;

        /**
         * Underlying template engine used to evaluate template source files.
         */
        private TemplateEngine engine;


        public TemplateWriter(GroovyClassLoader classLoader) {
                try {
                        this.cache = Collections.synchronizedMap(new HashMap());
                        this.dependencyCache = new DependencyCache();
                        CompilerConfiguration c = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
                        //c.setTargetDirectory("c:/temp/test/views");

                        this.engine = new IncludeTemplateEngine(classLoader, c);

                } catch (Throwable e) {
                        throw new RuntimeException(e);
                }

        }

        /**
         * Gets the template created by the underlying engine parsing the request.
         * <p/>
         * <p>
         * This method looks up a simple (weak) hash map for an existing template
         * object that matches the source file. If the source file didn't change in
         * length and its last modified stamp hasn't changed compared to a precompiled
         * template object, this template is used. Otherwise, there is no or an
         * invalid template object cache entry, a new one is created by the underlying
         * template engine. This new instance is put to the cache for consecutive
         * calls.
         * </p>
         *
         * @param file The HttpServletRequest.
         * @return The template that will produce the response text.
         * @throws javax.servlet.ServletException If the request specified an invalid template source file
         */
        protected Template getTemplate(File file, TemplateInfo templateInfo) {

                String key = file.toURI().toString();
                Template template = null;

                /*
                 * Test cache for a valid template bound to the key.
                 */
                TemplateCacheEntry entry = (TemplateCacheEntry) cache.get(key);
                if (entry != null) {
                        if (entry.validate(file)) {
                                logger.finest("Cache hit! " + entry);
                                template = entry.template;

                                return template;
                        } else {
                                logger.finest("Cached template needs recompiliation!");
                        }

                }

                //
                // Template not cached or the source file changed - compile new template!
                //
                if (template == null || templateInfo.isCodeBehindNewer()) {
                        logger.finest("Creating new template from file " + file + "...");
                }

                String fileEncoding = System.getProperty("groovy.source.encoding");

                Reader reader = null;
                try {
                        reader = fileEncoding == null ? new FileReader(file) : new InputStreamReader(new FileInputStream(file), fileEncoding);
                        template = engine.createTemplate(reader);
                } catch (GroovyRuntimeException e) {
                        throw e;
                } catch (Exception e) {
                        throw new RuntimeException("Creation of template failed: " + e, e);
                } finally {
                        if (reader != null) {
                                try {
                                        reader.close();
                                } catch (IOException ignore) {
                                        // e.printStackTrace();
                                }
                        }
                }

                TemplateCacheEntry templateCacheEntry = new TemplateCacheEntry(file, template, false);
                cache.put(key, templateCacheEntry);


                for (File child : templateInfo.getChildren()) {
                        dependencyCache.get(child.toURI().toString()).add(key);
                }


                logger.finest("Created and added template to cache. [key=" + key + "]");


                //
                // Last sanity check.
                //
                if (template == null) {
                        throw new RuntimeException("Template is null? Should not happen here!");
                }

                return template;

        }

//        public void removeFromCache(String template) {
//                //template = StringUtil.capDriveLetter(template);
//                System.out.println("removing from cache, key:" + template);
//                List<String> dependencies = dependencyCache.remove(template);
//                if (dependencies != null) {
//                        for (String key : dependencies) {
////                                log("removing from cache, key:" + key);
////                                System.out.println("removing from cache, key:" + key);
//                                TemplateCacheEntry templateCacheEntry = (TemplateCacheEntry) cache.remove(key);
//                                dependencyCache.remove(key);
//                        }
//                }
//                cache.remove(template);
//
//        }

        public void process(HttpServletResponse response, TemplateInfo templateInfo, Binding binding) throws IOException, FileNotFoundException, TemplateException {

                if (!templateInfo.errorOccurred())
                        templateInfo.setTemplateRequest(true);


                logger.finest("Creating/getting cached template...");

                //
                // Get the template source file handle.
                //
                //File file = super.getScriptUriAsFile(request);
                File file = templateInfo.getRequestFile();
                if (!file.exists()) {
                        throw new FileNotFoundException("file " + file.getAbsolutePath() + " not found");
                }

                //
                // Get the requested template.
                //
                long getMillis = System.currentTimeMillis();

                Template template = getTemplate(file, templateInfo);
                String templateName = ((IncludeTemplateEngine.SimpleTemplate) template).getScript().getClass().getName();
                if (!templateInfo.errorOccurred()) {
                        templateInfo.setUniqueTemplateScriptName(templateName);
                }
                getMillis = System.currentTimeMillis() - getMillis;
                //setVariables(RequestThreadInfo.get().getBinding());


                Writer out = response.getWriter();

                long makeMillis = System.currentTimeMillis();
                try {
                        template.make(binding.getVariables()).writeTo(out);
                } catch (Exception e) {
                        throw new TemplateException(e);
                }
                makeMillis = System.currentTimeMillis() - makeMillis;

                if (response.getContentType() == null)
                        response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + encoding);
                out.flush();

        }
}
