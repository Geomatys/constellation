/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class CoverageDataDescription implements DataDescription {

    private double[] boundingBox;
    private List<BandDescription> bands;

    public CoverageDataDescription() {
        this.boundingBox = new double[]{-180,-90,180,90};
        bands = new ArrayList<BandDescription>(0);
    }

    public List<BandDescription> getBands() {
        return bands;
    }

    public void setBands(final List<BandDescription> bands) {
        this.bands = bands;
    }

    @Override
    public double[] getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void setBoundingBox(final double[] boundingBox) {
        this.boundingBox = boundingBox;
    }
}
