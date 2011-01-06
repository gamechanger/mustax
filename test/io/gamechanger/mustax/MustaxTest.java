// MustaxTest.java

package io.gamechanger.mustax;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

public class MustaxTest {
    @Test public void testParserFunctions() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "{{z}}");
        map.put("b", "here is {{#x}}{{> a}}{{/x}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "b" );
        assertNotNull(template);
    }

    @Test public void testMapVariableInterpolation() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "{{z}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Map<String,String> context = new HashMap();
        context.put("z", "zebra");
        String res = template.render(context);
        assertEquals("zebra", res);
    }

    @Test public void testFieldVariableInterpolation() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "{{z}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Object o = new Object() {
                String z = "zero";
            };
        assertEquals("zero", template.render(o));
    }

    @Test public void testBasicContextPush() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "hi {{#person}}{{name}}{{/person}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Map<String,Object> context = new HashMap();
        context.put("z", "zebra");
        Map<String,String> person = new HashMap();
        person.put("name", "Kiril");
        context.put("person", person);
        assertEquals( "hi Kiril", template.render(context));
    }

    @Test public void testInvertedSection() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "hi {{^person}}Nobody{{/person}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Map<String,Object> context = new HashMap();
        context.put("z", "zebra");
        Map<String,String> person = new HashMap();
        person.put("name", "Kiril");
        context.put("person", person);

        assertEquals("hi ", template.render(context));
        assertEquals("hi Nobody", template.render(new Object()));
    }

    @Test public void testListContext() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "{{#person}}hi {{name}}, {{/person}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Map<String,Object> context = new HashMap();
        context.put("z", "zebra");
        Map<String,String> m = new HashMap();
        m.put("name", "Kiril");
        List people = new ArrayList();
        people.add(m);
        m = new HashMap();
        m.put("name", "Ted");
        people.add(m);

        context.put("person", people);
        assertEquals( "hi Kiril, hi Ted, ", template.render(context));
    }

    @Test public void testNestedButFlatContext() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "{{#person}}{{#frog}}hi {{name}}, {{/frog}}{{/person}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Map<String,Object> context = new HashMap();
        context.put("z", "zebra");
        Map<String,Object> m = new HashMap();
        m.put("name", "Kiril");
        m.put("frog", Boolean.TRUE);
        List people = new ArrayList();
        people.add(m);
        m = new HashMap();
        m.put("name", "Ted");
        people.add(m);

        context.put("person", people);
        assertEquals( "hi Kiril, ", template.render(context));
    }

    @Test public void testContextTraversal() throws java.io.IOException {
        Map<String,String> map = new HashMap();
        map.put("a", "{{#person}}{{#frog}}hi {{name}}, {{/frog}}{{/person}}");
        MustacheContext ctx = new MapMustacheContext(map);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = parser.templateByName( "a" );
        assertNotNull(template);

        Map<String,Object> context = new HashMap();
        context.put("z", "zebra");
        Map<String,Object> m = new HashMap();
        m.put("name", "Kiril");

        Map frog = new HashMap();
        frog.put("x", "y");
        m.put("frog", frog);

        List people = new ArrayList();
        people.add(m);
        m = new HashMap();
        m.put("name", "Ted");
        people.add(m);

        context.put("person", people);
        assertEquals( "hi Kiril, ", template.render(context));
    }

    @Test public void testBasicPartials() throws java.io.IOException {
        Map m = new HashMap();
        m.put("A", "hello {{#thing}}{{> B}}{{/thing}}");
        m.put("B", "{{x}}");
        MustacheContext ctx = new MapMustacheContext(m);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = ctx.getTemplate( "A", parser );

        Map view = new HashMap();
        Map thing = new HashMap();
        thing.put("x", "world");
        view.put("thing", thing);

        assertEquals( "hello world", template.render(view) );
    }

    @Test public void testBooleanSection() throws java.io.IOException {
        Map m = new HashMap();
        m.put( "X", "{{#a}}A{{/a}{{#b}}B{{/b}}{{^c}}!C{{/c}}" );
        MustacheContext ctx = new MapMustacheContext(m);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = ctx.getTemplate( "X", parser );

        Map view = new HashMap();
        view.put( "a", true );
        view.put( "b", true );
        view.put( "c", true );
        assertEquals( "AB", template.render( view ) );
        view.put( "c", false );
        assertEquals( "AB!C", template.render( view ) );
        view.put( "a", false );
        assertEquals( "B!C", template.render( view ) );
    }

    @Test public void testIntegerSection() throws java.io.IOException {
        Map m = new HashMap();
        m.put( "X", "{{#a}}A{{/a}{{#b}}B{{/b}}{{^c}}!C{{/c}}" );
        MustacheContext ctx = new MapMustacheContext(m);
        MustacheParser parser = new MustacheParser(ctx);
        MustacheTemplate template = ctx.getTemplate( "X", parser );

        Map view = new HashMap();
        view.put( "a", 777 );
        view.put( "b", 3 );
        view.put( "c", 1 );
        assertEquals( "AB", template.render( view ) );
        view.put( "c", 0 );
        assertEquals( "AB!C", template.render( view ) );
        view.put( "a", 0 );
        assertEquals( "B!C", template.render( view ) );
        view.put( "a", -1 );
        assertEquals( "AB!C", template.render( view ) );
    }
}
