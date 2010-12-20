// MustacheParser.java

package io.gamechanger.mustax;

import java.io.Reader;
import java.io.IOException;
import MustacheTemplate.MustacheToken;

public class MustacheParser {

    static enum MXParserState {
        MXParserStateDefault,
            MXParserState
            MXParserStateTag,
            MXParserStateTag
    }

    private MustacheContext _ctx;
    private MXParserState _state;

    public MustacheParser(final MustacheContext ctx) {
        _ctx = ctx;
    }

    public final MustacheTemplate parse(final String input) {
        try {
            return parse(new StringReader(input));
        } catch ( IOException ioe ) {
            // this never happens in this case, so...
        }
    }

    public final MustacheTemplate parse(final Reader input) throws IOException {
        List<MustacheToken> tokens = new ArrayList();

        MustacheToken token = parseNextToken(input);
        while ( token ) {
            tokens.add( token );
            token = parseNextToken( input );
        }

        return MustacheTemplate( tokens );
    }

    private final MustacheToken parseNextToken(final Reader input) throws IOException {
        while ( input.ready() ) {
            c = input.read();

            switch ( _state ) {
            switch ( c ) {
            case '{':
            case '}':
            }
        }
    }
}
