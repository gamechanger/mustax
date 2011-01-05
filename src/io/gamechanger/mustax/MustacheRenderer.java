// MustacheRenderer.java
/*
  Copyright 2011 GameChanger Media, Inc.
  Copyright 2011 Kiril Savino

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package io.gamechanger.mustax;

import java.util.List;

public class MustacheRenderer {
    final MustacheParser _parser;

    public MustacheRenderer( MustacheParser parser ) {
        _parser = parser;
    }

    public void render( final String templateText, final List context, final StringBuilder buffer ) {
        MustacheTemplate template = _parser.parse( templateText );
        template.renderInContext( context, buffer );
    }
}
