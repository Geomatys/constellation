/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.geotoolkit.display2d.ext.text.DefaultTextTemplate;
import org.geotoolkit.display2d.ext.text.TextTemplate;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TextDecoration extends PositionableDecoration {
    
    @XmlElement(name="Text")
    private String text;

    public TextDecoration() {
        super();
        this.text = "";
    }

    public TextDecoration(final String text, final Background background, final Integer offsetX, 
            final Integer offsetY, final String position) {
        super(background, offsetX, offsetY, position);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Convert to TextTemplate.
     * @return TextTemplate.
     */
    public TextTemplate toTextTemplate() {
        return new DefaultTextTemplate(getBackground().toBackgroundTemplate(), text);
    }
}
