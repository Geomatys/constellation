/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.tile.visitor;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.Unit;

import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;


/**
 * Visit results of a GetFeatureInfo request, and format the output into CSV.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class CSVGraphicVisitor extends TextGraphicVisitor {

    private int index = 0;

    public CSVGraphicVisitor(final GetFeatureInfo gfi) {
        super(gfi);

        final String key = gfi.getGetTile().getLayer();
        values.put(key, new ArrayList<String>());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic, Shape queryArea) {
        super.visit(graphic, queryArea);
        index++;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedCoverage coverage, Shape queryArea) {
        index++;
        final Object[][] results = getCoverageValues(coverage, queryArea);

        if (results == null) {
            return;
        }

        final String layerName = coverage.getCoverageLayer().getCoverageName().getLocalPart();
        List<String> strs = values.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            values.put(layerName, strs);
        }

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < results.length; i++) {
            final Object value = results[i][0];
            final Unit unit = (Unit) results[i][1];
            if (value == null) {
                continue;
            }
            builder.append(value);
            if (unit != null) {
                builder.append(" ").append(unit.toString());
            }
            //builder.append(" [").append(i).append(']').append(';');
        }

        final String result = builder.toString();
        strs.add(result.substring(0, result.length() - 2));

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResult() {
        final StringBuilder builder = new StringBuilder();

        for (final String layerName : values.keySet()) {
            builder.append(layerName).append("\n");
            final List<String> features = values.get(layerName);

            if (features.isEmpty()) {
                builder.append("No values.").append("\n");
            } else {
                for (final String record : values.get(layerName)) {
                    builder.append(record).append("\n");
                }
            }
        }

        values.clear();
        return builder.toString();
    }
}
