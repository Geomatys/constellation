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

package org.constellation.filter;

import java.awt.geom.Line2D;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Apache Lucene dependencies
import org.apache.lucene.search.Filter;

// constellation dependencies
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.filter.text.cql2.CQL;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.gml.xml.v311.CoordinatesType;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.geometry.jts.SRIDGenerator.Version;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.LineStringType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SpatialFilterType;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.DistanceBufferType;
import org.geotoolkit.ogc.xml.v110.BinarySpatialOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.filter.FilterFactoryImpl;
import org.geotoolkit.lucene.filter.SerialChainFilter;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.lucene.filter.LuceneOGCFilter.*;

// GeoAPI dependencies
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// JTS dependencies
import com.vividsolutions.jts.geom.Geometry;
import org.geotoolkit.gml.GeometrytoJTS;

import static org.constellation.metadata.CSWConstants.*;

/**
 *
 * @author Guilhem Legal
 */
public abstract class FilterParser {

    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");

    protected static final String PARSE_ERROR_MSG = "The service was unable to parse the Date: ";

    protected static final String UNKNOW_CRS_ERROR_MSG = "Unknow Coordinate Reference System: ";

    protected static final String INCORRECT_BBOX_DIM_ERROR_MSG = "The dimensions of the bounding box are incorrect: ";

    protected static final String FACTORY_BBOX_ERROR_MSG = "Factory exception while parsing spatial filter BBox: ";
    
   /**
     * Build a Filter with the specified CQL query
     * 
     * @param cqlQuery A well-formed CQL query .
     */
    public static FilterType cqlToFilter(String cqlQuery) throws CQLException, JAXBException {
        FilterType result;
        final Object newFilter = CQL.toFilter(cqlQuery, new FilterFactoryImpl());
        /*
         * here we put a temporary patch consisting in using the geotoolkit filterFactory implementation
         * instead of our own implementation.
         * Then we unmarshaller the xml to get a constellation Filter object.
         *
         * File f = File.createTempFile("CQL", "query");
         * FileWriter fw = new FileWriter(f);
         * new FilterTransformer().transform(newFilter, fw);
         * fw.close();
         * JAXBElement jb = (JAXBElement) filterUnMarshaller.unmarshal(f);
         */

        if (!(newFilter instanceof FilterType)) {
            result = new FilterType(newFilter);
        } else {
            result = (FilterType) newFilter;
        }
        
        /*Debuging purpose
        filterMarshaller.marshal(result, System.out);*/
        return result;
    }
    
