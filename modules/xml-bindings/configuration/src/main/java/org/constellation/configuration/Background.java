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

import java.awt.Color;
import java.awt.Insets;
import java.io.Serializable;
import javax.xml.bind.annotation.*;
import org.constellation.configuration.utils.WMSPortrayalUtils;
import org.geotoolkit.display2d.ext.BackgroundTemplate;
import org.geotoolkit.display2d.ext.DefaultBackgroundTemplate;

/**
 * Background used by decorations and legend template.
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Background implements Serializable {

    @XmlElement(name = "Stroke")
    private Stroke stroke;
    
    @XmlElement(name = "FillColor")
    private String fillColor;
    
    @XmlElement(name = "FillOpacity")
    private Float fillOpacity;
    
    @XmlElement(name = "Inset")
    private Inset inset;
    
    @XmlElement(name = "Round")
    private Float round;

    /**
     * Default background.
     */
    public Background() {
        this.stroke = new Stroke();
        this.fillColor = "#FFFFFF";
        this.fillOpacity = 1.0f;
        this.inset = new Inset();
        this.round = 10.0f;
    }

    public Background(final Stroke stroke, final String fillColor, final Float fillOpacity, final Inset inset, final Float round) {
        this.stroke = stroke;
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
        this.inset = inset;
        this.round = round;
    }

    public Background (final BackgroundTemplate displayBackground) {
        
        final String strokePaint = WMSPortrayalUtils.colorToHex((Color)displayBackground.getBackgroundStrokePaint());
        final Float strokeOpacity = WMSPortrayalUtils.getColorOpacity((Color)displayBackground.getBackgroundStrokePaint());
        this.stroke = new Stroke(displayBackground.getBackgroundStroke(), strokePaint, strokeOpacity);
        this.inset = new Inset(displayBackground.getBackgroundInsets());
        this.fillColor = WMSPortrayalUtils.colorToHex((Color)displayBackground.getBackgroundPaint());
        this.fillOpacity = WMSPortrayalUtils.getColorOpacity((Color)displayBackground.getBackgroundPaint());
        this.round = Integer.valueOf(displayBackground.getRoundBorder()).floatValue();
    }
            
    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public Float getFillOpacity() {
        return fillOpacity;
    }

    public void setFillOpacity(Float fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    public Inset getInset() {
        return inset;
    }

    public void setInset(Inset inset) {
        this.inset = inset;
    }

    public Float getRound() {
        return round;
    }

    public void setRound(Float round) {
        this.round = round;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
    
    /**
     * Convert to displayable background.
     * @return BackgroundTemplate
     */
    public BackgroundTemplate toBackgroundTemplate() {
        
        final java.awt.Stroke awtStroke = stroke.toAwtStroke();
        final Insets awtInset = inset.toAwtInsets();
        final Color strokePaint = WMSPortrayalUtils.parseColor(stroke.getStrokeColor(), stroke.getStrokeOpacity(), Color.DARK_GRAY);
        final Color fillPaint = WMSPortrayalUtils.parseColor(fillColor, fillOpacity, Color.WHITE);
        
        return new DefaultBackgroundTemplate(awtStroke, strokePaint, fillPaint, awtInset, round.intValue());
    }
    
}
