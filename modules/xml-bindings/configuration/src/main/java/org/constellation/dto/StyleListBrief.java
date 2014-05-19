/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
