// MustacheParser.java

package io.gamechanger.mustax;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import io.gamechanger.mustax.MustacheTemplate.*;
import java.util.*;

public class MustacheParser {

    public static final void main(String...args) {
        Map<String,String> map = new HashMap();
        map.put("a", "{{a}}");
        map.put("b", "{{> a}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
    }

    private MustacheContext _ctx;

    public MustacheParser(final MustacheContext ctx) {
        _ctx = ctx;
    }

    public final MustacheContext parsingContext() {
        return _ctx;
    }

    public final MustacheTemplate parse(final String input) {
        try {
            return parse(new StringReader(input));
        } catch ( IOException ioe ) {
            // this never happens in this case, so...
            throw new UndeclaredThrowableException( ioe );
        }
    }

    public final MustacheTemplate parse(final Reader input) throws IOException {
        MustacheParserDelegate delegate = new DefaultMustacheParserDelegate();
        parse(delegate, input);
        return new MustacheTemplate( delegate.tokens() );
    }

    private final String getVarName(final StringBuilder buffer, final Reader input) throws IOException {
        while ( input.ready() ) {
            final char c = (char)input.read();
            if ( c == '}' && input.ready() ) {
                final char c2 = (char)input.read();
                if ( c2 == '}' ) {
                    String name = buffer.toString().trim();
                    buffer.setLength(0);
                }

                buffer.append(c);
                buffer.append(c2);
            } else {
                buffer.append(c);
            }
        }
        throw new RuntimeException("Hit end of stream mid-tag : " + buffer);
    }

    private final void parse(final MustacheParserDelegate delegate, final Reader input) throws IOException {
        final StringBuilder buffer = new StringBuilder();

        delegate.start( this );

        while ( input.ready() ) {
            final char c = (char)input.read();

            switch ( c ) {
            case '{':
                final char c2 = (char)input.read();
                if ( c2 != '{' ) {
                    buffer.append(c);
                    buffer.append(c2);
                    continue;
                }

                if ( buffer.length() > 0 ) {
                    delegate.text( this, buffer.toString() );
                    buffer.setLength(0);
                }

                final char ctl = (char)input.read();
                switch ( ctl ) {
                case '#': // context
                    delegate.contextStart( this, getVarName(buffer, input) );
                    break;

                case '>': // partial
                    delegate.partial( this, getVarName(buffer, input) );
                    break;

                case '/': // end tag
                    delegate.contextEnd( this, getVarName(buffer, input) );
                    break;

                default:
                    delegate.variable( this, getVarName(buffer, input) );
                    break;
                }

                break;

            default:
                buffer.append( c );
            }
        }

        if ( buffer.length() > 0 )
            delegate.text( this, buffer.toString() );

        delegate.end( this );
    }

    public static interface MustacheParserDelegate {
        public List<MustacheToken> tokens();

        public void start(MustacheParser parser);
        public void text(MustacheParser parser, String text);
        public void contextStart(MustacheParser parser, String varName);
        public void contextEnd(MustacheParser parser, String varName);
        public void variable(MustacheParser parser, String varName);
        public void partial(MustacheParser parser, String varName);
        public void end(MustacheParser parser);
    }

    static final class DefaultMustacheParserDelegate implements MustacheParserDelegate {
        final List<MustacheToken> _tokens = new ArrayList();
        Stack<ContextToken> _context = new Stack();

        public DefaultMustacheParserDelegate() {
        }

        public final List<MustacheToken> tokens() {
            return _tokens;
        }

        private final void _push(final MustacheToken token) {
            if ( _context.empty() )
                _tokens.add(token);
            else
                _context.push( _context.pop().withAnotherChild(token) );
        }

        public final void start(final MustacheParser parser ) {
        }

        public void text(final MustacheParser parser, final String text) {
            _push( new TextToken( text ) );
        }

        public void contextStart(final MustacheParser parser, final String varName) {
            ContextToken token = new ContextToken(varName, new MustacheToken[0]);
            if ( ! _context.empty() )
                _context.push( _context.pop().withAnotherChild(token) );
            _context.push( token );
        }

        public void contextEnd(final MustacheParser parser, final String varName) {
            ContextToken ctx = null;
            while ( ! _context.empty() ) {
                ctx = _context.pop();
                if ( ctx.name().equals( varName ) )
                    break;
            }

            if ( _context.empty() && ctx != null )
                _tokens.add( ctx );
        }

        public void variable(final MustacheParser parser, final String varName) {
            _push( varName.equals( "." ) ? ThisToken.THIS_TOKEN : new PropertyToken( varName ) );
        }

        public void partial(final MustacheParser parser, final String varName) {
            MustacheContext parsingContext = parser.parsingContext();
            try {
                _push( new PartialToken( parser.parsingContext().getTemplate(varName, parser) ) );
                //partialTemplate = parser.parse( parsingContext.getTemplateSource( varName ) );
            } catch ( IOException e ) {
                throw new UndeclaredThrowableException(e);
            }
        }

        public final void end(final MustacheParser parser) {
            MustacheToken token = null;
            while ( ! _context.empty() )
                token = _context.pop();
            if ( token != null )
                _tokens.add( token );
        }
    }
}
