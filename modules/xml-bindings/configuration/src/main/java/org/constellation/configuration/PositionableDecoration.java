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

import java.io.Serializable;
import javax.swing.SwingConstants;
import javax.xml.bind.annotation.*;

/**
 * WMS Portrayal legend template configuration.
 * 
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class PositionableDecoration extends AbstractDecoration implements Serializable {
    
    public static final String POSITION_CENTER = "center";
    public static final String POSITION_NORTH = "north";
    public static final String POSITION_EAST = "east";
    public static final String POSITION_SOUTH = "south";
    public static final String POSITION_WEST = "west";
    public static final String POSITION_NORTH_EAST = "north-east";
    public static final String POSITION_NORTH_WEST = "north-west";
    public static final String POSITION_SOUTH_EAST = "south-east";
    public static final String POSITION_SOUTH_WEST = "south-west";
    
    @XmlElement(name="Background")
    private Background background;
    
    @XmlElement(name="OffsetX")
    private Integer offsetX;
    
    @XmlElement(name="OffsetY")
    private Integer offsetY;
    
    @XmlElement(name="Position")
    private String position;

    public PositionableDecoration() {
        this(new Background(), 0, 0, POSITION_CENTER);
    }

    public PositionableDecoration(Background background, Integer offsetX, Integer offsetY, String position) {
        super();
        this.background = background;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.position = position;
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public Integer getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(Integer offsetX) {
        this.offsetX = offsetX;
    }

    public Integer getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(Integer offsetY) {
        this.offsetY = offsetY;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
    
    /**
     * Return the <code>SwingConstant</code> corresponding to the decoration position.
     * If decoration position is not defined, <code>SwingConstants.CENTER</code> will be returned.
     * @return 
     */
    public int swingPosition() {
        if (PositionableDecoration.POSITION_CENTER.equalsIgnoreCase(position)) {
            return SwingConstants.CENTER;
        } else if (PositionableDecoration.POSITION_EAST.equalsIgnoreCase(position)) {
            return SwingConstants.EAST;
        } else if (PositionableDecoration.POSITION_WEST.equalsIgnoreCase(position)) {
            return SwingConstants.WEST;
        } else if (PositionableDecoration.POSITION_NORTH.equalsIgnoreCase(position)) {
            return SwingConstants.NORTH;
        } else if (PositionableDecoration.POSITION_NORTH_EAST.equalsIgnoreCase(position)) {
            return SwingConstants.NORTH_EAST;
        } else if (PositionableDecoration.POSITION_NORTH_WEST.equalsIgnoreCase(position)) {
            return SwingConstants.NORTH_WEST;
        } else if (PositionableDecoration.POSITION_SOUTH.equalsIgnoreCase(position)) {
            return SwingConstants.SOUTH;
        } else if (PositionableDecoration.POSITION_SOUTH_EAST.equalsIgnoreCase(position)) {
            return SwingConstants.SOUTH_EAST;
        } else if (PositionableDecoration.POSITION_SOUTH_WEST.equalsIgnoreCase(position)) {
            return SwingConstants.SOUTH_WEST;
        } else {
            return SwingConstants.CENTER;
        }
    }
}
