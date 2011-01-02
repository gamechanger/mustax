// MustacheOperation.java

package io.gamechanger.mustax;

public interface MustacheOperation {
    /**
     * Allows doing things like {{# dostuff }}sometext{{/dostuff}}
     * where 'dostuff' results in a function (one of these) called on the contents.
     */
    public void renderContents( String contents, StringBuilder buffer );
}
