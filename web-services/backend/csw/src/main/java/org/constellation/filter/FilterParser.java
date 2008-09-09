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

// J2SE dependencies
import java.awt.geom.Line2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// Constellation dependencies
import org.constellation.cat.csw.v202.QueryConstraintType;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.gml.v311.CoordinatesType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.LineStringType;
import org.constellation.gml.v311.PointType;
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialFilter;
import org.constellation.lucene.filter.SpatialQuery;
import org.constellation.ogc.AbstractIdType;
import org.constellation.ogc.BBOXType;
import org.constellation.ogc.BinaryComparisonOpType;
import org.constellation.ogc.BinaryLogicOpType;
import org.constellation.ogc.BinarySpatialOpType;
import org.constellation.ogc.ComparisonOpsType;
import org.constellation.ogc.DistanceBufferType;
import org.constellation.ogc.FilterType;
import org.constellation.ogc.LiteralType;
import org.constellation.ogc.LogicOpsType;
import org.constellation.ogc.PropertyIsBetweenType;
import org.constellation.ogc.PropertyIsLikeType;
import org.constellation.ogc.PropertyIsNullType;
import org.constellation.ogc.PropertyNameType;
import org.constellation.ogc.SpatialOpsType;
import org.constellation.ogc.UnaryLogicOpType;
import org.constellation.ows.v100.OWSWebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

// Lucene dependencies
import org.apache.lucene.search.Filter;

// geotools dependencies
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;

// GeoAPI dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A parser for filter 1.1.0 and CQL 2.0
 * 
 * @author Guilhem Legal
 */
public class FilterParser {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("org.constellation.filter");
    
     /**
     * The version of the service
     */
    private final ServiceVersion version;
    
    /**
     * A temporary marshaller whitch will be replaced by FilterFactoryImpl
     */
    private final Marshaller filterMarshaller;
    
    /**
     * Build a new FilterParser with the specified version.
     */
    public FilterParser(final ServiceVersion version) throws JAXBException {
        if (version == null) {
            // TODO restore this throw new IllegalArgumentException("version must not null");
            this.version = new ServiceVersion(Service.OWS, "2.0.2");
        } else {
            this.version = version;
        }
            
        JAXBContext jbcontext = JAXBContext.newInstance("org.constellation.ogc:org.constellation.gml.v311");
        filterMarshaller = jbcontext.createMarshaller();
        filterMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }
    
