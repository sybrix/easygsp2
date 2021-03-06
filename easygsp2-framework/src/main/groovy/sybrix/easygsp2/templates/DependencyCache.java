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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DependencyCache <br/>
 *
 * @author David Lee
 */
public class DependencyCache extends HashMap {
        @Override
        public Object get(Object key) {
                if (!containsKey(key)){
                        put(key, new ArrayList());
                }

                return super.get(key);
        }
}
