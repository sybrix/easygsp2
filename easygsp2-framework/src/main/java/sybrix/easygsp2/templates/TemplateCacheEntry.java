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

import groovy.text.Template;

import java.io.File;
import java.util.Date;

/**
 * TemplateCacheEntry <br/>
 *
 */

/**
         * Simple cache entry that validates against last modified and length
         * attributes of the specified file.
         *
         * @author Christian Stein
         */
        public  class TemplateCacheEntry {

                Date date;
                long hit;
                long lastModified;
                long length;
                Template template;
                //private Set children;


                public TemplateCacheEntry(File file, Template template) {
                        this(file, template, false); // don't get time millis for sake of speed
                }

                public TemplateCacheEntry(File file, Template template, boolean timestamp) {
                        if (file == null) {
                                throw new NullPointerException("file");
                        }
                        if (template == null) {
                                throw new NullPointerException("template");
                        }
                        if (timestamp) {
                                this.date = new Date(System.currentTimeMillis());
                        } else {
                                this.date = null;
                        }
                        this.hit = 0;
                        this.lastModified = file.lastModified();
                        this.length = file.length();
                        this.template = template;
                        //this.children = new HashSet();
                }

//                public Set getChildren() {
//                        return children;
//                }
//                public void setChildren(Set children) {
//                        this.children = children;
//                }

                /**
                 * Checks the passed file attributes against those cached ones.
                 *
                 * @param file Other file handle to compare to the cached values.
                 * @return <code>true</code> if all measured values match, else <code>false</code>
                 */
                public boolean validate(File file) {
                        if (file == null) {
                                throw new NullPointerException("file");
                        }
                        if (file.lastModified() != this.lastModified) {
                                return false;
                        }
                        if (file.length() != this.length) {
                                return false;
                        }
                        hit++;
                        return true;
                }

                public String toString() {
                        if (date == null) {
                                return "Hit #" + hit;
                        }
                        return "Hit #" + hit + " since " + date;
                }

        }
