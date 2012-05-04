/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.wps.ws;

import com.vividsolutions.jts.geom.Geometry;

import org.constellation.wps.converters.outputs.references.GeometryToReferenceConverter;
import org.constellation.wps.converters.outputs.references.FeatureToReferenceConverter;
import org.constellation.wps.converters.outputs.complex.FeatureCollectionToComplexConverter;
import org.constellation.wps.converters.outputs.complex.GeometryToComplexConverter;
import org.constellation.wps.converters.outputs.complex.GeometryArrayToComplexConverter;
import org.constellation.wps.converters.outputs.complex.FeatureToComplexConverter;
import org.constellation.wps.converters.inputs.references.ReferenceToGeometryConverter;
import org.constellation.wps.converters.inputs.references.ReferenceToFeatureConverter;
import org.constellation.wps.converters.inputs.references.ReferenceToFeatureCollectionConverter;
import org.constellation.wps.converters.inputs.references.ReferenceToFileConverter;
import org.constellation.wps.converters.inputs.references.ReferenceToFeatureTypeConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToFeatureArrayConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToFeatureCollectionArrayConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToFeatureConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToFeatureCollectionConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToGeometryConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToGeometryArrayConverter;
import org.constellation.wps.converters.inputs.complex.ComplexToFeatureTypeConverter;
import org.constellation.wps.converters.inputs.references.*;
import org.constellation.wps.converters.outputs.complex.*;
import org.constellation.wps.converters.outputs.references.FeatureTypeToReferenceConverter;
import org.constellation.wps.converters.outputs.references.RenderedImageToReferenceConverter;
import org.constellation.wps.utils.WPSMimeType;
import org.constellation.ws.CstlServiceException;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import javax.measure.unit.Unit;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.process.converters.*;
import org.geotoolkit.util.NumberRange;
import org.geotoolkit.util.converter.SimpleConverter;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * 
 * @author Quentin Boileau
 */
public final class WPSIO {

    private static final List<WPSSupport> SUPPORT = Collections.synchronizedList(new ArrayList<WPSSupport>());

