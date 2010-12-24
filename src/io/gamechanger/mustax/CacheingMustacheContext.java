// CacheingMustacheContext.java

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