    /**
     * Transform A GML lineString into a treatable geometric object : Line2D
     * 
     * @param GMLlineString A GML lineString.
     * 
     * @return A Line2D. 
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     */
    public static Line2D gmlLineToline2d(LineStringType gmlLine) throws NoSuchAuthorityCodeException, FactoryException, CstlServiceException {
        final String crsName = gmlLine.getSrsName();
        if (crsName == null) {
            throw new CstlServiceException("A CRS (coordinate Reference system) must be specified for the line.",
                                          INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }
       
        final CoordinatesType coord = gmlLine.getCoordinates();
        String s = coord.getValue();
        double x1, x2, y1, y2;
        
        x1 = Double.parseDouble(s.substring(0, s.indexOf(coord.getCs())));
        
        s = s.substring(s.indexOf(coord.getCs()) + 1);
        
        y1 = Double.parseDouble(s.substring(0, s.indexOf(coord.getTs())));
        
        s = s.substring(s.indexOf(coord.getTs()) + 1);
        
        x2 = Double.parseDouble(s.substring(0, s.indexOf(coord.getCs())));
        
        s = s.substring(s.indexOf(coord.getCs()) + 1);
        
        y2 = Double.parseDouble(s);

        final Line2D line = new Line2D.Double(x1, y1, x2, y2);
        
        // TODO CoordinateReferenceSystem crs = CRS.decode(CRSName, true);
        
        return line;
    }
    
    /**
     * Transform A GML envelope into a treatable geometric object : GeneralEnvelope
     * 
     * @param GMLenvelope A GML envelope.
     * 
     * @return A general Envelope. 
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     */
    public static GeneralEnvelope gmlEnvelopeToGeneralEnvelope(EnvelopeEntry gmlEnvelope) throws NoSuchAuthorityCodeException, FactoryException, CstlServiceException {
        final String crsName = gmlEnvelope.getSrsName();
        if (crsName == null) {
            throw new CstlServiceException("An operator BBOX must specified a CRS (coordinate Reference system) for the envelope.",
                                          INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }
       
        final List<Double> lmin = gmlEnvelope.getLowerCorner().getValue();
        final double[] min      = new double[lmin.size()];
        for (int i = 0; i < min.length; i++) {
            min[i] = lmin.get(i);
        }

        final List<Double> lmax = gmlEnvelope.getUpperCorner().getValue();
        final double[] max = new double[lmax.size()];
        for (int i = 0; i < min.length; i++) {
            max[i] = lmax.get(i);
        }

        final GeneralEnvelope envelopeF     = new GeneralEnvelope(min, max);
        final CoordinateReferenceSystem crs = CRS.decode(crsName, true);
        envelopeF.setCoordinateReferenceSystem(crs);
        return envelopeF;
    }
    
     /**
     * Transform A GML point into a treatable geometric object : GeneralDirectPosition
     * 
     * @param GMLpoint The GML point to transform.
     * 
     * @return A GeneralDirectPosition.
     * 
     * @throws org.constellation.coverage.web.CstlServiceException
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     */
    protected GeneralDirectPosition gmlPointToGeneralDirectPosition(PointType gmlPoint) throws CstlServiceException, NoSuchAuthorityCodeException, FactoryException {
        
        final String crsName = gmlPoint.getSrsName();

        if (crsName == null) {
            throw new CstlServiceException("A GML point must specify Coordinate Reference System.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }

        //we get the coordinate of the point (if they are present)
        if (gmlPoint.getCoordinates() == null && gmlPoint.getPos() == null) {
            throw new CstlServiceException("A GML point must specify coordinates or direct position.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }

        final double[] coordinates = new double[2];
        if (gmlPoint.getCoordinates() != null) {
            final String coord = gmlPoint.getCoordinates().getValue();
       
            final StringTokenizer tokens = new StringTokenizer(coord, " ");
            int index = 0;
            while (tokens.hasMoreTokens()) {
                final double value = parseDouble(tokens.nextToken());
                if (index >= coordinates.length) {
                    throw new CstlServiceException("This service support only 2D point.",
                            INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }
                coordinates[index++] = value;
            }
        } else if (gmlPoint.getPos().getValue() != null && gmlPoint.getPos().getValue().size() == 2){
            coordinates[0] = gmlPoint.getPos().getValue().get(0);
            coordinates[0] = gmlPoint.getPos().getValue().get(1);
        } else {
            throw new CstlServiceException("The GML point is malformed.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }
        final GeneralDirectPosition point   = new GeneralDirectPosition(coordinates);
        final CoordinateReferenceSystem crs = CRS.decode(crsName, true);
        point.setCoordinateReferenceSystem(crs);
        return point;    
    }
    
    /**
     * Parses a value as a floating point.
     *
     * @throws CstlServiceException if the value can't be parsed.
     */
    private double parseDouble(String value) throws CstlServiceException {
        value = value.trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new CstlServiceException("The value:" + value + " is not a valid double coordinate.",
                                         INVALID_PARAMETER_VALUE, "Coordinates");
        }
    }
    
    /**
     * Return a Date by parsing different kind of date format.
     * 
     * @param date a date representation (example 2002, 02-2007, 2004-03-04, ...)
     * 
     * @return a formated date (example 2002 -> 01-01-2002,  2004-03-04 -> 04-03-2004, ...) 
     */
    @Deprecated
    protected String createDate(String date) throws ParseException {
        
        final Map<String, String> monthPOOL = new HashMap<String, String>();
        //french lowerCase
        monthPOOL.put("janvier",   "01");
        monthPOOL.put("février",   "02");
        monthPOOL.put("mars",      "03");
        monthPOOL.put("avril",     "04");
        monthPOOL.put("mai",       "05");
        monthPOOL.put("juin",      "06");
        monthPOOL.put("juillet",   "07");
        monthPOOL.put("août",      "08");
        monthPOOL.put("septembre", "09");
        monthPOOL.put("octobre",   "10");
        monthPOOL.put("novembre",  "11");
        monthPOOL.put("décembre",  "12");
        //french first upperCase
        monthPOOL.put("Janvier",   "01");
        monthPOOL.put("Février",   "02");
        monthPOOL.put("Mars",      "03");
        monthPOOL.put("Avril",     "04");
        monthPOOL.put("Mai",       "05");
        monthPOOL.put("Juin",      "06");
        monthPOOL.put("Juillet",   "07");
        monthPOOL.put("Août",      "08");
        monthPOOL.put("Septembre", "09");
        monthPOOL.put("Octobre",   "10");
        monthPOOL.put("Novembre",  "11");
        monthPOOL.put("Décembre",  "12");
        
         //english first upperCase + cut
        monthPOOL.put("Jan",       "01");
        monthPOOL.put("Feb",       "02");
        monthPOOL.put("Mar",       "03");
        monthPOOL.put("Apr",       "04");
        monthPOOL.put("May",       "05");
        monthPOOL.put("Jun",       "06");
        monthPOOL.put("Jul",       "07");
        monthPOOL.put("Aug",       "08");
        monthPOOL.put("Sep",       "09");
        monthPOOL.put("Oct",       "10");
        monthPOOL.put("Nov",       "11");
        monthPOOL.put("Dec",       "12");
        
        
        String year;
        String month;
        String day;
        String tmp = "1970-01-01";
        if (date != null){
            if(date.contains("/")){
                
                day   = date.substring(0, date.indexOf('/'));
                date  = date.substring(date.indexOf('/') + 1);
                month = date.substring(0, date.indexOf('/'));
                year  = date.substring(date.indexOf('/') + 1);
                                
                tmp   = year + "-" + month + "-" + day;
            } else if ( getOccurence(date, " ") == 2 ) {
                if (! date.contains("?")){
                               
                    day    = date.substring(0, date.indexOf(' '));
                    date   = date.substring(date.indexOf(' ') + 1);
                    month  = monthPOOL.get(date.substring(0, date.indexOf(' ')));
                    year   = date.substring(date.indexOf(' ')+1);

                    tmp    = day + "-" + month + "-" + year;
                } else tmp = "2000-01-01";
                
            } else if ( getOccurence(date, " ") == 1 ) {
                
                month = monthPOOL.get(date.substring(0, date.indexOf(' ')));
                year  = date.substring(date.indexOf(' ') + 1);
               tmp   = year + "-" + month + "-01";
                
            } else if ( getOccurence(date, "-") == 1 ) {
                
                month = date.substring(0, date.indexOf('-'));
                year  = date.substring(date.indexOf('-') + 1);
                                
                tmp   = year + "-" + month + "-01";
                
            } else if ( getOccurence(date, "-") == 2 ) {
                
                //if date is in format yyyy-mm-dd
                if (date.substring(0, date.indexOf('-')).length()==4){
                    year  = date.substring(0, date.indexOf('-'));
                    date  = date.substring(date.indexOf('-') + 1);
                    month = date.substring(0, date.indexOf('-'));
                    day   = date.substring(date.indexOf('-') + 1);
                    
                    tmp   = year + "-" + month + "-" + day;
                }
                else{
                    day   = date.substring(0, date.indexOf('-'));
                    date  = date.substring(date.indexOf('-') + 1);
                    month = date.substring(0, date.indexOf('-'));
                    year  = date.substring(date.indexOf('-') + 1);
                    
                    tmp   = year + "-" + month + "-" + day;
                }
                
            } else if ( getOccurence(date, "CEST") == 1) {
                year  = date.substring(date.indexOf("CEST") + 4);
                month = monthPOOL.get(date.substring(4, 7));
                day   = date.substring(8, 10);
                tmp   = year + "-" + month + "-" + day;
            } else {
                year = date;
                tmp  =  year + "-01-01";
            }
        }
        return tmp;
    }
    
    /**
     * This method returns a number of occurences occ in the string s.
     * 
     * example getOccurence( "hello, welcome to hell", "hell") returns 2.
     * example getOccurence( "hello, welcome to hell", "o")    returns 3. 
     * 
     * @param s   A character String.
     * @param occ A character String.
     * 
     * @return The number of occurence of the string occ in the string s.
     */
    protected int getOccurence (String s, String occ){
        if (! s.contains(occ))
            return 0;
        else {
            int nbocc = 0;
            while(s.indexOf(occ) != -1){
                s = s.substring(s.indexOf(occ)+1);
                nbocc++;
            }
            return nbocc;
        }
    }
    
    
    
    public abstract Object getQuery(final QueryConstraint constraint, Map<String, QName> variables, Map<String, String> prefixs) throws CstlServiceException;
    
    protected abstract Object treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws CstlServiceException;
    
    protected abstract Object treatComparisonOperator(final JAXBElement<? extends ComparisonOpsType> jbComparisonOps) throws CstlServiceException;

    /**
     * Extract a OCG filter from the query constraint of the received request.
     * 
     * @param constraint
     * @return
     * @throws CstlServiceException
     */
    protected FilterType getFilterFromConstraint(final QueryConstraint constraint) throws CstlServiceException {
        
        //The null case must be trreated before calling this method
        if (constraint == null)  {
            throw new IllegalArgumentException("The null case must be already treated!");

        // both constraint type are filled we throw an exception
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new CstlServiceException("The query constraint must be in Filter or CQL but not both.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        
        // none constraint type are filled we throw an exception
        } else if (constraint.getCqlText() == null && constraint.getFilter() == null) {
            throw new CstlServiceException("The query constraint must contain a Filter or a CQL query.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        
        // for a CQL request we transform it in Filter
        } else if (constraint.getCqlText() != null) {
            try {
                return cqlToFilter(constraint.getCqlText());

            } catch (JAXBException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new CstlServiceException("JAXBException while parsing CQL query: " + ex.getMessage(), NO_APPLICABLE_CODE, QUERY_CONSTRAINT);
            } catch (CQLException ex) {
                throw new CstlServiceException("The CQL query is malformed: " + ex.getMessage() + '\n'
                                                 + "syntax Error: " + ex.getSyntaxError(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            
        // for a filter we return directly it
        } else {
            return constraint.getFilter();
        }
    }

    /**
     * 
     * @param operator
     * @param filters
     * @param query
     * @return
     */
    protected Filter getSpatialFilterFromList(int logicalOperand, final List<Filter> filters, String query) {

        Filter spatialFilter = null;
        if (filters.size() == 1) {

            if (logicalOperand == SerialChainFilter.NOT) {
                final int[] filterType = {SerialChainFilter.NOT};
                spatialFilter = new SerialChainFilter(filters, filterType);
            } else {
                spatialFilter = filters.get(0);
            }

        } else if (filters.size() > 1) {

            final int[] filterType = new int[filters.size() - 1];
            for (int i = 0; i < filterType.length; i++) {
                filterType[i] = logicalOperand;
            }
            spatialFilter = new SerialChainFilter(filters, filterType);
        }
        return spatialFilter;
    }

    /**
     * Build a piece of lucene query with the specified Spatial filter.
     *
     * @param JBlogicOps
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> jbSpatialOps) throws CstlServiceException {
        LuceneOGCFilter spatialfilter = null;
        final SpatialOpsType spatialOps   = jbSpatialOps.getValue();

        if (spatialOps instanceof BBOXType) {
            final BBOXType bbox       = (BBOXType) spatialOps;
            final String propertyName = bbox.getPropertyName();
            final String crsName      = bbox.getSRS();

            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new CstlServiceException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else if (!propertyName.contains("BoundingBox")) {
                throw new CstlServiceException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new CstlServiceException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (crsName == null) {
                throw new CstlServiceException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            //we transform the EnvelopeEntry in GeneralEnvelope
            spatialfilter = wrap(FF.bbox(GEOMETRY_PROPERTY, bbox.getMinX(), bbox.getMinY(),bbox.getMaxX(),bbox.getMaxY(),crsName));

        } else if (spatialOps instanceof DistanceBufferType) {

            final DistanceBufferType dist = (DistanceBufferType) spatialOps;
            final double distance         = dist.getDistance();
            final String units            = dist.getDistanceUnits();
            final JAXBElement jbGeom      = dist.getAbstractGeometry();
            final String operator         = jbSpatialOps.getName().getLocalPart();

            //we verify that all the parameters are specified
            if (dist.getPropertyName() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (units == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (jbGeom == null || jbGeom.getValue() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified a geometric object.",
                                                  INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            final Object gml = jbGeom.getValue();
            Geometry geometry = null;
            //String propName  = dist.getPropertyName().getPropertyName();
            String crsName   = null;

            // we transform the gml geometry in treatable geometry
            try {
                if (gml instanceof PointType) {
                    final PointType gmlPoint = (PointType) gml;
                    crsName  = gmlPoint.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlPoint);

                } else if (gml instanceof LineStringType) {
                    final LineStringType gmlLine =  (LineStringType) gml;
                    crsName  = gmlLine.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlLine);

                } else if (gml instanceof EnvelopeEntry) {
                    final EnvelopeEntry gmlEnvelope = (EnvelopeEntry) gml;
                    crsName  = gmlEnvelope.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlEnvelope);
                }

                if (operator.equals("DWithin")) {
                    spatialfilter = wrap(FF.dwithin(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else if (operator.equals("Beyond")) {
                    spatialfilter = wrap(FF.beyond(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else {
                    throw new CstlServiceException("Unknow DistanceBuffer operator.",
                            INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }

            } catch (NoSuchAuthorityCodeException e) {
                    throw new CstlServiceException(UNKNOW_CRS_ERROR_MSG + crsName,
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                    throw new CstlServiceException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                    throw new CstlServiceException(INCORRECT_BBOX_DIM_ERROR_MSG+ e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

        } else if (spatialOps instanceof BinarySpatialOpType) {

            final BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;

            String propertyName = null;
            String operator     = jbSpatialOps.getName().getLocalPart();
            operator            = operator.toUpperCase();
            Object gmlGeometry     = null;

            // the propertyName
            if (binSpatial.getPropertyName() != null && binSpatial.getPropertyName().getValue() != null) {
                final PropertyNameType p = binSpatial.getPropertyName().getValue();
                propertyName = p.getContent();
            }

            // geometric object: envelope
            if (binSpatial.getEnvelope() != null && binSpatial.getEnvelope().getValue() != null) {
                gmlGeometry = binSpatial.getEnvelope().getValue();
            }


            if (binSpatial.getAbstractGeometry() != null && binSpatial.getAbstractGeometry().getValue() != null) {
                final AbstractGeometryType ab =  binSpatial.getAbstractGeometry().getValue();

                // geometric object: point
                if (ab instanceof PointType) {
                    gmlGeometry     = (PointType) ab;

                // geometric object: Line
                } else if (ab instanceof LineStringType) {
                    gmlGeometry     = (LineStringType) ab;

                } else if (ab == null) {
                   throw new IllegalArgumentException("null value in BinarySpatialOp type");

                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + ab.getClass().getSimpleName());
                }
            }

            if (propertyName == null && gmlGeometry == null) {
                throw new CstlServiceException("An Binarary spatial operator must specified a propertyName and a geometry.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            SpatialFilterType filterType = null;
            try {
                filterType = SpatialFilterType.valueOf(operator);
            } catch (IllegalArgumentException ex) {
                LOGGER.severe("unknow spatial filter Type");
            }
            if (filterType == null) {
                throw new CstlServiceException("Unknow FilterType: " + operator,
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            String crsName = "undefined CRS";
            try {
                Geometry filterGeometry = null;
                if (gmlGeometry instanceof EnvelopeEntry) {

                    //we transform the EnvelopeEntry in GeneralEnvelope
                    final EnvelopeEntry gmlEnvelope = (EnvelopeEntry)gmlGeometry;
                    crsName                   = gmlEnvelope.getSrsName();
                    filterGeometry            = GeometrytoJTS.toJTS(gmlEnvelope);

                } else if (gmlGeometry instanceof PointType) {
                    final PointType gmlPoint  = (PointType) gmlGeometry;
                    crsName                   = gmlPoint.getSrsName();
                    filterGeometry            = GeometrytoJTS.toJTS(gmlPoint);

                } else if (gmlGeometry instanceof LineStringType) {
                    final LineStringType gmlLine =  (LineStringType) gmlGeometry;
                    crsName                = gmlLine.getSrsName();
                    filterGeometry         = GeometrytoJTS.toJTS(gmlLine);
                }

                final int srid = SRIDGenerator.toSRID(crsName, Version.V1);
                filterGeometry.setSRID(srid);

                switch (filterType) {
                    case CONTAINS   : spatialfilter = wrap(FF.contains(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case CROSSES    : spatialfilter = wrap(FF.crosses(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case DISJOINT   : spatialfilter = wrap(FF.disjoint(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case EQUALS     : spatialfilter = wrap(FF.equal(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case INTERSECTS : spatialfilter = wrap(FF.intersects(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case OVERLAPS   : spatialfilter = wrap(FF.overlaps(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case TOUCHES    : spatialfilter = wrap(FF.touches(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    case WITHIN     : spatialfilter = wrap(FF.within(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                    default         : LOGGER.info("using default filter within");
                                      spatialfilter = wrap(FF.within(GEOMETRY_PROPERTY, FF.literal(filterGeometry))); break;
                }

            } catch (NoSuchAuthorityCodeException e) {
                throw new CstlServiceException(UNKNOW_CRS_ERROR_MSG + crsName,
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                throw new CstlServiceException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                throw new CstlServiceException(INCORRECT_BBOX_DIM_ERROR_MSG + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

        }

        return spatialfilter;
    }
    

    protected boolean isDateField(String propertyName) {
        if (propertyName != null) {
            return (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")
                     || propertyName.equalsIgnoreCase("TempExtent_begin") || propertyName.equalsIgnoreCase("TempExtent_end"));
        }
        return false;
    }
}
