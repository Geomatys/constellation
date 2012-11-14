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

import java.awt.BasicStroke;
import java.io.Serializable;
import javax.xml.bind.annotation.*;
import org.constellation.configuration.utils.WMSPortrayalUtils;

/**
 * Background used by decorations and legend template.
 *
 * @author Quentin Boileau (Geomatys).
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class Stroke {
    
    @XmlElement(name = "StrokeWidth")
    private Float strokeWidth;
    
    @XmlElement(name = "StrokeDashPattern")
    private String strokeDashPattern;
    
    @XmlElement(name = "StrokeColor")
    private String strokeColor;
    
    @XmlElement(name = "StrokeOpacity")
    private Float strokeOpacity;

    /**
     * Default Stroke.
     */
    public Stroke() {
        this(1.0f, null, "#AAAAAA", 1.0f);
    }

    public Stroke(final Float strokeWidth, final String strokeDashPattern, final String strokeColor, final Float strokeOpacity) {
        this.strokeWidth = strokeWidth;
        this.strokeDashPattern = strokeDashPattern;
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
    }
    
    public Stroke (final java.awt.Stroke awtStroke, final String strokeColor, final Float strokeOpacity) {
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
        this.strokeDashPattern = buildDashPattern(((BasicStroke)awtStroke).getDashArray());
        this.strokeWidth = ((BasicStroke)awtStroke).getLineWidth();
    }

    public Float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getStrokeDashPattern() {
        return strokeDashPattern;
    }

    public void setStrokeDashPattern(String strokeDashPattern) {
        this.strokeDashPattern = strokeDashPattern;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public Float getStrokeOpacity() {
        return strokeOpacity;
    }

    public void setStrokeOpacity(Float strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }
    
    private String buildDashPattern(float[] dashs) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dashs.length; i++) {
            
            sb.append(dashs[i]);
            if (i < dashs.length-1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Convert to AWT displayable Stroke object.
     * @return java.awt.Stroke
     */
    public java.awt.Stroke toAwtStroke() {
        java.awt.Stroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        if(strokeDashPattern != null && !strokeDashPattern.isEmpty()){
            final String[] parts = strokeDashPattern.split(",");
            if (parts.length > 0){
                final float[] dashes = new float[parts.length];
                for (int i=0; i<parts.length; i++){
                    dashes[i] = WMSPortrayalUtils.parseFloat(parts[i], 5);
                }
                stroke =  new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10,dashes,0);
            }
        }
        return stroke;
    }
}
