// MustacheTemplate.java

package io.gamechanger.mustax;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class MustacheTemplate {
    final MustacheToken[] _tokens;
    final int _lengthEstimate;

    private static MustacheToken[] arr(final List<MustacheToken> tokens) {
        final MustacheToken[] arr = new MustacheToken[tokens.size()];
        tokens.toArray(arr);
        return arr;
    }

    public MustacheTemplate(final List<MustacheToken> tokens) {
        this(arr(tokens));
    }

    public MustacheTemplate(final MustacheToken[] tokens) {
        _tokens = tokens;
        int le = 0;
        for ( MustacheToken token : tokens )
            le += token.estimateLength();
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
            if ( f != null )
                return f.get( context );

            Method m = context.getClass().getMethod(name);
            if ( m != null )
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

        public final String name() {
            return _name;
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            Object subcontext = MustacheTemplate.getValue( context, _name );
            if ( subcontext == null ) return;

            if ( subcontext instanceof List ) {
                for ( Object o : (List)subcontext ) {
                    if ( o != null ) continue;
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

        public final ContextToken withAnotherChild(final MustacheToken child) {
            MustacheToken[] tokens = new MustacheToken[_subtokens.length+1];
            System.arraycopy(_subtokens, 0, tokens, 0, _subtokens.length);
            tokens[tokens.length-1] = child;
            return new ContextToken(_name, tokens);
        }
    }

    public static class PartialToken implements MustacheToken {
        private final MustacheTemplate _template;

        public PartialToken(MustacheTemplate template) {
            _template = template;
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            _template.render(context, buffer);
        }

        public final int estimateLength() {
            return _template.estimateLength();
        }
    }

    public static class ThisToken implements MustacheToken {
        public static final ThisToken THIS_TOKEN = new ThisToken();

        public ThisToken() {
        }

        public final void renderInContext(final Object context, final StringBuilder buffer) {
            if ( context instanceof MustacheRenderable )
                ((MustacheRenderable)context).renderInContext(context, buffer);
            else
                buffer.append( String.valueOf( context ) );
        }

        public final int estimateLength() {
            final int whatever_who_knows = 10;
            return whatever_who_knows;
        }
    }
}
