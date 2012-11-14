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

import java.awt.Insets;
import java.io.Serializable;
import javax.xml.bind.annotation.*;
/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Inset implements Serializable {
    
    @XmlAttribute(name="top")
    private Integer top;
    
    @XmlAttribute(name="left")
    private Integer left;
    
    @XmlAttribute(name="bottom")
    private Integer bottom;
    
    @XmlAttribute(name="right")
    private Integer right;

    public Inset() {
        this(5, 5, 5, 5);
    }

    public Inset(Integer top, Integer left, Integer bottom, Integer right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }
    
    public Inset(final Insets awtInset) {
        this.top = awtInset.top;
        this.right = awtInset.right;
        this.left = awtInset.left;
        this.bottom = awtInset.bottom;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getBottom() {
        return bottom;
    }

    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }

    public Integer getRight() {
        return right;
    }

    public void setRight(Integer right) {
        this.right = right;
    }
    
    /**
     * Convert to a displayable AWT Insets.
     * @return Insets
     */
    public Insets toAwtInsets() {
        return new Insets(top, left, bottom, right);
    }
}
