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

import org.constellation.configuration.utils.WMSPortrayalUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.awt.*;

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
