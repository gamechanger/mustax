// MapMustacheContext.java

package io.gamechanger.mustax;

import java.util.Map;

/**
 * uses a map of {template_name->template_string}
 */
public class MapMustacheContext extends CacheingMustacheContext implements MustacheContext {
    private final Map<String,String> _map;

    public MapMustacheContext(Map<String,String> map) {
        _map = map;
    }

    public MustacheTemplate getTemplate(final String name, final MustacheParser parser) {
        MustacheTemplate template = retrieveTemplate( name );
        if ( template != null )
            return template;
        template = parser.parse( _map.get( name ) );
        saveTemplate( name, template );
        return template;
    }
}
