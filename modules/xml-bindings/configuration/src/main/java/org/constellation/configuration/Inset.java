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
package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.awt.*;
import java.io.Serializable;
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
