// MustacheRenderer.java

package io.gamechanger.mustax;

public interface MustacheRenderer {
    public void renderInContext(java.util.List context, StringBuilder buffer);
}
