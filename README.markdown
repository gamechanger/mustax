A fully dependency-free Java implementation of the Mustache templating language.
   http://mustache.github.com/

Goals:

* Android compatible (no runtime compilation, etc.)
* Lightweight
* Efficient
* Supports all Mustache features
  (partials, function sections, context traversal, etc.)
* Stay within the standard as much as possible

So as usual, standing on shoulders, etc.

    import io.gamechanger.mustax.*;
    import java.util.*;

    public class Foo {
        public static void main( String... args ) {
            // you must give a parser a context; basically a way to look up partials.
            Map map = new HashMap();
            m.put( "x", "this is template x, {{content}}" );
            m.put( "y", "this is template y, containing some {{#foo}}{{>x}}{{/foo}}" );
            MustacheContext context = new MapMustacheContext( map );

            MustacheParser parser = new MustacheParser( context );
            // The context provides convenient ways to get at the templates defined within it
            MustacheTemplate template_y = context.getTemplate( "y", parser );
            // We can also just use this parser on any-old-text, and it'll use its context to look up partials
            MustacheTemplate template_z = context.parse( "This is template z, {{>x}}" );

            Map view = new HashMap(); // traverses maps, naturally
            Fooble fooble = new Fooble();
            fooble.content = "yay wow"; // and also public members of POJOs
            view.put( "foo", fooble );

            System.out.println( template_y.render( view ) );
            // should print 'this is template y, containing some this is template x, yay wow'
        }

        static class Fooble {
            public String content;
        }
    }

