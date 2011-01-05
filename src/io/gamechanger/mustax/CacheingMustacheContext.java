// CacheingMustacheContext.java
/*
  Copyright 2011 GameChanger Media, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package io.gamechanger.mustax;

import java.util.Map;
import java.util.HashMap;

public abstract class CacheingMustacheContext implements MustacheContext {
    private final Map<String,MustacheTemplate> _cache = new HashMap();

    public final void saveTemplate(final String name, final MustacheTemplate template) {
        _cache.put(name, template);
    }

    public final MustacheTemplate retrieveTemplate(final String name) {
        return _cache.get(name);
    }
}
