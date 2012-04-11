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

import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.*;
import javax.measure.unit.Unit;
import org.constellation.wps.converters.*;
import org.constellation.wps.utils.WPSMimeType;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.process.converters.*;
import org.geotoolkit.process.vector.sort.SortBy;
import org.geotoolkit.util.NumberRange;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Quentin Boileau
 */
public final class WPSIO {

   
    public final static Map<KeyTuple,List<DataInfo>> IOCLASSMAP = Collections.synchronizedMap(new HashMap<KeyTuple, List<DataInfo>> ());
    static{
        
        /* Feature */
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(Feature.class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureConverter.getInstance()), //GML
                    new DataInfo(false, WPSMimeType.APP_OCTET, Encoding.NONE, Schema.NONE,              ComplexToFeatureConverter.getInstance()), //SHP
                    new DataInfo(false, WPSMimeType.APP_SHP,   Encoding.NONE, Schema.NONE,              ComplexToFeatureConverter.getInstance())  //SHP
                ));
        //Reference INPUT
        IOCLASSMAP.put(new KeyTuple(Feature.class, IOType.INPUT, DataType.REFERENCE), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureConverter.getInstance()) //GML
                ));
        //Complex OUTPUT
        IOCLASSMAP.put(new KeyTuple(Feature.class, IOType.OUTPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, FeatureToComplexConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, FeatureToComplexConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, FeatureToComplexConverter.getInstance()) //GML
                ));
        
        
        /* FeatureCollection */
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(FeatureCollection.class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureCollectionConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureCollectionConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureCollectionConverter.getInstance()), //GML
                    new DataInfo(false, WPSMimeType.APP_OCTET, Encoding.NONE, Schema.NONE,              ComplexToFeatureCollectionConverter.getInstance()), //SHP
                    new DataInfo(false, WPSMimeType.APP_SHP,   Encoding.NONE, Schema.NONE,              ComplexToFeatureCollectionConverter.getInstance())  //SHP
                ));
        //Reference INPUT
        IOCLASSMAP.put(new KeyTuple(FeatureCollection.class, IOType.INPUT, DataType.REFERENCE), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureCollectionConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureCollectionConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureCollectionConverter.getInstance()) //GML
                ));
        //Complex OUTPUT
        IOCLASSMAP.put(new KeyTuple(FeatureCollection.class, IOType.OUTPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, FeatureCollectionToComplexConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, FeatureCollectionToComplexConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, FeatureCollectionToComplexConverter.getInstance()) //GML
                ));
        
        
        /* Feature[]*/
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(Feature[].class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureArrayConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureArrayConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureArrayConverter.getInstance()) //GML
                ));
        
        
        /* FeatureCollection[] */
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(FeatureCollection[].class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureCollectionArrayConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureCollectionArrayConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureCollectionArrayConverter.getInstance()) //GML
                ));
        
        
        /* Geometry */
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(com.vividsolutions.jts.geom.Geometry.class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToGeometryConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToGeometryConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToGeometryConverter.getInstance()) //GML
                ));
        //Reference INPUT
        IOCLASSMAP.put(new KeyTuple(com.vividsolutions.jts.geom.Geometry.class, IOType.INPUT, DataType.REFERENCE), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToGeometryConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToGeometryConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToGeometryConverter.getInstance()) //GML
                ));
        IOCLASSMAP.put(new KeyTuple(com.vividsolutions.jts.geom.Geometry.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.NONE,  Encoding.NONE, Schema.NONE, StringToGeometryConverter.getInstance()) //WKT
                ));
        //Complex OUTPUT
        IOCLASSMAP.put(new KeyTuple(com.vividsolutions.jts.geom.Geometry.class, IOType.OUTPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, GeometryToComplexConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, GeometryToComplexConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, GeometryToComplexConverter.getInstance()) //GML
                ));
        
        
        /* Geometry[] */
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(com.vividsolutions.jts.geom.Geometry.class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToGeometryArrayConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToGeometryArrayConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToGeometryArrayConverter.getInstance()) //GML
                ));
         //Complex OUTPUT
        IOCLASSMAP.put(new KeyTuple(com.vividsolutions.jts.geom.Geometry.class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, GeometryArrayToComplexConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, GeometryArrayToComplexConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, GeometryArrayToComplexConverter.getInstance()) //GML
                ));
        
        
        /* FeatureType */
        //Complex INPUT
        IOCLASSMAP.put(new KeyTuple(FeatureType.class, IOType.INPUT, DataType.COMPLEX), UnmodifiableArrayList.wrap(
                    new DataInfo(true,  WPSMimeType.TEXT_XML, Encoding.UTF8, Schema.NONE,              ComplexToFeatureTypeConverter.getInstance()),
                    new DataInfo(false, WPSMimeType.TEXT_GML, Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureTypeConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.APP_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ComplexToFeatureTypeConverter.getInstance()) //XML
                ));
        
        //Refernce INPUT
        IOCLASSMAP.put(new KeyTuple(FeatureType.class, IOType.INPUT, DataType.REFERENCE), UnmodifiableArrayList.wrap(
                    new DataInfo(false, WPSMimeType.TEXT_XML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureTypeConverter.getInstance()), //XML
                    new DataInfo(false, WPSMimeType.TEXT_GML,  Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureTypeConverter.getInstance()), //GML
                    new DataInfo(true,  WPSMimeType.APP_GML,   Encoding.UTF8, Schema.OGC_FEATURE_3_1_1, ReferenceToFeatureTypeConverter.getInstance()) //GML
                ));
        
        
        /* File */
        //Reference INPUT
        IOCLASSMAP.put(new KeyTuple(File.class, IOType.INPUT, DataType.REFERENCE), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.APP_OCTET, Encoding.NONE, Schema.NONE,             ReferenceToFileConverter.getInstance()) //octet-stream
                ));
        
        
        /* Number */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(Number.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(Number.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* Boolean */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(Boolean.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(Boolean.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* String */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(String.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(String.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* Unit */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(Unit.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToUnitConverter.getInstance()) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(Unit.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* AffineTransform */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(AffineTransform.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToAffineTransformConverter.getInstance()) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(AffineTransform.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* CoordinateReferenceSystem */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(CoordinateReferenceSystem.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToCRSConverter.getInstance()) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(CoordinateReferenceSystem.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* CoordinateReferenceSystem */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(CoordinateReferenceSystem.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToCRSConverter.getInstance()) 
                ));
        //Literal OUTPUT
        IOCLASSMAP.put(new KeyTuple(CoordinateReferenceSystem.class, IOType.OUTPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, null) 
                ));
        
        
        /* SortBy[] */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(SortBy[].class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToSortByConverter.getInstance()) 
                ));
        
        
        /* NumberRange[] */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(NumberRange[].class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToSortByConverter.getInstance()) 
                ));
        
        
        /* Filter */
        //Literal INPUT
        IOCLASSMAP.put(new KeyTuple(Filter.class, IOType.INPUT, DataType.LITERAL), UnmodifiableArrayList.wrap(
                    new DataInfo(true, WPSMimeType.NONE, Encoding.NONE, Schema.NONE, StringToFilterConverter.getInstance()) 
                ));
        
    }
    /**
     * Private constructor.
     */    
    private WPSIO() {
    }
   
    /**
     * Check if a class for one IOType and one DataType is supported by the service.
     *
     * @param clazz
     * @param ioType
     * @param dataType
     * @return true if supported else false.
     */
    private static boolean isSupportedClass(final Class clazz, final IOType ioType, final DataType dataType) {
        boolean isSupported = false;
        if (clazz != null) {
            if(dataType.equals(DataType.ALL)){
                final Set<Map.Entry<KeyTuple, List<DataInfo>>> entrySet = IOCLASSMAP.entrySet();
                for (final Map.Entry<KeyTuple, List<DataInfo>> entry : entrySet) {
                    final KeyTuple key = entry.getKey();
                    if((key.getClazz().equals(clazz) || key.getClazz().isAssignableFrom(clazz)) && key.getType().equals(ioType)){
                        isSupported = true;
                        break;
                    }
                }
            }else{
                if (IOCLASSMAP.containsKey(new KeyTuple(clazz, ioType, dataType))) {
                    isSupported = true;
                }
            }
        }
        return isSupported;
    }
    
    /**
     * Check if a class is supported in INPUT.
     * 
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedInputClass(final Class clazz){
        return isSupportedClass(clazz, IOType.INPUT, DataType.ALL);
    }

    /**
     * Check if a class is supported in OUTPUT.
     * 
     * @param clazz
     * @return true if supported, false otherwise.
     */
    public static boolean isSupportedOutputClass(final Class clazz){
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
     * Return the converter used to parse the data, using his class, his IOType, hist DataType and his mimeType.
     * 
     * @param clazz
     * @param ioType
     * @param dataType
     * @param mimeType
     * @return converter or null if not found.
     */
    public static SimpleConverter getConverter(final Class clazz, final IOType ioType, final DataType dataType, final String mimeType){
        if(clazz!=null){
            final KeyTuple key = new KeyTuple(clazz, ioType, dataType);
            if(IOCLASSMAP.containsKey(key)){
                final List<DataInfo> infos = IOCLASSMAP.get(key);
                for (final DataInfo dataInfo : infos) {
                    if(WPSMimeType.valueOf(mimeType).equals(dataInfo.getMime())){
                        return dataInfo.getConverter();
                    }
                }
                
            }
        }
        return null;
    }

    /**
     * Supported encoding.
     */
    public static enum Encoding {

        NONE(null),
        UTF8("utf-8");
        public final String encoding;

        private Encoding(final String encoding) {
            this.encoding = encoding;
        }

        public String getValue() {
            return encoding;
        }
    }

    /**
     * Supported schema.
     */
    public static enum Schema {

        NONE(null),
        OGC_FEATURE_3_1_1("http://schemas.opengis.net/gml/3.1.1/base/feature.xsd");
        public final String schema;

        private Schema(final String schema) {
            this.schema = schema;
        }

        public String getValue() {
            return schema;
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
     * Tuple that define a data using his class, his IOType and his WPS type.
     */
    public static class KeyTuple {
        private Class clazz;
        private IOType type;
        private DataType from;

        public KeyTuple(final Class clazz, final IOType type, final DataType from) {
            this.clazz = clazz;
            this.type = type;
            this.from = from;
        }

        public Class getClazz() {
            return clazz;
        }

        public DataType getFrom() {
            return from;
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
            final KeyTuple other = (KeyTuple) obj;
            if (this.clazz != other.clazz && (this.clazz == null || (!this.clazz.equals(other.clazz) && !other.clazz.isAssignableFrom(this.clazz)))) {
                return false;
            }
            if (this.type != other.type) {
                return false;
            }
            if (this.from != other.from) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 41 * hash + (this.from != null ? this.from.hashCode() : 0);
            return hash;
        }
        
    }
    
    /**
     * Define for one data (INPUT/OUTPUT) whatever his WPS type, informations supported informations like :
     * <ul>
     *  <li>If it the default dataInfo or not.</li>
     *  <li>Supported MimeType</li>
     *  <li>Supported Encoding</li>
     *  <li>Supported Schema (GML)</li>
     *  <li>The converter to use.</li>
     * </ul>
     */
    public static class DataInfo {
        
        private boolean defaultIO;
        private WPSMimeType mime;
        private Encoding encoding;
        private Schema schema;
        private SimpleConverter converter;

        public DataInfo(final boolean defaultIO, final WPSMimeType mime, final Encoding encoding, final Schema schema, final SimpleConverter converter) {
            this.defaultIO = defaultIO;
            this.mime = mime;
            this.encoding = encoding;
            this.schema = schema;
            this.converter = converter;
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

        public WPSMimeType getMime() {
            return mime;
        }

        public Schema getSchema() {
            return schema;
        } 
    }
    
}
