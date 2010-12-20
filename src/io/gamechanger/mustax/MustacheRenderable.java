// MustacheRenderable.java

package io.gamechanger.mustax;

import java.util.Map;

public interface MustacheRenderable {
    public void renderInContext(Map context, StringBuilder buffer);
}
