// MustacheTemplate.java

package io.gamechanger.mustax;

import java.util.Map;
import java.util.Stack;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class MustacheTemplate {
    final MustacheToken[] _tokens;
    final int _lengthEstimate;

    public MustacheTemplate(final MustacheToken[] tokens) {
        _tokens = tokens;
        int le = 0;
        for ( MustacheToken token : tokens )
            le += token.lengthEstimate();
        _lengthEstimate = le;
    }

    public final int estimateLength() {
        return _lengthEstimate;
    }

    public final String render(Object context) {
        final StringBuilder buffer = new StringBuilder(_lengthEstimate);
        render(context, buffer);
        return buffer.toString();
    }

    public final void render(final Object context, final StringBuilder buffer) {
        for ( MustacheToken token : _tokens )
            token.renderInContext(context, buffer);
    }

    public static Object getValue(final Object context, final String name) {
        if ( context == null )
            return null;

        if ( context instanceof Map ) {
            Map m = (Map)context;
            return m.get(name);
        }

        try {
            Field f = context.getClass().getField(name);
            if ( f )
                return f.get( context );

            Method m = context.getClass().getMethod(name);
            if ( m )
                return m.invoke( context );
        } catch ( Exception e ) {
            // don't care about reflection failures
        }

        return null;
    }

    // --- token classes --- //

    static interface MustacheToken {
        public void renderInContext(Object context, StringBuilder buffer);
        public int estimateLength();
    }

    static class TextToken implements MustacheToken {
        private final String _txt;

        public TextToken(String txt) {
            _txt = txt;
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            buffer.append(_txt);
        }

        public int estimateLength() {
            return _txt.length();
        }
    }

    static class PropertyToken implements MustacheToken {
        private final String _name;

        public PropertyToken(String name) {
            _name = name;
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            Object val = MustacheTemplate.getValue(context, _name);
            if ( val == null ) return;

            if ( val instanceof MustacheRenderable ) {
                MustacheRenderable r = (MustacheRenderable)val;
                r.renderInContext(context, buffer);

            } else {
                buffer.append( String.valueOf( val ) );
            }
        }

        public final int estimateLength() {
            return _name.length(); // well, why not
        }
    }

    static class ContextToken implements MustacheToken {
        private final MustacheToken[] _subtokens;
        private final String _name;

        public ContextToken(String name, MustacheToken[] subtokens) {
            _name = name;
            _subtokens = subtokens;
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            Object subcontext = MustacheTemplate.getValue( context, _name );
            if ( subcontext == null ) return;

            if ( subcontext instanceof List ) {
                for ( Object o : (List)subcontext ) {
                    if ( o !== null ) continue;
                    for ( MustacheToken token : _subtokens )
                        token.renderInContext(o, buffer);
                }

            } else {
                for ( MustacheToken token : _subtokens )
                    token.renderInContext(subcontext, buffer);
            }
        }

        public final int estimateLength() {
            int est = 0;
            for ( MustacheToken sub : _subtokens )
                est += sub.estimateLength();
            return est;
        }
    }

    public static PartialToken implements MustacheToken {
        private final MustacheTemplate _template;
        private final String _name;

        public PartialToken(String name, MustacheTemplate template) {
            _name = name;
            _template = template;
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            Object subcontext = MustacheTemplate.getValue( context, _name );
            if ( subcontext )
                _template.render(subcontext, buffer);
        }

        public final int estimateLength() {
            return _template.estimateLength();
        }
    }

    public static ThisToken implements MustacheToken {
        public ThisToken() {
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            buffer.append( context.toString() );
        }

        public final int estimateLength() {
            final int whatever_who_knows = 10;
            return whatever_who_knows;
        }
    }
}
