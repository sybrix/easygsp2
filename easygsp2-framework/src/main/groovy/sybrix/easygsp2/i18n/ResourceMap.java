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

package sybrix.easygsp2.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;


public class ResourceMap extends HashMap<String,Object> {
        static final long serialVersionUID = 1L;

        ResourceBundle bundle;

        public ResourceMap(ResourceBundle bundle) {
                this.bundle = bundle;
        }

        @Override
        public Object get(Object o) {
                return bundle.getString(o.toString());
        }

        public Locale getLocale(){
                return bundle.getLocale();
        }

        public Set<String> getKeySet(){
                  return bundle.keySet();
        }

        @Override
        public boolean containsKey(Object o) {
                return bundle.containsKey(o.toString());
        }
}
