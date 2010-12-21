// MustacheRenderable.java

package io.gamechanger.mustax;

public interface MustacheRenderable {
    public void renderInContext(Object context, StringBuilder buffer);
}
