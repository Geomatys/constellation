/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.filter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;
import net.seagis.cat.csw.QueryConstraintType;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.ogc.AbstractIdType;
import net.seagis.ogc.BBOXType;
import net.seagis.ogc.BinaryComparisonOpType;
import net.seagis.ogc.BinaryLogicOpType;
import net.seagis.ogc.BinarySpatialOpType;
import net.seagis.ogc.ComparisonOpsType;
import net.seagis.ogc.DistanceBufferType;
import net.seagis.ogc.FilterType;
import net.seagis.ogc.LiteralType;
import net.seagis.ogc.LogicOpsType;
import net.seagis.ogc.PropertyIsBetweenType;
import net.seagis.ogc.PropertyIsLikeType;
import net.seagis.ogc.PropertyIsNullType;
import net.seagis.ogc.PropertyNameType;
import net.seagis.ogc.SpatialOpsType;
import net.seagis.ogc.UnaryLogicOpType;
import net.seagis.ows.v100.OWSWebServiceException;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.text.cql2.CQL;
import static net.seagis.ows.OWSExceptionCode.*;
import org.geotools.filter.text.cql2.CQLException;

/**
 * A parser for filter 1.1.0 and CQL 2.0
 * 
 * @author Guilhem Legal
 */
public class FilterParser {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("net.seagis.filter");
    
     /**
     * The version of the service
     */
    private ServiceVersion version;
    
    /**
     * A temporary marshaller whitch will be replaced by FilterFactoryImpl
     */
    private Marshaller filterMarshaller;
    
    /**
     * Build a new FilterParser with the specified version.
     */
    public FilterParser(ServiceVersion version) throws JAXBException {
        this.version = version;
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.ogc:net.seagis.gml");
        filterMarshaller = jbcontext.createMarshaller();
        filterMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }
    
    /**
     * Build a lucene request from the specified constraint
     * 
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public String getLuceneQuery(QueryConstraintType constraint) throws WebServiceException {
        FilterType filter = null;
        if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new OWSWebServiceException("The query constraint must be in Filter or CQL but not both.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }
        if (constraint.getCqlText() != null) {
            try {
                String query = constraint.getCqlText();
                Object newFilter = CQL.toFilter(query, new FilterFactoryImpl());
                /*
                 * here we put a temporary patch consisting in using the geotools filterFactory implementation
                 * instead of our own implementation.
                 * The we unmarshaller the xml to get a seagis Filter object.
                 *
                 *
                 * File f = File.createTempFile("CQL", "query");
                 * FileWriter fw = new FileWriter(f);
                 * new FilterTransformer().transform(newFilter, fw);
                 * fw.close();
                 * JAXBElement jb = (JAXBElement) filterUnMarshaller.unmarshal(f);
                 */
               
