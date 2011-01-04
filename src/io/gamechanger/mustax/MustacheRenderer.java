// MustacheRenderer.java

package io.gamechanger.mustax;

import java.util.List;

public class MustacheRenderer {
    final MustacheParser _parser;

    public MustacheRenderer( MustacheParser parser ) {
        _parser = parser;
    }

    public void render( final String templateText, final List context, final StringBuilder buffer ) {
        MustacheTemplate template = _parser.parse( templateText );
        template.renderInContext( context, buffer );
    }
}
