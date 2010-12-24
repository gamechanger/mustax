// MustacheContext.java

package io.gamechanger.mustax;

public interface MustacheContext {
    public MustacheTemplate getTemplate(String name, MustacheParser parser) throws java.io.IOException;
}