                if (!(newFilter instanceof FilterType)) {
                    filter = new FilterType(newFilter);
                } else {
                    filter = (FilterType) newFilter;
                }
                filterMarshaller.marshal(filter, System.out);
                 
            } catch (JAXBException ex) {
                ex.printStackTrace();
                throw new OWSWebServiceException("JAXBException while parsing CQL query: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE, "QueryConstraint", version);
                
            /*} catch (TransformerException ex) {
                throw new OWSWebServiceException("TransformerException while parsing CQL query: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE, "QueryConstraint", version);
            } catch (IOException ex) {
                throw new OWSWebServiceException("IO exception while parsing CQL query: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE, "QueryConstraint", version);*/
            } catch (CQLException ex) {
                throw new OWSWebServiceException("The CQL query is malformed: " + ex.getMessage() + '\n' 
                                                 + "syntax Error: " + ex.getSyntaxError(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
        } else if (constraint.getFilter() != null) {
            filter = constraint.getFilter();
            
        }
        return getLuceneQuery(filter);
    }
     /**
     * Build a lucene request from the specified Filter
     * 
     * @param filter a Filter object build directly from the XML or from a CQL request
     */
    public String getLuceneQuery(FilterType filter) throws WebServiceException {
        
        StringBuilder query = new StringBuilder("");
            
        // we treat logical Operators like AND, OR, ...
        if (filter.getLogicOps() != null) {
            query.append(treatLogicalOperator(filter.getLogicOps()));
            
        // we treat directly comparison operator: PropertyIsLike, IsNull, ISBetween,...    
        } else if (filter.getComparisonOps() != null) {
            query.append(treatComparisonOperator(filter.getComparisonOps()));
                
        // we treat spatial constraint    
        } else if (filter.getSpatialOps() != null) {
            query.append(treatSpatialOperator(filter.getSpatialOps()));
                
        } else if (filter.getId() != null) {
            query.append(treatIDOperator(filter.getId()));
        }  
        
        return query.toString();
    }
    
    /**
     * Build a piece of lucene query with the specified Logical filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private String treatLogicalOperator(JAXBElement<? extends LogicOpsType> JBlogicOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        LogicOpsType logicOps = JBlogicOps.getValue();
        String operator = JBlogicOps.getName().getLocalPart();
        
        if (logicOps instanceof BinaryLogicOpType) {
            BinaryLogicOpType binary = (BinaryLogicOpType) logicOps;
            response.append('(');
            for (JAXBElement<?> jb: binary.getOperators()) {
                
                if (jb.getValue() instanceof LogicOpsType) {
                    
                    response.append(treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb));
                    
                } else if (jb.getValue() instanceof ComparisonOpsType) {
                    
                    response.append(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
                
                } else if (jb.getValue() instanceof SpatialOpsType) {
                    
                   response.append(treatSpatialOperator((JAXBElement<? extends SpatialOpsType>)jb)); 
                    
                } else {
                    
                    throw new IllegalArgumentException("unknow BinaryLogicalOp:" + jb.getValue().getClass().getSimpleName()); 
                }
                
                response.append(" ").append(operator.toUpperCase()).append(" ");
            }
          
          // we remove the last Operator and add a )  
          response.delete(response.length()- (operator.length() + 2), response.length());
          response.append(')');
                
        } else if (logicOps instanceof UnaryLogicOpType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        return response.toString();
    }
    
    /**
     * Build a piece of lucene query with the specified Comparison filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private String treatComparisonOperator(JAXBElement<? extends ComparisonOpsType> JBComparisonOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        ComparisonOpsType comparisonOps = JBComparisonOps.getValue();
        
        if (comparisonOps instanceof PropertyIsLikeType ) {
            PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            
            //we get the field
            if (pil.getPropertyName() != null) {
                response.append(removePrefix(pil.getPropertyName().getContent())).append(':');
            } else {
                throw new OWSWebServiceException("An operator propertyIsLike must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //we get the value of the field
            if (pil.getLiteral() != null && pil.getLiteral() != null) {
                
                //we format the value by replacing the specified special char by the lucene special char
                String brutValue = pil.getLiteral();
                brutValue = brutValue.replace(pil.getWildCard(),    "*");
                brutValue = brutValue.replace(pil.getSingleChar(),  "?");
                brutValue = brutValue.replace(pil.getEscapeChar(),  "\\");
                
                // lucene does not accept '*' on first character
                while (brutValue.charAt(0) == '*') {
                    brutValue = brutValue.substring(1);
                }
                response.append(brutValue);
                
            } else {
                throw new OWSWebServiceException("An operator propertyIsLike must specified the literal value.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
        } else if (comparisonOps instanceof PropertyIsNullType) {
             PropertyIsNullType pin = (PropertyIsNullType) comparisonOps;

            //we get the field
            if (pin.getPropertyName() != null) {
                response.append(removePrefix(pin.getPropertyName().getContent())).append(':').append("null");
            } else {
                throw new OWSWebServiceException("An operator propertyIsNull must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
        } else if (comparisonOps instanceof PropertyIsBetweenType) {
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
        
        } else if (comparisonOps instanceof BinaryComparisonOpType) {
            
            BinaryComparisonOpType bc = (BinaryComparisonOpType) comparisonOps;
            String propertyName       = null;
            LiteralType literal       = null;
            String operator           = JBComparisonOps.getName().getLocalPart(); 
            for (Object obj: bc.getExpressionOrLiteralOrPropertyName()) {
                if (obj instanceof LiteralType) {
                    literal      = (LiteralType) obj;
                } else if (obj instanceof String) {
                    propertyName = (String) obj;
                } else  if (obj instanceof PropertyNameType) {
                    propertyName = ((PropertyNameType) obj).getPropertyName();
                } else {
                    throw new IllegalArgumentException("BinaryComparisonOpType parameter not known: " + obj.getClass().getSimpleName());
                }
            }
            if (propertyName == null || literal == null) {
                throw new OWSWebServiceException("A binary comparison operator must be constitued of a literal and a property name.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } else {
                if (operator.equals("PropertyIsEqualTo")) {                
                    response.append(removePrefix(propertyName)).append(":\"").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsNotEqualTo")) {
                    
                   response.append("metafile:doc NOT ");
                   response.append(removePrefix(propertyName)).append(":").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsGreaterThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replace("-", "");
                        response.append(removePrefix(propertyName)).append(":[").append(dateValue).append(' ').append(" 30000101]");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replace("-", "");
                        response.append(removePrefix(propertyName)).append(":{").append(dateValue).append(' ').append(" 30000101}");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                
                } else if (operator.equals("PropertyIsLessThan") ) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        //if we are passed by CQL we must format the date
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replace("-", "");
                        response.append(removePrefix(propertyName)).append(":{00000101").append(' ').append(dateValue).append("}");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replace("-", "");
                        response.append(removePrefix(propertyName)).append(":[00000101").append(' ').append(dateValue).append("]");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                } else {
                    throw new OWSWebServiceException("Unkwnow comparison operator: " + operator,
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                }
            }
        }
        return response.toString();
    }
    
    /**
     * Build a piece of lucene query with the specified Spatial filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private String treatSpatialOperator(JAXBElement<? extends SpatialOpsType> JBSpatialOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        SpatialOpsType spatialOps = JBSpatialOps.getValue();
        
        if (spatialOps instanceof BBOXType) {
            BBOXType bbox       = (BBOXType) spatialOps;
            String propertyName = bbox.getPropertyName();
            if (propertyName == null) {
                throw new OWSWebServiceException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new OWSWebServiceException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
            
        } else if (spatialOps instanceof DistanceBufferType) {
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
            
        } else if (spatialOps instanceof BinarySpatialOpType) {
            
            BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;
            List<JAXBElement<?>> objects   = binSpatial.getAbstractGeometryOrAbstractGeometricPrimitiveOrPoint();
            
            String propertyName    = null;
            String operator        = JBSpatialOps.getName().getLocalPart();
            EnvelopeEntry envelope = null;
            
            for (JAXBElement<?> jb: objects) {
                if (jb.getValue() instanceof PropertyNameType) {
                    PropertyNameType p = (PropertyNameType) jb.getValue();
                    propertyName = p.getContent();
                } else if (jb.getValue() instanceof EnvelopeEntry) {
                    envelope     = (EnvelopeEntry) jb.getValue();
                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + jb.getValue().getClass().getSimpleName());
                }
            }
            
            if (propertyName == null && envelope == null) {
                throw new OWSWebServiceException("An Binarary spatial operator must specified a propertyName and an envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
            
        }
        
        return response.toString();
    }
    
    private String treatIDOperator(List<JAXBElement<? extends AbstractIdType>> JBIdsOps) {
        StringBuilder response = new StringBuilder();
        
        //TODO
        if (true)
            throw new UnsupportedOperationException("Not supported yet.");
            
        return response.toString();
    }
    
    /**
     * Remove the prefix on propertyName.
     */
    private String removePrefix(String s) {
        int i = s.indexOf(':');
        if ( i != -1) {
            s = s.substring(i + 1, s.length());
        }
        return s;
    }
    
    /**
     * Return a Date by parsing different kind of date format.
     * 
     * @param date a date representation (example 2002, 02-2007, 2004-03-04, ...)
     * 
     * @return a formated date (example 2002 -> 01-01-2002,  2004-03-04 -> 04-03-2004, ...) 
     */
    private String createDate(String date) throws ParseException {
        
        Map<String, String> monthPOOL = new HashMap<String, String>();
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
                
                day   = date.substring(0, date.indexOf("/"));
                date  = date.substring(date.indexOf("/")+1);
                month = date.substring(0, date.indexOf("/"));
                year  = date.substring(date.indexOf("/")+1);
                                
                tmp   = year + "-" + month + "-" + day;
            } else if ( getOccurence(date, " ") == 2 ) {
                if (! date.contains("?")){
                               
                    day    = date.substring(0, date.indexOf(" "));
                    date   = date.substring(date.indexOf(" ")+1);
                    month  = monthPOOL.get(date.substring(0, date.indexOf(" ")));
                    year   = date.substring(date.indexOf(" ")+1);

                    tmp    = day+"-"+month+"-"+year;
                } else tmp = "2000-01-01";
                
            } else if ( getOccurence(date, " ") == 1 ) {
                
                month = monthPOOL.get(date.substring(0, date.indexOf(" ")));
                year  = date.substring(date.indexOf(" ") + 1);   
               tmp   = year + "-" + month + "-01";
                
            } else if ( getOccurence(date, "-") == 1 ) {
                
                month = date.substring(0, date.indexOf("-"));
                year  = date.substring(date.indexOf("-")+1);
                                
                tmp   = year + "-" + month + "-01";
                
            } else if ( getOccurence(date, "-") == 2 ) {
                
                //if date is in format yyyy-mm-dd
                if (date.substring(0, date.indexOf("-")).length()==4){
                    year  = date.substring(0, date.indexOf("-"));
                    date  = date.substring(date.indexOf("-")+1);
                    month = date.substring(0, date.indexOf("-"));
                    day   = date.substring(date.indexOf("-")+1);
                    
                    tmp   = year + "-" + month + "-" + day;
                }
                else{
                    day   = date.substring(0, date.indexOf("-"));
                    date  = date.substring(date.indexOf("-")+1);
                    month = date.substring(0, date.indexOf("-"));
                    year  = date.substring(date.indexOf("-")+1);
                    
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
     */
    private int getOccurence (String s, String occ){
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

}