    static {
        /**
         * Feature.
         */
        SUPPORT.add(new WPSSupport(Feature.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));

        SUPPORT.add(new WPSSupport(Feature.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        
        SUPPORT.add(new WPSSupport(Feature.class, IOType.OUTPUT, DataType.COMPLEX, FeatureToComplexConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.OUTPUT, DataType.COMPLEX, FeatureToComplexConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.OUTPUT, DataType.COMPLEX, FeatureToComplexConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));

        SUPPORT.add(new WPSSupport(Feature.class, IOType.OUTPUT, DataType.REFERENCE, FeatureToReferenceConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.OUTPUT, DataType.REFERENCE, FeatureToReferenceConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature.class, IOType.OUTPUT, DataType.REFERENCE, FeatureToReferenceConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));

        /**
         * FeatureCollection.
         */
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureCollectionConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureCollectionConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureCollectionConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));

        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureCollectionConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureCollectionConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureCollectionConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureCollectionConverter.getInstance(), WPSMimeType.APP_SHP.getValue(), false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureCollectionConverter.getInstance(), WPSMimeType.APP_OCTET.getValue(), false));

        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.OUTPUT, DataType.COMPLEX, FeatureCollectionToComplexConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.OUTPUT, DataType.COMPLEX, FeatureCollectionToComplexConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.OUTPUT, DataType.COMPLEX, FeatureCollectionToComplexConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));

        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.OUTPUT, DataType.REFERENCE, FeatureToReferenceConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.OUTPUT, DataType.REFERENCE, FeatureToReferenceConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection.class, IOType.OUTPUT, DataType.REFERENCE, FeatureToReferenceConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));

        /**
         * Feature[].
         */
        SUPPORT.add(new WPSSupport(Feature[].class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureArrayConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature[].class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureArrayConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(Feature[].class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureArrayConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        
        /**
         * FeatureCollection[].
         */
        SUPPORT.add(new WPSSupport(FeatureCollection[].class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureCollectionArrayConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection[].class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureCollectionArrayConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, false));
        SUPPORT.add(new WPSSupport(FeatureCollection[].class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureCollectionArrayConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        
        /**
         * Geometry.
         */
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.INPUT, DataType.COMPLEX, ComplexToGeometryConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.INPUT, DataType.COMPLEX, ComplexToGeometryConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.INPUT, DataType.COMPLEX, ComplexToGeometryConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, true));

        SUPPORT.add(new WPSSupport(Geometry.class, IOType.INPUT, DataType.REFERENCE, ReferenceToGeometryConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.INPUT, DataType.REFERENCE, ReferenceToGeometryConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.INPUT, DataType.REFERENCE, ReferenceToGeometryConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, true));

        SUPPORT.add(new WPSSupport(Geometry.class, IOType.OUTPUT, DataType.COMPLEX, GeometryToComplexConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.OUTPUT, DataType.COMPLEX, GeometryToComplexConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.OUTPUT, DataType.COMPLEX, GeometryToComplexConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, true));

        SUPPORT.add(new WPSSupport(Geometry.class, IOType.OUTPUT, DataType.REFERENCE, GeometryToReferenceConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.OUTPUT, DataType.REFERENCE, GeometryToReferenceConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry.class, IOType.OUTPUT, DataType.REFERENCE, GeometryToReferenceConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, true));
        
        /**
         * Geometry[].
         */
        SUPPORT.add(new WPSSupport(Geometry[].class, IOType.INPUT, DataType.COMPLEX, ComplexToGeometryArrayConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry[].class, IOType.INPUT, DataType.COMPLEX, ComplexToGeometryArrayConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry[].class, IOType.INPUT, DataType.COMPLEX, ComplexToGeometryArrayConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, true));

        SUPPORT.add(new WPSSupport(Geometry[].class, IOType.OUTPUT, DataType.COMPLEX, GeometryArrayToComplexConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry[].class, IOType.OUTPUT, DataType.COMPLEX, GeometryArrayToComplexConverter.getInstance(), WPSMimeType.TEXT_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, false));
        SUPPORT.add(new WPSSupport(Geometry[].class, IOType.OUTPUT, DataType.COMPLEX, GeometryArrayToComplexConverter.getInstance(), WPSMimeType.APP_GML.getValue(), Encoding.UTF8, Schema.ORC_GML_3_1_1, true));
        
        /**
         * FeatureType.
         */
        SUPPORT.add(new WPSSupport(FeatureType.class, IOType.INPUT, DataType.COMPLEX, ComplexToFeatureTypeConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        SUPPORT.add(new WPSSupport(FeatureType.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFeatureTypeConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        SUPPORT.add(new WPSSupport(FeatureType.class, IOType.OUTPUT, DataType.COMPLEX, FeatureTypeToComplexConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
        SUPPORT.add(new WPSSupport(FeatureType.class, IOType.OUTPUT, DataType.REFERENCE, FeatureTypeToReferenceConverter.getInstance(), WPSMimeType.TEXT_XML.getValue(), Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, true));
             
        /**
         * RenderedImage.
         */
        for (final String readerMime : ImageIO.getReaderMIMETypes()) {
            if (!readerMime.isEmpty()) {
                if (readerMime.equals("image/png")) {
                    SUPPORT.add(new WPSSupport(RenderedImage.class, IOType.INPUT, DataType.REFERENCE, ReferenceToRenderedImageConverter.getInstance(), readerMime, null, null, true));
                } else {
                    SUPPORT.add(new WPSSupport(RenderedImage.class, IOType.INPUT, DataType.REFERENCE, ReferenceToRenderedImageConverter.getInstance(), readerMime, null, null, false));
                }
            }
        }

        for (final String writerMime : ImageIO.getWriterMIMETypes()) {
            if (!writerMime.isEmpty()) {
                if (writerMime.equals("image/png")) {
                    SUPPORT.add(new WPSSupport(RenderedImage.class, IOType.OUTPUT, DataType.REFERENCE, RenderedImageToReferenceConverter.getInstance(), writerMime, null, null, true));
                } else {
                    SUPPORT.add(new WPSSupport(RenderedImage.class, IOType.OUTPUT, DataType.REFERENCE, RenderedImageToReferenceConverter.getInstance(), writerMime, null, null, false));
                }
            }
        }
        
        /**
         * Coverage. @TODO wait to URL support in GridCoverageReader
         */
        /*for (final String readerMime : ImageIO.getReaderMIMETypes()) {
            if (!readerMime.isEmpty()) {
                if (readerMime.equals("image/png")) {
                    SUPPORT.add(new WPSSupport(Coverage.class, IOType.INPUT, DataType.REFERENCE, ReferenceToGridCoverage2DConverter.getInstance(), readerMime, null, null, true));
                } else {
                    SUPPORT.add(new WPSSupport(Coverage.class, IOType.INPUT, DataType.REFERENCE, ReferenceToGridCoverage2DConverter.getInstance(), readerMime, null, null, false));
                }
            }
        }

        for (final String writerMime : ImageIO.getWriterMIMETypes()) {
            if (!writerMime.isEmpty()) {
                if (writerMime.equals("image/png")) {
                    SUPPORT.add(new WPSSupport(Coverage.class, IOType.OUTPUT, DataType.REFERENCE, CoverageToReferenceConverter.getInstance(), writerMime, null, null, true));
                } else {
                    SUPPORT.add(new WPSSupport(Coverage.class, IOType.OUTPUT, DataType.REFERENCE, CoverageToReferenceConverter.getInstance(), writerMime, null, null, false));
                }
            }
        }*/
        
        
        /**
         * File.
         */
        SUPPORT.add(new WPSSupport(File.class, IOType.INPUT, DataType.REFERENCE, ReferenceToFileConverter.getInstance(), null, null, null, true));

        /**
         * Number.
         */
        SUPPORT.add(new WPSSupport(Number.class, IOType.INPUT, DataType.LITERAL, null, true));
        SUPPORT.add(new WPSSupport(Number.class, IOType.OUTPUT, DataType.LITERAL, null, true));
       
        /**
         * Boolean.
         */
        SUPPORT.add(new WPSSupport(Boolean.class, IOType.INPUT, DataType.LITERAL, null, true));
        SUPPORT.add(new WPSSupport(Boolean.class, IOType.OUTPUT, DataType.LITERAL, null, true));

        /**
         * String.
         */
        SUPPORT.add(new WPSSupport(String.class, IOType.INPUT, DataType.LITERAL, null, true));
        SUPPORT.add(new WPSSupport(String.class, IOType.OUTPUT, DataType.LITERAL, null, true));

        /**
         * Unit.
         */
        SUPPORT.add(new WPSSupport(Unit.class, IOType.INPUT, DataType.LITERAL, StringToUnitConverter.getInstance(), true));
        SUPPORT.add(new WPSSupport(Unit.class, IOType.OUTPUT, DataType.LITERAL, null, true));

        /**
         * AffineTransform.
         */
        SUPPORT.add(new WPSSupport(AffineTransform.class, IOType.INPUT, DataType.LITERAL, StringToAffineTransformConverter.getInstance(), true));
        SUPPORT.add(new WPSSupport(AffineTransform.class, IOType.OUTPUT, DataType.LITERAL, null, true));

        /**
         * CoordinateReferenceSystem.
         */
        SUPPORT.add(new WPSSupport(CoordinateReferenceSystem.class, IOType.INPUT, DataType.LITERAL, StringToCRSConverter.getInstance(), true));
        SUPPORT.add(new WPSSupport(CoordinateReferenceSystem.class, IOType.OUTPUT, DataType.LITERAL, null, true));

        /**
         * SortBy[].
         */
        SUPPORT.add(new WPSSupport(SortBy[].class, IOType.INPUT, DataType.LITERAL, StringToSortByConverter.getInstance(), true));

        /**
         * NumberRange[].
         */
        SUPPORT.add(new WPSSupport(NumberRange[].class, IOType.INPUT, DataType.LITERAL, StringToNumberRangeConverter.getInstance(), true));

        /**
         * Filter.
         */
        SUPPORT.add(new WPSSupport(Filter.class, IOType.INPUT, DataType.LITERAL, StringToFilterConverter.getInstance(), true));
        
        /**
         * BBOX Envelop opengis.
         */
        SUPPORT.add(new WPSSupport(Envelope.class, IOType.INPUT, DataType.BBOX, StringToFilterConverter.getInstance(), true));
        SUPPORT.add(new WPSSupport(Envelope.class, IOType.OUTPUT, DataType.BBOX, StringToFilterConverter.getInstance(), true));
       
    }

    /**
     * Private constructor.
     */
    private WPSIO() {
    }

    /**
     * Check if a class for one {@link IOType} and one {@link DataType} is supported by the service.
     *
     * @param clazz
     * @param ioType
     * @param dataType
     * @return true if supported else false.
     */
    private static boolean isSupportedClass(final Class clazz, final IOType ioType, final DataType dataType) {
        boolean isSupported = false;
        if (clazz != null) {
            for (final WPSSupport wPSSupport : SUPPORT) {
                if ((wPSSupport.getClazz().equals(clazz) || wPSSupport.getClazz().isAssignableFrom(clazz)) && wPSSupport.getType().equals(ioType)) {
                    if (dataType.equals(DataType.ALL)) {
                        isSupported = true;
                        break;
                    } else {
                        if (wPSSupport.getFrom().equals(dataType)) {
                            isSupported = true;
                            break;
                        }
                    }
                }
            }
        }
        return isSupported;
    }

    /**
     * Return the list of {@link WPSSupport } that match the given {@link Class binding}, {@link IOType io type} and {@link DataType type}.
     * 
     * @param clazz
     * @param ioType
     * @param dataType
     * @return list of {@link WPSSupport }
     */
    public static List<WPSSupport> getSupports(final Class clazz, final IOType ioType, final DataType dataType) {
        final List<WPSSupport> supports = new ArrayList<WPSSupport>();
        for (final WPSSupport wpsSupport : SUPPORT) {
            if ((wpsSupport.getClazz().equals(clazz) || wpsSupport.getClazz().isAssignableFrom(clazz))
                    && wpsSupport.getType().equals(ioType)
                    && wpsSupport.getFrom().equals(dataType)) {
                supports.add(wpsSupport);
            }
        }
        return supports;
    }

    /**
     * Check if a class is supported in INPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedInputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.INPUT, DataType.ALL);
    }

    /**
     * Check if a class is supported in OUTPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedOutputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.OUTPUT, DataType.ALL);
    }

    /**
     * Check if a class is supported for LITERAL INPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedLiteralInputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.INPUT, DataType.LITERAL);
    }

    /**
     * Check if a class is supported for COMPLEX INPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedComplexInputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.INPUT, DataType.COMPLEX);
    }

    /**
     * Check if a class is supported for REFERENCE INPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedReferenceInputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.INPUT, DataType.REFERENCE);
    }

    /**
     * Check if a class is supported for BBOX INPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedBBoxInputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.INPUT, DataType.BBOX);
    }

    /**
     * Check if a class is supported for LITERAL OUTPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedLiteralOutputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.OUTPUT, DataType.LITERAL);
    }

    /**
     * Check if a class is supported for COMPLEX OUTPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedComplexOutputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.OUTPUT, DataType.COMPLEX);
    }

    /**
     * Check if a class is supported for REFERENCE OUTPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedReferenceOutputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.OUTPUT, DataType.REFERENCE);
    }

    /**
     * Check if a class is supported for BBOX OUTPUT.
     *
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedBBoxOutputClass(final Class clazz) {
        return isSupportedClass(clazz, IOType.OUTPUT, DataType.BBOX);
    }

    /**
     * Return the converter used to parse the data, using his class, his IOType, hist DataType and his mimeType.
     *
     * @param clazz
     * @param ioType
     * @param dataType
     * @param mimeType
     * @return converter or null if not found.
     */
    public static SimpleConverter getConverter(final Class clazz, final IOType ioType, final DataType dataType, final String mimeType,
            final String encoding, final String schema) throws CstlServiceException {
        if (clazz != null) {

            final List<WPSSupport> candidates = getSupports(clazz, ioType, dataType);
            SimpleConverter converter = null;

            for (WPSSupport wpsSupport : candidates) {
                if (converter == null) {
                    converter = wpsSupport.getConverter(); // first converter found will be the default converter.
                }
                if (mimeType != null && mimeType.equalsIgnoreCase(wpsSupport.getMime())) {
                    converter = wpsSupport.getConverter();
                    break;
                }
            }
            if (converter != null) {
                return converter;
            } else {
                throw new CstlServiceException("A converter can't be found to " + clazz.getCanonicalName()
                        + " for " + dataType.toString() + "/" + ioType.toString(), NO_APPLICABLE_CODE);
            }
        }
        return null;
    }

    /**
     * Supported encoding.
     */
    public static enum Encoding {

        UTF8("utf-8");
        public final String encoding;

        private Encoding(final String encoding) {
            this.encoding = encoding;
        }

        public String getValue() {
            return encoding;
        }

        public static Encoding customValueOf(String candidate) {
            for (final Encoding encoding : values()) {
                if (encoding.getValue() != null) {
                    if (encoding.getValue().equalsIgnoreCase(candidate)) {
                        return encoding;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Supported schema.
     */
    public static enum Schema {

        OGC_FEATURE_3_1_1("http://schemas.opengis.net/gml/3.1.1/base/feature.xsd"),
        ORC_GML_3_1_1("http://schemas.opengis.net/gml/3.1.1/base/gml.xsd");
        public final String schema;

        private Schema(final String schema) {
            this.schema = schema;
        }

        public String getValue() {
            return schema;
        }

        public static Schema customValueOf(String candidate) {
            for (final Schema schema : values()) {
                if (schema.getValue() != null) {
                    if (schema.getValue().equalsIgnoreCase(candidate)) {
                        return schema;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Enumeration for INPUT/OUTPUT.
     */
    public static enum IOType {

        INPUT, OUTPUT;
    }

    /**
     * Enumeration of WPS data type.
     */
    public static enum DataType {

        LITERAL, COMPLEX, BBOX, REFERENCE, ALL;
    }

    /**
     * POJO that contain for one supported INPUT or OUTPUT. That pojo define an 
     * {@link Class binding}, {@link IOType io type} (INPUT/OUTPUT), {@link DataType data type} define by the WPS standard 
     * (LITERAL,COMPLEX,BBOX,REFERENCE). He also define a {@link SimpleConverter converter}, a mimeType, an encoding and a schema.
     */
    public static class WPSSupport {

        private Class clazz;
        private IOType type;
        private DataType from;
        private SimpleConverter converter;
        private String mime;
        private Encoding encoding;
        private Schema schema;
        private boolean defaultIO;

        public WPSSupport(final Class clazz, final IOType type, final DataType from, final SimpleConverter converter,
                final boolean defaultIO) {
            this(clazz, type, from, converter, null, null, null, defaultIO);
        }

        public WPSSupport(final Class clazz, final IOType type, final DataType from, final SimpleConverter converter,
                final String mime, final boolean defaultIO) {
            this(clazz, type, from, converter, mime, null, null, defaultIO);
        }

        public WPSSupport(final Class clazz, final IOType type, final DataType from, final SimpleConverter converter,
                final String mime, final Encoding encoding, final Schema schema, final boolean defaultIO) {
            this.clazz = clazz;
            this.type = type;
            this.from = from;
            this.converter = converter;
            this.mime = mime;
            this.encoding = encoding;
            this.schema = schema;
            this.defaultIO = defaultIO;
        }

        public Class getClazz() {
            return clazz;
        }

        public SimpleConverter getConverter() {
            return converter;
        }

        public boolean isDefaultIO() {
            return defaultIO;
        }

        public Encoding getEncoding() {
            return encoding;
        }

        public DataType getFrom() {
            return from;
        }

        public String getMime() {
            return mime;
        }

        public Schema getSchema() {
            return schema;
        }

        public IOType getType() {
            return type;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final WPSSupport other = (WPSSupport) obj;
            if (this.clazz != other.clazz && (this.clazz == null || (!this.clazz.equals(other.clazz) && !other.clazz.isAssignableFrom(this.clazz)))) {
                return false;
            }
            if (this.type != other.type) {
                return false;
            }
            if (this.from != other.from) {
                return false;
            }
            if ((this.mime == null) ? (other.mime != null) : !this.mime.equals(other.mime)) {
                return false;
            }
            if (this.encoding != other.encoding) {
                return false;
            }
            if (this.schema != other.schema) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
            hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 37 * hash + (this.from != null ? this.from.hashCode() : 0);
            return hash;
        }
    }
}
