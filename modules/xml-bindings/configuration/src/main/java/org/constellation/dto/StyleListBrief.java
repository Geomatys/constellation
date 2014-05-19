/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.dto;

import org.constellation.configuration.StyleBrief;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Garcia (Geomatys)
 * @author Fabien Bernard (Geomatys)
 */
@XmlRootElement
public class StyleListBrief {

    private List<StyleBrief> styles;

    public StyleListBrief() {
        this.styles = new ArrayList<>();
    }

    public StyleListBrief(final List<StyleBrief> styles) {
        this.styles = styles;
    }

    public List<StyleBrief> getStyles() {
        return styles;
    }

    public void setStyles(final List<StyleBrief> styles) {
        this.styles = styles;
    }
}
