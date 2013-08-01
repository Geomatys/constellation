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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import org.geotoolkit.display2d.ext.BackgroundTemplate;
import org.geotoolkit.display2d.service.PortrayalExtension;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Decorations {
    
    
    private static final Logger LOGGER = Logging.getLogger(Decorations.class);
    
    static final String DECORATION_TYPE = "type";
    static final String DECORATION_VALUE = "value";
    static final String DECORATION_POSITION = "position";
    static final String DECORATION_OFFSET_X = "offsetX";
    static final String DECORATION_OFFSET_Y = "offsetY";
    
    @XmlElements(value={@XmlElement(name="GridDecoration", type=GridDecoration.class),
                        @XmlElement(name="TextDecoration", type=TextDecoration.class),
                        @XmlElement(name="ImageDecoration", type=ImageDecoration.class),
                        @XmlElement(name="ScaleBarDecoration", type=ScaleBarDecoration.class),
                        @XmlElement(name="LegendDecoration", type=LegendDecoration.class),
                        @XmlElement(name="CompasDecoration", type=CompasDecoration.class)})
    private List<AbstractDecoration> decorations = new ArrayList<AbstractDecoration>();

    public Decorations() {
    }

    
    public PortrayalExtension getExtension() {
               
        final DecorationExtension extention = new DecorationExtension();
        
        Map<String, Object> decorationMap;
        for (final AbstractDecoration decoration : decorations) {
            if(decoration != null) {
                decorationMap = new HashMap<String, Object>();

                if (decoration instanceof GridDecoration) {
                    final GridDecoration gridDeco = (GridDecoration) decoration;

                    decorationMap.put(DECORATION_TYPE, GridDecoration.class);
                    decorationMap.put(DECORATION_VALUE, gridDeco.toGridTemplate());

                } else if(decoration instanceof PositionableDecoration) {
                    final PositionableDecoration posDeco = (PositionableDecoration) decoration;
                    final BackgroundTemplate background = posDeco.getBackground().toBackgroundTemplate();
                    decorationMap.put(DECORATION_OFFSET_X, posDeco.getOffsetX());
                    decorationMap.put(DECORATION_OFFSET_Y, posDeco.getOffsetY());
                    decorationMap.put(DECORATION_POSITION, posDeco.swingPosition());

                    if (decoration instanceof TextDecoration) {
                        final TextDecoration textDeco = (TextDecoration) decoration;
                        decorationMap.put(DECORATION_TYPE, TextDecoration.class);
                        decorationMap.put(DECORATION_VALUE, textDeco.toTextTemplate());

                    } else if (decoration instanceof ImageDecoration) {
                        final ImageDecoration imageDeco = (ImageDecoration) decoration;
                        decorationMap.put(DECORATION_TYPE, ImageDecoration.class);
                        decorationMap.put(DECORATION_VALUE, imageDeco.toImageTemplate());

                    } else if (decoration instanceof ScaleBarDecoration) {
                        final ScaleBarDecoration scaleDeco = (ScaleBarDecoration) decoration;
                        decorationMap.put(DECORATION_TYPE, ScaleBarDecoration.class);
                        decorationMap.put(DECORATION_VALUE, scaleDeco.toScaleBarTemplate());

                    } else if (decoration instanceof LegendDecoration) {
                        final LegendDecoration legendDeco = (LegendDecoration) decoration;
                        decorationMap.put(DECORATION_TYPE, LegendDecoration.class);
                        decorationMap.put(DECORATION_VALUE, legendDeco.toLegendTemplate());

                    } else if (decoration instanceof CompasDecoration) {
                        final CompasDecoration compasDeco = (CompasDecoration) decoration;
                        decorationMap.put(DECORATION_TYPE, CompasDecoration.class);
                        decorationMap.put(DECORATION_VALUE, compasDeco.toNorthArrowTemplate());
                    }
                }

                extention.getDecorations().add(decorationMap);
            }
        }
        
        return extention;
    }
    
    
    public List<AbstractDecoration> getDecorations() {
        return decorations;
    }

    public void setDecorations(List<AbstractDecoration> decorations) {
        this.decorations = decorations;
    }
}
