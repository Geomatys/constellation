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

import com.vividsolutions.jts.geom.Geometry;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.measure.unit.Unit;

import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.query.wms.GetFeatureInfo;

import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.isoonjts.JTSUtils;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.internal.jaxb.ObjectFactory;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.DateRange;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;

import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Visit results of a GetFeatureInfo request, and format the output into GML.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public final class GMLGraphicVisitor extends TextGraphicVisitor {
    private static final Logger LOGGER = Logging.getLogger(GMLGraphicVisitor.class);

    private final LayerProviderProxy dp = LayerProviderProxy.getInstance();

    private int index = 0;

    /**
     * a flag indicating the version of GML: 0 for mapserver output
     *                                       1 for GML 3 output
     */
    private final int mode;

    /**
     * A Map of namespace / prefix
     */
    private final Map<String, String> prefixMap = new HashMap<String, String>();

    private static final MarshallerPool pool;
    static {
        MarshallerPool candidate = null;
        try {
            final Map<String, String> properties = new HashMap<String, String>();
            properties.put(Marshaller.JAXB_FRAGMENT, "true");
            properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, "false");
            candidate = new MarshallerPool(properties, ObjectFactory.class);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB Exception while initalizing the marshaller pool", ex);
        }
        pool = candidate;
    }


    public GMLGraphicVisitor(GetFeatureInfo gfi, int mode) {
        super(gfi);
        this.mode = mode;
        prefixMap.put("http://www.opengis.net/gml", "gml");
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
    public void visit(ProjectedFeature graphic,  RenderingContext2D context, SearchAreaJ2D queryArea) {
        if (mode == 0) {
            super.visit(graphic, context, queryArea);
        } else {

            //TODO handle features as real GML features here
            final StringBuilder builder   = new StringBuilder();
            final FeatureMapLayer layer   = graphic.getLayer();
            final Feature feature         = graphic.getCandidate();
            final FeatureType featureType = feature.getType();
            String margin                 = "\t";

            // feature member  mark
            builder.append(margin).append("<gml:featureMember>\n");
            margin += "\t";

            // featureType mark
            if (featureType != null) {
                String ftLocal = featureType.getName().getLocalPart();
                String ftPrefix  = acquirePrefix(featureType.getName().getNamespaceURI());

                builder.append(margin).append('<').append(ftPrefix).append(ftLocal).append(">\n");
                margin += "\t";

                for (final Property prop : feature.getProperties()) {
                    if (prop == null) {
                        continue;
                    }
                    final Name propName = prop.getName();
                    if (propName == null) {
                        continue;
                    }
                    String pLocal = propName.getLocalPart();
                    String pPrefix  = acquirePrefix(propName.getNamespaceURI());

                    if (Geometry.class.isAssignableFrom(prop.getType().getBinding())) {
                        GeometryAttribute geomProp = (GeometryAttribute) prop;
                        builder.append(margin).append('<').append(pPrefix).append(pLocal).append(">\n");
                        Marshaller m = null;
                        try {
                             m = pool.acquireMarshaller();
                             StringWriter sw = new StringWriter();
                             org.opengis.geometry.Geometry gmlGeometry =  JTSUtils.toISO((Geometry) prop.getValue(),geomProp.getType().getCoordinateReferenceSystem());
                             ObjectFactory factory =  new ObjectFactory();
                             m.setProperty(Marshaller.JAXB_FRAGMENT, true);
                             m.marshal(factory.buildAnyGeometry(gmlGeometry), sw);
                             builder.append(sw.toString());
                        } catch (JAXBException ex) {
                            LOGGER.log(Level.WARNING, "JAXB exception while marshalling the geometry", ex);
                        } finally {
                            if (m != null) {
                                pool.release(m);
                            }
                        }
                        builder.append(margin).append("</").append(pPrefix).append(pLocal).append(">\n");
                    } else {
                        final Object value = prop.getValue();
                        builder.append(margin).append('<').append(pPrefix).append(pLocal).append('>').append(value).append("</").append(pPrefix).append(pLocal).append(">\n");
                    }
                }

                // end featureType mark
                margin = margin.substring(1);
                builder.append(margin).append("</").append(ftPrefix).append(ftLocal).append(">\n");
            } else {
                LOGGER.warning("The feature type is null");
            }

            // end feature member mark
            margin = margin.substring(1);
            builder.append(margin).append("</gml:featureMember>");

            final String result = builder.toString();
            if (builder.length() > 0) {
                final String layerName = layer.getName();
                List<String> strs = values.get(layerName);
                if (strs == null) {
                    strs = new ArrayList<String>();
                    values.put(layerName, strs);
                }
                strs.add(result.substring(0, result.length()));
            }
        }
        index++;
    }

    /**
     * Return the defined prefix for the specified namespace.
     * if it does not already exist a prefix for this namespace,
     * a new one will be created on the form: "ns" + prefixMap.size()
     *
     * @param namespace a attribute or featureType namespace.
     *
     * @return a prefix used in XML.
     */
    private String acquirePrefix(String namespace) {
        if (namespace != null && !namespace.isEmpty()) {
            String result = prefixMap.get(namespace);
            if (result == null) {
                result = "ns" + prefixMap.size();
                prefixMap.put(namespace,result);
            }
            return result + ":";
        }
        return "";
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedCoverage coverage,  RenderingContext2D context, SearchAreaJ2D queryArea) {
        index++;
        final List<Entry<GridSampleDimension,Object>> results = getCoverageValues(coverage, context, queryArea);

        if (results == null) {
            return;
        }

        final Name fullLayerName = coverage.getLayer().getCoverageName();
        String layerName = fullLayerName.getLocalPart();

        List<String> strs = values.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            values.put(layerName, strs);
        }

        StringBuilder builder = new StringBuilder();
        for (final Entry<GridSampleDimension,Object> entry : results) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            builder.append(value);
        }

        final String result = builder.toString();
        builder = new StringBuilder();

        final String endMark = ">\n";
        layerName = layerName.replaceAll("\\W", "");
        builder.append("\t<").append(layerName).append("_layer").append(endMark)
               .append("\t\t<").append(layerName).append("_feature").append(endMark);

        final LayerDetails layerPostgrid = dp.getByIdentifier(fullLayerName);
        final Envelope objEnv = gfi.getEnvelope2D();
        final Date time = gfi.getTime();
        final Double elevation = gfi.getElevation();
        final CoordinateReferenceSystem crs = objEnv.getCoordinateReferenceSystem();
        builder.append("\t\t\t<gml:boundedBy>").append("\n");
        String crsName;
        try {
            crsName = CRS.lookupIdentifier(crs, true);
        } catch (FactoryException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
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
            /*
             * Get the date of the last slice in this layer. Don't invoke
             * layerPostgrid.getAvailableTimes().last() because getAvailableTimes() is very
             * costly. The layerPostgrid.getEnvelope() method is much cheaper, since it can
             * leverage the database index.
             */
            DateRange dates = null;
            try {
                dates = layerPostgrid.getDateRange();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            if (dates != null && !(dates.isEmpty())) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                builder.append("\t\t\t<time>").append(df.format(dates.getMaxValue()))
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
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                elevs = null;
            }
            if (elevs != null && !(elevs.isEmpty())) {
                builder.append("\t\t\t<elevation>").append(elevs.first().toString())
                       .append("</elevation>").append("\n");
            }
        }

        if (!results.isEmpty()) {
            builder.append("\t\t\t<variable>")
                   .append(results.get(0).getKey().getDescription())
                   .append("</variable>").append("\n");
        }

        final MeasurementRange[] ranges = layerPostgrid.getSampleValueRanges();
        if (ranges != null && ranges.length > 0) {
            final MeasurementRange range = ranges[0];
            if (range != null) {
                final Unit unit = range.getUnits();
                if (unit != null && !unit.toString().isEmpty()) {
                    builder.append("\t\t\t<unit>").append(unit.toString())
                            .append("</unit>").append("\n");
                }
            }
        }
        builder.append("\t\t\t<value>").append(result)
               .append("</value>").append("\n")
               .append("\t\t</").append(layerName).append("_feature").append(endMark)
               .append("\t</").append(layerName).append("_layer").append(endMark);

        strs.add(builder.toString());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResult(){
        final StringBuilder builder = new StringBuilder();

        if (mode == 0) {
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n")
                   .append("<msGMLOutput xmlns:gml=\"http://www.opengis.net/gml\" ")
                   .append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
                   .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
                   .append("\n");
        } else {
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n")
                   .append("<gml:featureCollection ")
                   .append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
                   .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
            for (Entry<String, String> entry: prefixMap.entrySet()) {
                builder.append("xmlns:").append(entry.getValue()).append("=\"").append(entry.getKey()).append("\" ");
            }
            builder.append(">\n");
        }

        for (String layerName : values.keySet()) {
            for (final String record : values.get(layerName)) {
                builder.append(record).append("\n");
            }
        }
        if (mode == 0) {
            builder.append("</msGMLOutput>");
        } else {
            builder.append("</gml:featureCollection>");
        }


        values.clear();
        return builder.toString();
    }

    /**
     * Returns the coordinates of the requested pixel in the image, expressed in the
     * {@linkplain CoordinateReferenceSystem crs} defined in the request.
     */
    public GeneralDirectPosition getPixelCoordinates(final GetFeatureInfo gfi) {
        final JTSEnvelope2D objEnv = new JTSEnvelope2D(gfi.getEnvelope2D());
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
