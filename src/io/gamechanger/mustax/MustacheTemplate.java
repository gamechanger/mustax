// MustacheTemplate.java
/*
  Copyright 2011 GameChanger Media, Inc.
  Copyright 2011 Kiril Savino

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

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class MustacheTemplate {
    final MustacheToken[] _tokens;
    final int _lengthEstimate;
    final MustacheContext _context;

    private static MustacheToken[] arr(final List<MustacheToken> tokens) {
        final MustacheToken[] arr = new MustacheToken[tokens.size()];
        tokens.toArray(arr);
        return arr;
    }

    public MustacheTemplate(final MustacheContext context, final List<MustacheToken> tokens) {
        this(context, arr(tokens));
    }

    public MustacheTemplate(final MustacheContext context, final MustacheToken[] tokens) {
        _context = context;
        _tokens = tokens;
        int le = 0;
        for ( MustacheToken token : tokens )
            le += token.estimateLength();
        _lengthEstimate = le;
    }

    public final String toString() {
        final StringBuilder sb = new StringBuilder( "<MustacheTemplate: " );
        int len = sb.length();
        for ( MustacheToken t : _tokens ) {
            if ( sb.length() > len )
                sb.append( ", " );
            sb.append( t.toString() );
        }
        sb.append( " >" );
        return sb.toString();
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
        LinkedList contextStack = new LinkedList();
        contextStack.add( context );
        renderInContext( contextStack, buffer );
    }

    public final void renderInContext(final List context, final StringBuilder buffer) {
        for ( MustacheToken token : _tokens )
            token.renderInContext(context, buffer);
    }

    public static Object getValue(final List context, final String name) {

        for ( Object object : context ) {

            if ( object == null )
                continue;

            if ( object instanceof Map ) {
                Map m = (Map)object;
                Object v = m.get(name);
                if ( v != null )
                    return v;
                else
                    continue;
            }

            try {
                Field f = object.getClass().getDeclaredField(name);
                if ( f != null ) {
                    Object v = f.get( object );
                    if ( v != null )
                        return v;
                    continue;
                }
            } catch ( NoSuchFieldException nsfe ) {
            } catch ( SecurityException e ) {
            } catch ( IllegalAccessException iae ) {
            }

            try {
                Method m = object.getClass().getMethod(name);
                if ( m != null ) {
                    Object v = m.invoke( object );
                    if ( v != null )
                        return v;
                    continue;
                }
            } catch ( NoSuchMethodException nsfe ) {
            } catch ( SecurityException e ) {
            } catch ( IllegalAccessException iae ) {
            } catch ( java.lang.reflect.InvocationTargetException ite ) {
                throw new java.lang.reflect.UndeclaredThrowableException( ite );
            }
        }

        return null;
    }

    // --- token classes --- //

    static interface MustacheToken {
        public void renderInContext(List context, StringBuilder buffer);
        public int estimateLength();
        public String toRepresentation();
    }

    static class TextToken implements MustacheToken {
        private final String _txt;

        public TextToken(String txt) {
            _txt = txt;
        }

        public final void renderInContext(final List context, final StringBuilder buffer) {
            buffer.append(_txt);
        }

        public int estimateLength() {
            return _txt.length();
        }

        public String toString() {
            return "<TextToken: " + _txt + ">";
        }

        public String toRepresentation() {
            return _txt;
        }
    }

    static class PropertyToken implements MustacheToken {
        private final String _name;

        public PropertyToken(String name) {
            _name = name;
        }

        public final void renderInContext(final List context, final StringBuilder buffer) {
            Object val = MustacheTemplate.getValue(context, _name);
            if ( val instanceof MustacheFunction )
                val = ((MustacheFunction)val).invoke( context );

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

        public String toString() {
            return "<PropertyToken: " + _name + ">";
        }

        public String toRepresentation() {
            return "{{" + _name + "}}";
        }
    }

    static class SectionToken implements MustacheToken {
        protected MustacheToken[] _subtokens;
        private final String _name;
        private final boolean _reversed;
        private final MustacheContext _context;

        public SectionToken(String name, MustacheToken[] subtokens, MustacheContext context, boolean reversed) {
            _subtokens = subtokens;
            _name = name;
            _subtokens = subtokens;
            _context = context;
            _reversed = reversed;
        }

        public final String name() {
            return _name;
        }

        public final String toRepresentation() {
            final StringBuilder b = new StringBuilder();
            b.append( "{{" + ( _reversed ? "^" : "#") + _name + "}}" );
            for ( MustacheToken t : _subtokens )
                b.append( t.toRepresentation() );
            b.append( "{{/" + _name + "}}" );
            return b.toString();
        }

        private final void _renderSubTokens(final List context, final StringBuilder buffer) {
            for ( MustacheToken t : _subtokens )
                t.renderInContext(context, buffer);
        }

        public final void renderInContext(final List context, final StringBuilder buffer) {
            Object subcontext = MustacheTemplate.getValue( context, _name );

            if ( subcontext instanceof MustacheFunction )
                subcontext = ((MustacheFunction)subcontext).invoke( context );

            if ( subcontext == null ) {
                if ( _reversed )
                    _renderSubTokens(context, buffer);
                return;
            } else if ( _reversed )
                return;

            boolean truthiness = ! _reversed;
            if ( subcontext instanceof Boolean ) {
                if ( ((Boolean)subcontext).booleanValue() == truthiness )
                    _renderSubTokens(context, buffer);
                return;

            } else if ( subcontext instanceof Number ) {
                Number n = (Number)subcontext;
                if ( ( n.intValue() != 0 ) == truthiness )
                    _renderSubTokens(context, buffer);
                return;
            }

            if (subcontext instanceof List) {
                for ( Object o : (List)subcontext ) {
                    if ( o == null ) continue;
                    context.add( 0, o );
                    _renderSubTokens(context, buffer);
                    context.remove( 0 );
                }

            } else if ( subcontext instanceof MustacheFilter ) {
                MustacheFilter filter = (MustacheFilter)subcontext;

                final StringBuilder rawBuffer = new StringBuilder();
                for ( MustacheToken t : _subtokens )
                    rawBuffer.append( t.toRepresentation() );

                filter.renderContents( context, rawBuffer.toString(), new MustacheRenderer( new MustacheParser( _context ) ), buffer );

            } else {
                context.add( 0, subcontext );
                _renderSubTokens(context, buffer);
                context.remove( 0 );
            }
        }

        public final int estimateLength() {
            int est = 0;
            for ( MustacheToken sub : _subtokens )
                est += sub.estimateLength();
            return est;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            for ( MustacheToken t : _subtokens ) {
                if ( sb.length() > 1 )
                    sb.append( ", " );
                sb.append(t.toString());
            }
            sb.append("]");
            return "<SectionToken: " + _name + ":" + sb + ">";
        }

        public final void addChild(final MustacheToken child) {
            MustacheToken[] tokens = new MustacheToken[_subtokens.length+1];
            System.arraycopy(_subtokens, 0, tokens, 0, _subtokens.length);
            tokens[tokens.length-1] = child;
            _subtokens = tokens;
        }
    }

    public static class PartialToken implements MustacheToken {
        private final String _name;
        private final MustacheTemplate _template;

        public PartialToken(String name, MustacheTemplate template) {
            _name = name;
            _template = template;
        }

        public final String toRepresentation() {
            return "{{> " + _name + "}}";
        }

        public final void renderInContext(final List context, final StringBuilder buffer) {
            _template.renderInContext(context, buffer);
        }

        public final int estimateLength() {
            return _template.estimateLength();
        }
    }

    public static class ThisToken implements MustacheToken {
        public static final ThisToken THIS_TOKEN = new ThisToken();

        public ThisToken() {
        }

        public final String toRepresentation() {
            return "{{.}}";
        }

        public final void renderInContext(final List context, final StringBuilder buffer) {
            Object top = context.get(0);
            if ( top instanceof MustacheRenderable )
                ((MustacheRenderable)top).renderInContext(context, buffer);
            else
                buffer.append( String.valueOf( top ) );
        }

        public final int estimateLength() {
            final int whatever_who_knows = 10;
            return whatever_who_knows;
        }
    }
}
