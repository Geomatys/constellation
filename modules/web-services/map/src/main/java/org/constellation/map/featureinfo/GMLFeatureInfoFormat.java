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
package org.constellation.map.featureinfo;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.provider.Data;
import org.constellation.provider.DataProviders;
import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.geometry.isoonjts.JTSUtils;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.internal.jaxb.ObjectFactory;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.util.DateRange;
import org.geotoolkit.ows.xml.GetFeatureInfo;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import javax.measure.unit.Unit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.awt.Rectangle;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic FeatureInfoFormat that produce GML output for Features and Coverages.
 * Supported mimeTypes are :
 * <ul>
 *     <li>application/vnd.ogc.gml : will return msGMLOutput</li>
 *     <li>application/gml+xml : will return OGC GML3</li>
 * </ul>
 *
 * @author Quentin Boileau (Geomatys)
 */
public class GMLFeatureInfoFormat extends AbstractTextFeatureInfoFormat {

    private static final Logger LOGGER = Logging.getLogger(GMLFeatureInfoFormat.class);

    private final DataProviders dp = DataProviders.getInstance();

    /**
     * GML version flag : 0 for mapserver output
     *                    1 for GML 3 output
     */
    private int mode = 1;

    private GetFeatureInfo gfi;

    /**
     * A Map of namespace / prefix
     */
    private final Map<String, String> prefixMap = new HashMap<String, String>();

    private static final MarshallerPool pool;
    static {
        MarshallerPool candidate = null;
        try {
            final Map<String, Object> properties = new HashMap<>();
            properties.put(Marshaller.JAXB_FRAGMENT, true);
            properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            candidate = new MarshallerPool(JAXBContext.newInstance(ObjectFactory.class), properties);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB Exception while initializing the marshaller pool", ex);
        }
        pool = candidate;
    }

