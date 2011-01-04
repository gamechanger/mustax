// MustacheParser.java

package io.gamechanger.mustax;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import io.gamechanger.mustax.MustacheTemplate.*;
import java.util.*;

public class MustacheParser {

    public static final void main(String...args) throws Exception {
        Map<String,String> map = new HashMap();
        map.put("a", "{{a}}");
        map.put("b", "here is {{#x}}{{> a}}{{/x}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        System.out.println("template b : " + parser.templateByName("b"));
    }

    private MustacheContext _ctx;

    public MustacheParser(final MustacheContext ctx) {
        _ctx = ctx;
    }

    public final MustacheContext parsingContext() {
        return _ctx;
    }

    public final MustacheTemplate templateByName(final String name) throws IOException {
        return _ctx.getTemplate(name, this);
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
            int i = input.read();
            if ( i == -1 )
                break;
            final char c = (char)i;//input.read();
            if ( c == '}' && ( i = input.read() ) != -1 ) {
                final char c2 = (char)i;
                if ( c2 == '}' ) {
                    String name = buffer.toString().trim();
                    buffer.setLength(0);
                    return name;
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

        int i = -1;
        while ( input.ready() && ( i = input.read() ) != -1 ) {
            final char c = (char)i;

            switch ( c ) {
            case '{':
                i = input.ready() ? input.read() : -1;
                final char c2 = (char)i;
                if ( i == -1 || c2 != '{' ) {
                    buffer.append(c);
                    if ( i > -1 )
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
                    delegate.contextStart( this, getVarName(buffer, input), false );
                    break;

                case '^': // reversed context
                    delegate.contextStart( this, getVarName(buffer, input), true );
                    break;

                case '>': // partial
                    delegate.partial( this, getVarName(buffer, input) );
                    break;

                case '/': // end tag
                    delegate.contextEnd( this, getVarName(buffer, input) );
                    break;

                default:
                    buffer.append(ctl);
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
        public void contextStart(MustacheParser parser, String varName, boolean reversed);
        public void contextEnd(MustacheParser parser, String varName);
        public void variable(MustacheParser parser, String varName);
        public void partial(MustacheParser parser, String varName);
        public void end(MustacheParser parser);
    }

    static final class DefaultMustacheParserDelegate implements MustacheParserDelegate {
        final List<MustacheToken> _tokens = new ArrayList();
        Stack<SectionToken> _context = new Stack();

        public DefaultMustacheParserDelegate() {
        }

        public final List<MustacheToken> tokens() {
            return _tokens;
        }

        private final void _push(final MustacheToken token) {
            if ( _context.empty() )
                _tokens.add(token);
            else
                _context.peek().addChild( token );
        }

        public final void start(final MustacheParser parser ) {
        }

        public void text(final MustacheParser parser, final String text) {
            _push( new TextToken( text ) );
        }

        public void contextStart(final MustacheParser parser, final String varName, final boolean reversed) {
            SectionToken token = new SectionToken(varName, new MustacheToken[0], reversed);
            if ( ! _context.empty() )
                _context.peek().addChild( token );
            _context.push( token );
        }

        public void contextEnd(final MustacheParser parser, final String varName) {
            SectionToken ctx = null;
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
                _push( new PartialToken( varName, parser.parsingContext().getTemplate(varName, parser) ) );
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