    /**
     * Build a lucene request from the specified constraint
     * 
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public SpatialQuery getLuceneQuery(final QueryConstraintType constraint) throws WebServiceException {
        FilterType filter = null;
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            Filter nullFilter = null;
            return new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
            
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new OWSWebServiceException("The query constraint must be in Filter or CQL but not both.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        } else if (constraint.getCqlText() == null && constraint.getFilter() == null) {
            throw new OWSWebServiceException("The query constraint must contain a Filter or a CQL query.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }
        
        if (constraint.getCqlText() != null) {
            try {
                filter = CQLtoFilter(constraint.getCqlText());
                 
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
     * Build a Filter with the specified CQL query
     * 
     * @param cqlQuery A well-formed CQL query .
     */
    public FilterType CQLtoFilter(String cqlQuery) throws CQLException, JAXBException {
        FilterType result;
        Object newFilter = CQL.toFilter(cqlQuery, new FilterFactoryImpl());
        /*
         * here we put a temporary patch consisting in using the geotools filterFactory implementation
         * instead of our own implementation.
         * Then we unmarshaller the xml to get a constellation Filter object.
         *
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
     * Build a lucene request from the specified Filter.
     * 
     * @param filter a Filter object build directly from the XML or from a CQL request
     */
    public SpatialQuery getLuceneQuery(final FilterType filter) throws WebServiceException {
        
        SpatialQuery response = null;
        //for ambigous purpose
        Filter nullFilter     = null;
        
        if (filter != null) { 
            // we treat logical Operators like AND, OR, ...
            if (filter.getLogicOps() != null) {
                response = treatLogicalOperator(filter.getLogicOps());
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...    
            } else if (filter.getComparisonOps() != null) {
                response = new SpatialQuery(treatComparisonOperator(filter.getComparisonOps()), nullFilter, SerialChainFilter.AND);
                
            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...    
            } else if (filter.getSpatialOps() != null) {
                response = new SpatialQuery("", treatSpatialOperator(filter.getSpatialOps()), SerialChainFilter.AND);
                
            } else if (filter.getId() != null) {
                response = new SpatialQuery(treatIDOperator(filter.getId()), nullFilter, SerialChainFilter.AND);
            }  
        }
        return response;
    }
    
    /**
     * Build a piece of lucene query with the specified Logical filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private SpatialQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> JBlogicOps) throws WebServiceException {
        List<SpatialQuery> subQueries = new ArrayList<SpatialQuery>();
        StringBuilder queryBuilder    = new StringBuilder();
        LogicOpsType logicOps         = JBlogicOps.getValue();
        String operator               = JBlogicOps.getName().getLocalPart();
        List<Filter> filters          = new ArrayList<Filter>();
        //for ambigous purpose
        Filter nullFilter             = null;
        
        if (logicOps instanceof BinaryLogicOpType) {
            BinaryLogicOpType binary = (BinaryLogicOpType) logicOps;
            queryBuilder.append('(');
            for (JAXBElement<?> jb: binary.getOperators()) {
                
                boolean writeOperator = true;
                
                // we treat logical Operators like AND, OR, ...
                if (jb.getValue() instanceof LogicOpsType) {
                    SpatialQuery sq  = treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb);
                    String subQuery  = sq.getQuery();
                    Filter subFilter = sq.getSpatialFilter();
                    
                    //if the sub spatial query contains both term search and spatial search we create a subQuery 
                    if ((subFilter != null && !subQuery.equals("")) 
                        || sq.getSubQueries().size() != 0 
                        || (sq.getLogicalOperator() == SerialChainFilter.NOT && sq.getSpatialFilter() == null)) {
                        
                        subQueries.add(sq);
                        writeOperator = false;
                    } else {
                        
                        if (subQuery.equals("")) {
                            writeOperator = false;
                        } else  {
                            queryBuilder.append(subQuery);
                        }
                        if (subFilter != null)
                            filters.add(subFilter);
                    }
                
                // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...        
                } else if (jb.getValue() instanceof ComparisonOpsType) {
                    
                    queryBuilder.append(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
                
                // we treat spatial constraint : BBOX, Beyond, Overlaps, ...        
                } else if (jb.getValue() instanceof SpatialOpsType) {
                   
                   //for the spatial filter we don't need to write into the lucene query 
                   filters.add(treatSpatialOperator((JAXBElement<? extends SpatialOpsType>)jb));
                   writeOperator = false;
                    
                } else {
                    
                    throw new IllegalArgumentException("unknow BinaryLogicalOp:" + jb.getValue().getClass().getSimpleName()); 
                }
                
                if (writeOperator) {
                    queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
                } else {
                    writeOperator = true;
                }
            }
          
          // we remove the last Operator and add a ') '
          int pos = queryBuilder.length()- (operator.length() + 2);
          if (pos > 0)
            queryBuilder.delete(queryBuilder.length()- (operator.length() + 2), queryBuilder.length());
          
          queryBuilder.append(')');
                
        } else if (logicOps instanceof UnaryLogicOpType) {
            UnaryLogicOpType unary = (UnaryLogicOpType) logicOps;
                       
                        
            // we treat comparison operator: PropertyIsLike, IsNull, IsBetween, ...    
            if (unary.getComparisonOps() != null) {
                queryBuilder.append(treatComparisonOperator(unary.getComparisonOps()));
                
            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...        
            } else if (unary.getSpatialOps() != null) {
                
                filters.add(treatSpatialOperator(unary.getSpatialOps()));
                
                
             // we treat logical Operators like AND, OR, ...
            } else if (unary.getLogicOps() != null) {
                SpatialQuery sq  = treatLogicalOperator(unary.getLogicOps());
                String subQuery  = sq.getQuery();
                Filter subFilter = sq.getSpatialFilter();
                    
                if ((sq.getLogicalOperator() == SerialChainFilter.OR && subFilter != null && !subQuery.equals("")) ||
                    (sq.getLogicalOperator() == SerialChainFilter.NOT)) {
                    subQueries.add(sq);
                   
                  } else {
                        
                        if (!subQuery.equals("")) {
                            queryBuilder.append(subQuery);
                        }
                        if (subFilter != null)
                            filters.add(sq.getSpatialFilter());
                  }
            }
        }
        
        int logicalOperand = SerialChainFilter.valueOf(operator);
        
        Filter spatialFilter = null;
        String query = queryBuilder.toString();
        if (query.equals("()"))
            query = "";
       
        if (filters.size() == 1) {
            
            if (logicalOperand == SerialChainFilter.NOT) {
                int filterType[] = {SerialChainFilter.NOT};
                spatialFilter = new SerialChainFilter(filters, filterType);
                if (query.equals("")) {
                    logicalOperand = SerialChainFilter.AND;
                } 
            } else {
                spatialFilter = filters.get(0);
            }
        
        } else if (filters.size() > 1) {
            
            int filterType[] = new int[filters.size() - 1];
            for (int i = 0; i < filterType.length; i++) {
                filterType[i] = logicalOperand;
            }
            spatialFilter = new SerialChainFilter(filters, filterType);
        }
        
            
        SpatialQuery response = new SpatialQuery(query, spatialFilter, logicalOperand);
        response.setSubQueries(subQueries);
        return response;
    }
    
    /**
     * Build a piece of lucene query with the specified Comparison filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private String treatComparisonOperator(final JAXBElement<? extends ComparisonOpsType> JBComparisonOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        ComparisonOpsType comparisonOps = JBComparisonOps.getValue();
        
        if (comparisonOps instanceof PropertyIsLikeType ) {
            PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            String propertyName    = "";
            //we get the field
            if (pil.getPropertyName() != null) {
                propertyName = pil.getPropertyName().getContent();
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
                
                //for a date we remove the '-'
                if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        brutValue = brutValue.replaceAll("-", "");
                        brutValue = brutValue.replace("Z", "");
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
                   response.append(removePrefix(propertyName)).append(":\"").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsGreaterThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":[").append(dateValue).append(' ').append(" 30000101]");
                    } else {
                        throw new OWSWebServiceException("PropertyIsGreaterThanOrEqualTo operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint", version);
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified") || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":{").append(dateValue).append(' ').append(" 30000101}");
                    } else {
                        throw new OWSWebServiceException("PropertyIsGreaterThan operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint", version);
                    }
                
                } else if (operator.equals("PropertyIsLessThan") ) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified") || propertyName.contains("date")) {
                        //if we are passed by CQL we must format the date
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":{00000101").append(' ').append(dateValue).append("}");
                    } else {
                        throw new OWSWebServiceException("PropertyIsLessThan operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint", version);
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new OWSWebServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":[00000101").append(' ').append(dateValue).append("]");
                    } else {
                         throw new OWSWebServiceException("PropertyIsLessThanOrEqualTo operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint", version);
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
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> JBSpatialOps) throws WebServiceException {
        SpatialFilter spatialfilter = null;
        
        SpatialOpsType spatialOps = JBSpatialOps.getValue();
        
        if (spatialOps instanceof BBOXType) {
            BBOXType bbox       = (BBOXType) spatialOps;
            String propertyName = bbox.getPropertyName();
            String CRSName      = bbox.getSRS();
            
            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new OWSWebServiceException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } else if (!propertyName.contains("BoundingBox")) {
                throw new OWSWebServiceException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new OWSWebServiceException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            if (CRSName == null) {
                throw new OWSWebServiceException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //we transform the EnvelopeEntry in GeneralEnvelope
            double min[] = {bbox.getMinX(), bbox.getMinY()};
            double max[] = {bbox.getMaxX(), bbox.getMaxY()};
            try {
                GeneralEnvelope envelope      = new GeneralEnvelope(min, max);
                CoordinateReferenceSystem crs = CRS.decode(CRSName, true);
                envelope.setCoordinateReferenceSystem(crs);
                spatialfilter = new SpatialFilter(envelope, CRSName, SpatialFilter.BBOX);
                
            } catch (NoSuchAuthorityCodeException e) {
                throw new OWSWebServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } catch (FactoryException e) {
                throw new OWSWebServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } catch (IllegalArgumentException e) {
                throw new OWSWebServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
        } else if (spatialOps instanceof DistanceBufferType) {
            
            DistanceBufferType dist = (DistanceBufferType) spatialOps;
            double distance         = dist.getDistance();
            String units            = dist.getDistanceUnits();
            JAXBElement JBgeom      = dist.getAbstractGeometry();
            String operator         = JBSpatialOps.getName().getLocalPart();
            int filterType;
            if (operator.equals("DWithin"))
                filterType = SpatialFilter.DWITHIN;
            else if (operator.equals("Beyond"))
                filterType = SpatialFilter.BEYOND;
            else
                throw new OWSWebServiceException("Unknow DistanceBuffer operator.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
           
            //we verify that all the parameters are specified
            if (dist.getPropertyName() == null) {
                 throw new OWSWebServiceException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            if (units == null) {
                 throw new OWSWebServiceException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            if (JBgeom == null || JBgeom.getValue() == null) {
                 throw new OWSWebServiceException("An distanceBuffer operator must specified a geometric object.",
                                                  INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
           
            Object geometry  = JBgeom.getValue(); 
            String propName  = dist.getPropertyName().getPropertyName();
            String CRSName   = null;
           
            // we transform the gml geometry in treatable geometry
            try {
                if (geometry instanceof PointType) {
                    PointType GMLpoint = (PointType) geometry;
                    CRSName  = GMLpoint.getSrsName();
                    geometry = GMLpointToGeneralDirectPosition(GMLpoint);
                    
                } else if (geometry instanceof LineStringType) {
                    LineStringType GMLline =  (LineStringType) geometry;
                    CRSName  = GMLline.getSrsName();
                    geometry = GMLlineToline2d(GMLline);
                    
                } else if (geometry instanceof EnvelopeEntry) {
                    EnvelopeEntry GMLenvelope = (EnvelopeEntry) geometry;
                    CRSName  = GMLenvelope.getSrsName();
                    geometry = GMLenvelopeToGeneralEnvelope(GMLenvelope);
                }
                spatialfilter = new SpatialFilter(geometry, CRSName, filterType, distance, units);
               
            } catch (NoSuchAuthorityCodeException e) {
                    throw new OWSWebServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } catch (FactoryException e) {
                    throw new OWSWebServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } catch (IllegalArgumentException e) {
                    throw new OWSWebServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
           
        } else if (spatialOps instanceof BinarySpatialOpType) {
            
            BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;
            List<JAXBElement<?>> objects   = binSpatial.getRest();
            
            String propertyName = null;
            String operator     = JBSpatialOps.getName().getLocalPart();
            Object geometry     = null;
            
            for (JAXBElement<?> jb: objects) {
                
                // the propertyName
                if (jb.getValue() instanceof PropertyNameType) {
                    PropertyNameType p = (PropertyNameType) jb.getValue();
                    propertyName = p.getContent();
                
                // geometric object: envelope    
                } else if (jb.getValue() instanceof EnvelopeEntry) {
                    geometry     = (EnvelopeEntry) jb.getValue();
                
                // geometric object: point
                } else if (jb.getValue() instanceof PointType) {
                    geometry     = (PointType) jb.getValue();
                 
                // geometric object: Line    
                } else if (jb.getValue() instanceof LineStringType) {
                    geometry     = (LineStringType) jb.getValue();    
                
                } else if (jb.getValue() == null) {
                   throw new IllegalArgumentException("null value in BinarySpatialOp type");
                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + jb.getValue().getClass().getSimpleName());
                }
            }
            
            if (propertyName == null && geometry == null) {
                throw new OWSWebServiceException("An Binarary spatial operator must specified a propertyName and a geometry.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            int filterType = SpatialFilter.valueOf(operator);
            if (filterType == -1) {
                throw new OWSWebServiceException("Unknow FilterType: " + operator,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            String CRSName = "undefined CRS";
            try {
                if (geometry instanceof EnvelopeEntry) {
                    
                    //we transform the EnvelopeEntry in GeneralEnvelope
                    EnvelopeEntry GMLenvelope   = (EnvelopeEntry)geometry;
                    CRSName                     = GMLenvelope.getSrsName();
                    GeneralEnvelope envelope    = GMLenvelopeToGeneralEnvelope(GMLenvelope);
                    spatialfilter               = new SpatialFilter(envelope, CRSName, filterType);
                
                } else if (geometry instanceof PointType) {
                    PointType GMLpoint          = (PointType) geometry;
                    CRSName                     = GMLpoint.getSrsName();
                    GeneralDirectPosition point = GMLpointToGeneralDirectPosition(GMLpoint);
                    spatialfilter               = new SpatialFilter(point, CRSName, filterType);
                
                } else if (geometry instanceof LineStringType) {
                    LineStringType GMLline =  (LineStringType) geometry;
                    CRSName                = GMLline.getSrsName();
                    Line2D line            = GMLlineToline2d(GMLline);
                    spatialfilter          = new SpatialFilter(line, CRSName, filterType);
                }
                
            } catch (NoSuchAuthorityCodeException e) {
                throw new OWSWebServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } catch (FactoryException e) {
                throw new OWSWebServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } catch (IllegalArgumentException e) {
                throw new OWSWebServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
        }
        
        return spatialfilter;
    }
    
    private String treatIDOperator(final List<JAXBElement<? extends AbstractIdType>> JBIdsOps) {
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
     * 
     * example getOccurence( "hello, welcome to hell", "hell") returns 2.
     * example getOccurence( "hello, welcome to hell", "o")    returns 3. 
     * 
     * @param s   A character String.
     * @param occ A character String.
     * 
     * @return The number of occurence of the string occ in the string s.
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
    
    /**
     * Parses a value as a floating point.
     *
     * @throws WebServiceException if the value can't be parsed.
     */
    private double parseDouble(String value) throws WebServiceException {
        value = value.trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new OWSWebServiceException("The value:" + value + " is not a valid double coordinate.",
                                              INVALID_PARAMETER_VALUE, "Coordinates", version);
        }
    }
    
