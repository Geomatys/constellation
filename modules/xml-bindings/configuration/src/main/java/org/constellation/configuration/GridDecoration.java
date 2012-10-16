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
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.constellation.configuration.utils.WMSPortrayalUtils;
import org.geotoolkit.display2d.ext.grid.DefaultGridTemplate;
import org.geotoolkit.display2d.ext.grid.GridTemplate;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.logging.Logging;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GridDecoration extends AbstractDecoration {

    private static final Logger LOGGER = Logging.getLogger(GridDecoration.class);
    @XmlElement(name = "MainGrid")
    private Grid mainGrid;
    @XmlElement(name = "SecondGrid")
    private Grid secondGrid;
    @XmlElement(name = "CRS")
    private String crsStr;

    public GridDecoration() {
        this(new Grid(), new Grid(new Stroke(1.0f, null, "#C0C0C0", 1.0f), "serial-BOLD-14"), null);
    }

    public GridDecoration(Grid mainGrid, Grid secondGrid, String crs) {
        this.mainGrid = mainGrid;
        this.secondGrid = secondGrid;
        this.crsStr = crs;
    }

    public Grid getMainGrid() {
        return mainGrid;
    }

    public void setMainGrid(Grid mainGrid) {
        this.mainGrid = mainGrid;
    }

    public Grid getSecondGrid() {
        return secondGrid;
    }

    public void setSecondGrid(Grid secondGrid) {
        this.secondGrid = secondGrid;
    }

    public String getCrsStr() {
        return crsStr;
    }

    public void setCrsStr(String crs) {
        this.crsStr = crs;
    }

    /**
     * Convert to geotoolkit GridTemplate.
     * @return GridTemplate
     */
    public GridTemplate toGridTemplate() {
        CoordinateReferenceSystem crs = null;
        if (crsStr != null) {
            try {
                crs = CRS.decode(crsStr);
            } catch (FactoryException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        final java.awt.Stroke mainLineStroke = mainGrid.getStroke() != null ? mainGrid.getStroke().toAwtStroke() : new BasicStroke(2);
        final Paint mainLinePaint = WMSPortrayalUtils.parseColor(mainGrid.getStroke().getStrokeColor(), mainGrid.getStroke().getStrokeOpacity(), Color.DARK_GRAY);
        final Font mainLineFont = mainGrid.getFont() != null ? Font.decode(mainGrid.getFont()) : new Font("serial", Font.BOLD, 14);
        
        final java.awt.Stroke secondLineStroke = secondGrid.getStroke() != null ? secondGrid.getStroke().toAwtStroke()
                : new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0);
        final Paint secondLinePaint = WMSPortrayalUtils.parseColor(secondGrid.getStroke().getStrokeColor(), secondGrid.getStroke().getStrokeOpacity(), Color.GRAY);
        final Font secondLineFont = secondGrid.getFont() != null ? Font.decode(secondGrid.getFont()) : new Font("serial", Font.BOLD, 14);

        return new DefaultGridTemplate(
                crs,
                mainLineStroke,
                mainLinePaint,
                secondLineStroke,
                secondLinePaint,
                mainLineFont,
                mainLinePaint,
                0,
                new Color(0f, 0f, 0f, 0f),
                secondLineFont,
                secondLinePaint,
                0,
                new Color(0f, 0f, 0f, 0f));
    }
}
