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

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Apache Lucene dependencies
import org.apache.lucene.search.Filter;

// constellation dependencies
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.filter.FilterFactoryImpl;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Geotools dependencies
import org.geotoolkit.filter.text.cql2.CQL;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.gml.xml.v311modified.CoordinatesType;
import org.geotoolkit.gml.xml.v311modified.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311modified.LineStringType;
import org.geotoolkit.gml.xml.v311modified.PointType;
import org.geotoolkit.ogc.xml.v110modified.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110modified.FilterType;
import org.geotoolkit.ogc.xml.v110modified.LogicOpsType;
import org.geotoolkit.ogc.xml.v110modified.SpatialOpsType;
import org.geotoolkit.referencing.CRS;

// GeoAPI dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Guilhem Legal
 */
public abstract class FilterParser {
    
     /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");
    
   /**
     * Build a Filter with the specified CQL query
     * 
     * @param cqlQuery A well-formed CQL query .
     */
    public static FilterType cqlToFilter(String cqlQuery) throws CQLException, JAXBException {
        FilterType result;
        final Object newFilter = CQL.toFilter(cqlQuery, new FilterFactoryImpl());
        /*
         * here we put a temporary patch consisting in using the geotools filterFactory implementation
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
                                          INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                                          INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                    INVALID_PARAMETER_VALUE, "QueryConstraint");
        }

        //we get the coordinate of the point (if they are present)
        if (gmlPoint.getCoordinates() == null && gmlPoint.getPos() == null) {
            throw new CstlServiceException("A GML point must specify coordinates or direct position.",
                    INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                            INVALID_PARAMETER_VALUE, "QueryConstraint");
                }
                coordinates[index++] = value;
            }
        } else if (gmlPoint.getPos().getValue() != null && gmlPoint.getPos().getValue().size() == 2){
            coordinates[0] = gmlPoint.getPos().getValue().get(0);
            coordinates[0] = gmlPoint.getPos().getValue().get(1);
        } else {
            throw new CstlServiceException("The GML point is malformed.",
                    INVALID_PARAMETER_VALUE, "QueryConstraint");
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
    
    protected abstract Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> jbSpatialOps) throws CstlServiceException;
    

}
