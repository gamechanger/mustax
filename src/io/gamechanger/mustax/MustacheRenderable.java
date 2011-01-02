// MustacheRenderable.java

package io.gamechanger.mustax;

import java.util.List;

public interface MustacheRenderable {
    public void renderInContext(List context, StringBuilder buffer);
}
