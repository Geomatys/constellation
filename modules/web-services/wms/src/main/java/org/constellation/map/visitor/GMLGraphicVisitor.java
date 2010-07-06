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
package org.constellation.map.visitor;

import java.awt.Dimension;
import java.awt.Shape;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.query.wms.GetFeatureInfo;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.metadata.iso.citation.Citations;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.MeasurementRange;
import org.opengis.feature.type.Name;

import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Visit results of a GetFeatureInfo request, and format the output into GML.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public final class GMLGraphicVisitor extends TextGraphicVisitor {

    private final LayerProviderProxy dp = LayerProviderProxy.getInstance();

    private int index = 0;

    public GMLGraphicVisitor(GetFeatureInfo gfi) {
        super(gfi);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        final Integer count = gfi.getFeatureCount();
        if (count != null) {
            return index == count;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic, Shape queryArea) {
        super.visit(graphic, queryArea);
        index++;
        //TODO handle features as real GML features here
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

        final Name fullLayerName = coverage.getCoverageLayer().getCoverageName();
        final String layerName = fullLayerName.getLocalPart();

        List<String> strs = values.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            values.put(layerName, strs);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < results.length; i++) {
            final Object value = results[i][0];
            if (value == null) {
                continue;
            }
            builder.append(value);
        }

        final String result = builder.toString();
        builder = new StringBuilder();

        final String endMark = ">\n";
        final String layerNameCorrected = layerName.replaceAll("\\W", "");
        builder.append("\t<").append(layerNameCorrected).append("_layer").append(endMark)
               .append("\t\t<").append(layerNameCorrected).append("_feature").append(endMark);

        final LayerDetails layerPostgrid = dp.getByIdentifier(fullLayerName);
        final Envelope objEnv = gfi.getEnvelope();
        final Date time = gfi.getTime();
        final Double elevation = gfi.getElevation();
        final CoordinateReferenceSystem crs = objEnv.getCoordinateReferenceSystem();
        builder.append("\t\t\t<gml:boundedBy>").append("\n");
        String crsName;
        try {
            crsName = CRS.lookupIdentifier(Citations.EPSG, crs, true);
            if (!crsName.startsWith("EPSG:")) {
                crsName = "ESPG:" + crsName;
            }
        } catch (FactoryException ex) {
            crsName = crs.getName().getCode();
        }
        builder.append("\t\t\t\t<gml:Box srsName=\"").append(crsName).append("\">\n");
        builder.append("\t\t\t\t\t<gml:coordinates>");
        final GeneralDirectPosition pos = getPixelCoordinates(gfi);
        builder.append(pos.getOrdinate(0)).append(",").append(pos.getOrdinate(1)).append(" ")
               .append(pos.getOrdinate(0)).append(",").append(pos.getOrdinate(1));
        builder.append("</gml:coordinates>").append("\n");
        builder.append("\t\t\t\t</gml:Box>").append("\n");
        builder.append("\t\t\t</gml:boundedBy>").append("\n");
        builder.append("\t\t\t<x>").append(pos.getOrdinate(0)).append("</x>").append("\n")
               .append("\t\t\t<y>").append(pos.getOrdinate(1)).append("</y>").append("\n");
        if (time != null) {
            builder.append("\t\t\t<time>").append(time).append("</time>")
                   .append("\n");
        } else {
            SortedSet<Date> dates = null;
            try {
                dates = layerPostgrid.getAvailableTimes();
            } catch (DataStoreException ex) {
                dates = null;
            }
            if (dates != null && !(dates.isEmpty())) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                builder.append("\t\t\t<time>").append(df.format(dates.last()))
                       .append("</time>").append("\n");
            }
        }
        if (elevation != null) {
            builder.append("\t\t\t<elevation>").append(elevation)
                   .append("</elevation>").append("\n");
        } else {
            SortedSet<Number> elevs = null;
            try {
                elevs = layerPostgrid.getAvailableElevations();
            } catch (DataStoreException ex) {
                elevs = null;
            }
            if (elevs != null && !(elevs.isEmpty())) {
                builder.append("\t\t\t<elevation>").append(elevs.first().toString())
                       .append("</elevation>").append("\n");
            }
        }
        final GridCoverage2D grid;
        try {
            grid = layerPostgrid.getCoverage(objEnv, new Dimension(gfi.getSize()), elevation, time);
        } catch (DataStoreException cat) {
            Logger.getAnonymousLogger().log(Level.SEVERE, cat.getMessage(), cat);
            return;
        } catch (IOException io) {
            Logger.getAnonymousLogger().log(Level.SEVERE, io.getMessage(), io);
            return;
        }
        if (grid != null) {
            builder.append("\t\t\t<variable>")
                   .append(grid.getSampleDimension(0).getDescription())
                   .append("</variable>").append("\n");
        }
        final MeasurementRange[] ranges = layerPostgrid.getSampleValueRanges();
        if (ranges != null && ranges.length > 0 && !ranges[0].toString().equals("")) {
            builder.append("\t\t\t<unit>").append(ranges[0].getUnits().toString())
                   .append("</unit>").append("\n");
        }
        builder.append("\t\t\t<value>").append(result)
               .append("</value>").append("\n")
               .append("\t\t</").append(layerNameCorrected).append("_feature").append(endMark)
               .append("\t</").append(layerNameCorrected).append("_layer").append(endMark);

        strs.add(builder.toString());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResult(){
        final StringBuilder builder = new StringBuilder();

        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n")
               .append("<msGMLOutput xmlns:gml=\"http://www.opengis.net/gml\" ")
               .append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
               .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
               .append("\n");

        for (String layerName : values.keySet()) {
            for (final String record : values.get(layerName)) {
                builder.append(record).append("\n");
            }
        }
        builder.append("</msGMLOutput>");


        values.clear();
        return builder.toString();
    }

    /**
     * Returns the coordinates of the requested pixel in the image, expressed in the
     * {@linkplain CoordinateReferenceSystem crs} defined in the request.
     */
    public GeneralDirectPosition getPixelCoordinates(final GetFeatureInfo gfi) {
        final JTSEnvelope2D objEnv = new JTSEnvelope2D(gfi.getEnvelope());
        final int width = gfi.getSize().width;
        final int height = gfi.getSize().height;
        final int pixelX = gfi.getX();
        final int pixelY = gfi.getY();
        final double widthEnv = objEnv.getSpan(0);
        final double heightEnv = objEnv.getSpan(1);
        final double resX = widthEnv / width;
        final double resY = -1 * heightEnv / height;
        final double geoX = (pixelX + 0.5) * resX + objEnv.getMinimum(0);
        final double geoY = (pixelY + 0.5) * resY + objEnv.getMaximum(1);
        final GeneralDirectPosition position = new GeneralDirectPosition(geoX, geoY);
        position.setCoordinateReferenceSystem(objEnv.getCoordinateReferenceSystem());
        return position;
    }
}