    public GMLFeatureInfoFormat() {
        prefixMap.put("http://www.opengis.net/gml", "gml");
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
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedCoverage(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final List<Map.Entry<GridSampleDimension,Object>> results =
                FeatureInfoUtilities.getCoverageValues(coverage, context, queryArea);

        if (results == null) {
            return;
        }

        final CoverageReference ref = coverage.getLayer().getCoverageReference();
        final Name fullLayerName = ref.getName();
        String layerName = fullLayerName.getLocalPart();

        List<String> strs = coverages.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            coverages.put(layerName, strs);
        }

        StringBuilder builder = new StringBuilder();
        for (final Map.Entry<GridSampleDimension,Object> entry : results) {
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

        final List<Data> layerDetailsList = getLayersDetails();
        Data layerPostgrid = null;

        for (Data layer : layerDetailsList) {
            if (layer.getType().equals(Data.TYPE.COVERAGE) && layer.getName().equals(fullLayerName)) {
                layerPostgrid = layer;
            }
        }

        final Envelope objEnv;
        final List<Date> time;
        final Double elevation;
        if (gfi != null && gfi instanceof org.geotoolkit.wms.xml.GetFeatureInfo) {
            org.geotoolkit.wms.xml.GetFeatureInfo wmsGFI = (org.geotoolkit.wms.xml.GetFeatureInfo) gfi;
            objEnv = wmsGFI.getEnvelope2D();
            time = wmsGFI.getTime();
            elevation = wmsGFI.getElevation();
        } else {
            objEnv = null;
            time = null;
            elevation = null;
        }

        if (objEnv != null) {
            final CoordinateReferenceSystem crs = objEnv.getCoordinateReferenceSystem();
            final GeneralDirectPosition pos = getPixelCoordinates(gfi);
            if (pos != null) {
                builder.append("\t\t\t<gml:boundedBy>").append("\n");
                String crsName;
                try {
                    crsName = IdentifiedObjects.lookupIdentifier(crs, true);
                } catch (FactoryException ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    crsName = crs.getName().getCode();
                }
                builder.append("\t\t\t\t<gml:Box srsName=\"").append(crsName).append("\">\n");
                builder.append("\t\t\t\t\t<gml:coordinates>");
                builder.append(pos.getOrdinate(0)).append(",").append(pos.getOrdinate(1)).append(" ")
                        .append(pos.getOrdinate(0)).append(",").append(pos.getOrdinate(1));
                builder.append("</gml:coordinates>").append("\n");
                builder.append("\t\t\t\t</gml:Box>").append("\n");
                builder.append("\t\t\t</gml:boundedBy>").append("\n");
                builder.append("\t\t\t<x>").append(pos.getOrdinate(0)).append("</x>").append("\n")
                        .append("\t\t\t<y>").append(pos.getOrdinate(1)).append("</y>").append("\n");
            }
        }
        if (time != null && !time.isEmpty()) {
            // TODO : Adapt code to use periods.
            builder.append("\t\t\t<time>").append(time.get(time.size()-1)).append("</time>")
                    .append("\n");
        } else {
            /*
             * Get the date of the last slice in this layer. Don't invoke
             * layerPostgrid.getAvailableTimes().last() because getAvailableTimes() is very
             * costly. The layerPostgrid.getEnvelope() method is much cheaper, since it can
             * leverage the database index.
             */
            DateRange dates = null;
            if (layerPostgrid != null) {
                try {
                    dates = layerPostgrid.getDateRange();
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
            if (dates != null && !(dates.isEmpty())) {
                if (dates.getMaxValue() != null) {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    builder.append("\t\t\t<time>").append(df.format(dates.getMaxValue()))
                            .append("</time>").append("\n");
                }
            }
        }
        if (elevation != null) {
            builder.append("\t\t\t<elevation>").append(elevation)
                    .append("</elevation>").append("\n");
        } else {
            SortedSet<Number> elevs = null;
            if (layerPostgrid != null) {
                try {
                    elevs = layerPostgrid.getAvailableElevations();
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    elevs = null;
                }
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

        MeasurementRange[] ranges = null;
        if (layerPostgrid != null) {
            ranges = layerPostgrid.getSampleValueRanges();
        }
        if (ranges != null && ranges.length > 0) {
            final MeasurementRange range = ranges[0];
            if (range != null) {
                final Unit unit = range.unit();
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
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedFeature(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {

        final StringBuilder builder   = new StringBuilder();
        final FeatureMapLayer layer   = graphic.getLayer();
        final Feature feature         = graphic.getCandidate();
        final FeatureType featureType = feature.getType();
        String margin                 = "\t";

        if (mode == 0) {

            // featureType mark
            if (featureType != null) {
                final String ftLocal = featureType.getName().getLocalPart();
                builder.append(margin).append("<").append(encodeXML(ftLocal)).append("_feature").append(">\n");

                margin += "\t";
                builder.append(margin).append("<ID>").append(encodeXML(feature.getIdentifier().getID())).append("</ID>\n");
                XMLFeatureInfoFormat.complexAttributetoXML(builder, feature, margin);

                // end featureType mark
                margin = margin.substring(1);
                builder.append(margin).append("</").append(encodeXML(ftLocal)).append("_feature").append(">\n");
            } else {
                LOGGER.warning("The feature type is null");
            }

        } else {
            // feature member  mark
            builder.append(margin).append("<gml:featureMember>\n");
            margin += "\t";

            // featureType mark
            if (featureType != null) {
                String ftLocal = featureType.getName().getLocalPart();
                String ftPrefix  = acquirePrefix(featureType.getName().getNamespaceURI());

                builder.append(margin).append('<').append(ftPrefix).append(ftLocal).append(">\n");
                margin += "\t";

                toGML3(builder, feature, margin);

                // end featureType mark
                margin = margin.substring(1);
                builder.append(margin).append("</").append(ftPrefix).append(ftLocal).append(">\n");
            } else {
                LOGGER.warning("The feature type is null");
            }

            // end feature member mark
            margin = margin.substring(1);
            builder.append(margin).append("</gml:featureMember>\n");

        }

        final String result = builder.toString();
        if (builder.length() > 0) {
            final String layerName = layer.getName();
            List<String> strs = features.get(layerName);
            if (strs == null) {
                strs = new ArrayList<String>();
                features.put(layerName, strs);
            }
            strs.add(result.substring(0, result.length()));
        }
    }

    private void toGML3(final StringBuilder builder, final ComplexAttribute complexAtt, String margin) {
        for (final Property prop : complexAtt.getProperties()) {
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
                    org.opengis.geometry.Geometry gmlGeometry =  JTSUtils.toISO((Geometry) prop.getValue(), geomProp.getType().getCoordinateReferenceSystem());
                    ObjectFactory factory =  new ObjectFactory();
                    m.setProperty(Marshaller.JAXB_FRAGMENT, true);
                    m.marshal(factory.buildAnyGeometry(gmlGeometry), sw);
                    builder.append(sw.toString());
                } catch (JAXBException ex) {
                    LOGGER.log(Level.WARNING, "JAXB exception while marshalling the geometry", ex);
                } finally {
                    if (m != null) {
                        pool.recycle(m);
                    }
                }
                builder.append("\n");
                builder.append(margin).append("</").append(pPrefix).append(pLocal).append(">\n");
            } else {

                if (prop instanceof ComplexAttribute) {
                    final ComplexAttribute complex = (ComplexAttribute) prop;
                    builder.append(margin).append('<').append(pPrefix).append(pLocal).append(">\n");
                    margin += "\t";

                    toGML3(builder, complex, margin);

                    margin = margin.substring(1);
                    builder.append(margin).append("</").append(pPrefix).append(pLocal).append(">\n");
                } else {
                    //simple
                    final Object value = prop.getValue();
                    builder.append(margin).append('<').append(pPrefix).append(pLocal).append('>')
                            .append(value)
                            .append("</").append(pPrefix).append(pLocal).append(">\n");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFeatureInfo(SceneDef sdef, ViewDef vdef, CanvasDef cdef, Rectangle searchArea, GetFeatureInfo getFI) throws PortrayalException {

        this.gfi = getFI;
        final StringBuilder builder = new StringBuilder();
        if ( getFI.getInfoFormat().equals(MimeType.APP_GML)) {
            mode = 0;//msGMLOutput
        } else {
            mode = 1;//GML3
        }

        //fill coverages and features maps
        getCandidates(sdef, vdef, cdef, searchArea, -1);

        if (mode == 0) {
            // Map Server GML output
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
            for (Map.Entry<String, String> entry: prefixMap.entrySet()) {
                builder.append("xmlns:").append(entry.getValue()).append("=\"").append(entry.getKey()).append("\" ");
            }
            builder.append(">\n");
        }

        final Map<String, List<String>> values = new HashMap<String, List<String>>();
        values.putAll(features);
        values.putAll(coverages);

        for (String layerName : values.keySet()) {

            if (mode == 0) {
                builder.append("<").append(encodeXML(layerName)).append("_layer").append(">\n");
            }

            for (final String record : values.get(layerName)) {
                builder.append(record);
            }

            if (mode == 0) {
                builder.append("</").append(encodeXML(layerName)).append("_layer").append(">\n");
            }
        }

        if (mode == 0) {
            builder.append("</msGMLOutput>");
        } else {
            builder.append("</gml:featureCollection>");
        }

        features.clear();
        coverages.clear();

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedMimeTypes() {
        final List<String> mimes = new ArrayList<String>();
        mimes.add(MimeType.APP_GML);//will return map server GML
        mimes.add(MimeType.APP_GML_XML);//will return GML 3
        return mimes;
    }

    /**
     * Returns the coordinates of the requested pixel in the image, expressed in the
     * {@linkplain CoordinateReferenceSystem crs} defined in the request.
     */
    private GeneralDirectPosition getPixelCoordinates(final GetFeatureInfo gfi) {
        if (gfi != null) {

            JTSEnvelope2D objEnv = new JTSEnvelope2D();
            int width  = 0;
            int height = 0;
            int pixelX = 0;
            int pixelY = 0;

            if(gfi instanceof org.geotoolkit.wms.xml.GetFeatureInfo) {
                org.geotoolkit.wms.xml.GetFeatureInfo wmsGFI = (org.geotoolkit.wms.xml.GetFeatureInfo) gfi;
                objEnv = new JTSEnvelope2D(wmsGFI.getEnvelope2D());
                width = wmsGFI.getSize().width;
                height = wmsGFI.getSize().height;
                pixelX = wmsGFI.getX();
                pixelY = wmsGFI.getY();
            } else if (gfi instanceof org.geotoolkit.wmts.xml.v100.GetFeatureInfo) {
                org.geotoolkit.wmts.xml.v100.GetFeatureInfo wmtsGFI = (org.geotoolkit.wmts.xml.v100.GetFeatureInfo) gfi;
                objEnv = new JTSEnvelope2D(); //gfi.getEnvelope());
                width  = 0; // gfi.getSize().width;
                height = 0; // gfi.getSize().height;
                pixelX = wmtsGFI.getI();
                pixelY = wmtsGFI.getJ();
            }

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
        return null;
    }
}
