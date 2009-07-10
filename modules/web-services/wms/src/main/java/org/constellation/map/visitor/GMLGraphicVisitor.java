/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Dimension;
import java.awt.Shape;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.measure.unit.Unit;

import org.constellation.catalog.CatalogException;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.query.wms.GetFeatureInfo;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.metadata.iso.citation.Citations;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.MeasurementRange;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Visit results of a GetFeatureInfo request, and format the output into GML.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public final class GMLGraphicVisitor extends TextGraphicVisitor {

    private final LayerProviderProxy dp = LayerProviderProxy.getInstance();
    private final Map<String, List<String>> values = new HashMap<String, List<String>>();
    private int index = 0;

    public GMLGraphicVisitor(GetFeatureInfo gfi) {
        super(gfi);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        Integer count = gfi.getFeatureCount();
        if (count != null) {
            return (index == count);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic, Shape queryArea) {
        index++;
        final StringBuilder builder = new StringBuilder();
        final FeatureMapLayer layer = graphic.getFeatureLayer();
        final Feature feature = graphic.getFeature();

        for (final Property prop : feature.getProperties()) {
            if (prop == null) {
                continue;
            }
            final Name propName = prop.getName();
            if (propName == null) {
                continue;
            }

            if (Geometry.class.isAssignableFrom(prop.getType().getBinding())) {
                builder.append(propName.toString()).append(':').append(prop.getType().getBinding().getSimpleName()).append(';');
            } else {
                Object value = prop.getValue();
                builder.append(propName.toString()).append(':').append(value).append(';');
            }
        }

        final String result = builder.toString();
        if (builder.length() > 0 && result.endsWith(";")) {
            final String layerName = layer.getName();
            List<String> strs = values.get(layerName);
            if (strs == null) {
                strs = new ArrayList<String>();
                values.put(layerName, strs);
            }
            strs.add(result.substring(0, result.length() - 1));
        }

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

        final String layerName = coverage.getCoverageLayer().getName();
        List<String> strs = values.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            values.put(layerName, strs);
        }

        StringBuilder builder = new StringBuilder();
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
            //builder.append(" [").append(i).append(']');
        }

        final String result = builder.toString();
        builder = new StringBuilder();

        final String layerNameCorrected = layerName.replaceAll("\\W", "");
        builder.append("\t<").append(layerNameCorrected).append("_layer").append(">\n")
               .append("\t\t<").append(layerNameCorrected).append("_feature").append(">\n");

        final LayerDetails layerPostgrid = dp.get(layerName);
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
            } catch (CatalogException ex) {
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
            } catch (CatalogException ex) {
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
        } catch (CatalogException cat) {
            cat.printStackTrace();
            return;
        } catch (IOException io) {
            io.printStackTrace();
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
               .append("\t\t</").append(layerNameCorrected).append("_feature").append(">\n")
               .append("\t</").append(layerNameCorrected).append("_layer").append(">\n");

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
        final ReferencedEnvelope objEnv = new ReferencedEnvelope(gfi.getEnvelope());
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