    /**
     * Transform A GML point into a treatable geometric object : GeneralDirectPosition
     * 
     * @param GMLpoint The GML point to transform.
     * 
     * @return A GeneralDirectPosition.
     * 
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     */
    private GeneralDirectPosition GMLpointToGeneralDirectPosition(PointType GMLpoint) throws WebServiceException, NoSuchAuthorityCodeException, FactoryException {
        
        String CRSName = GMLpoint.getSrsName();

        if (CRSName == null) {
            throw new OWSWebServiceException("A GML point must specify Coordinate Reference System.",
                    INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }

        //we get the coordinate of the point (if they are present)
        if (GMLpoint.getCoordinates() == null && GMLpoint.getPos() == null) {
            throw new OWSWebServiceException("A GML point must specify coordinates or direct position.",
                    INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }

        final double[] coordinates = new double[2];
        if (GMLpoint.getCoordinates() != null) {
            String coord = GMLpoint.getCoordinates().getValue();
       
            final StringTokenizer tokens = new StringTokenizer(coord, " ");
            int index = 0;
            while (tokens.hasMoreTokens()) {
                final double value = parseDouble(tokens.nextToken());
                if (index >= coordinates.length) {
                    throw new OWSWebServiceException("This service support only 2D point.",
                            INVALID_PARAMETER_VALUE, "QueryConstraint", version);
                }
                coordinates[index++] = value;
            }
        } else if (GMLpoint.getPos().getValue() != null && GMLpoint.getPos().getValue().size() == 2){
            coordinates[0] = GMLpoint.getPos().getValue().get(0);
            coordinates[0] = GMLpoint.getPos().getValue().get(1);
        } else {
            throw new OWSWebServiceException("The GML pointis malformed.",
                    INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }
        GeneralDirectPosition point = new GeneralDirectPosition(coordinates);
        CoordinateReferenceSystem crs = CRS.decode(CRSName, true);
        point.setCoordinateReferenceSystem(crs);
        return point;    
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
    public GeneralEnvelope GMLenvelopeToGeneralEnvelope(EnvelopeEntry GMLenvelope) throws NoSuchAuthorityCodeException, FactoryException, WebServiceException {
        String CRSName = GMLenvelope.getSrsName();
        if (CRSName == null) {
            throw new OWSWebServiceException("An operator BBOX must specified a CRS (coordinate Reference system) for the envelope.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }
       
        List<Double> lmin = GMLenvelope.getLowerCorner().getValue();
        double min[] = new double[lmin.size()];
        for (int i = 0; i < min.length; i++) {
            min[i] = lmin.get(i);
        }

        List<Double> lmax = GMLenvelope.getUpperCorner().getValue();
        double max[] = new double[lmax.size()];
        for (int i = 0; i < min.length; i++) {
            max[i] = lmax.get(i);
        }

        GeneralEnvelope envelopeF = new GeneralEnvelope(min, max);
        CoordinateReferenceSystem crs = CRS.decode(CRSName, true);
        envelopeF.setCoordinateReferenceSystem(crs);
        return envelopeF;
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
    public Line2D GMLlineToline2d(LineStringType GMLline) throws NoSuchAuthorityCodeException, FactoryException, WebServiceException {
        String CRSName = GMLline.getSrsName();
        if (CRSName == null) {
            throw new OWSWebServiceException("A CRS (coordinate Reference system) must be specified for the line.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }
       
        CoordinatesType coord = GMLline.getCoordinates();
        String s = coord.getValue();
        double X1, X2, Y1, Y2;
        
        X1 = Double.parseDouble(s.substring(0, s.indexOf(coord.getCs())));
        
        s = s.substring(s.indexOf(coord.getCs()) + 1);
        
        Y1 = Double.parseDouble(s.substring(0, s.indexOf(coord.getTs())));
        
        s = s.substring(s.indexOf(coord.getTs()) + 1);
        
        X2 = Double.parseDouble(s.substring(0, s.indexOf(coord.getCs())));
        
        s = s.substring(s.indexOf(coord.getCs()) + 1);
        
        Y2 = Double.parseDouble(s);

        Line2D line = new Line2D.Double(X1, Y1, X2, Y2);
        
        // TODO CoordinateReferenceSystem crs = CRS.decode(CRSName, true);
        
        return line;
    }
}
