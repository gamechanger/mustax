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
}
