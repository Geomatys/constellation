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
import java.util.List;
import java.util.Map;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import javax.xml.namespace.QName;
import org.constellation.cat.csw.v202.QueryConstraintType;
import org.constellation.ws.WebServiceException;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.LineStringType;
import org.constellation.gml.v311.PointType;
import org.constellation.gml.v311.AbstractGeometryType;
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
import static org.constellation.ows.OWSExceptionCode.*;

// Lucene dependencies
import org.apache.lucene.search.Filter;

// geotools dependencies
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
public class LuceneFilterParser extends FilterParser {
    
    /**
     * Build a lucene request from the specified constraint
     * 
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public SpatialQuery getQuery(final QueryConstraintType constraint, Map<String, QName> variables, Map<String, String> prefixs) throws WebServiceException {
        FilterType filter = null;
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            Filter nullFilter = null;
            return new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
            
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new WebServiceException("The query constraint must be in Filter or CQL but not both.",
                                          INVALID_PARAMETER_VALUE, "QueryConstraint");
        } else if (constraint.getCqlText() == null && constraint.getFilter() == null) {
            throw new WebServiceException("The query constraint must contain a Filter or a CQL query.",
                                         INVALID_PARAMETER_VALUE, "QueryConstraint");
        }
        
        if (constraint.getCqlText() != null) {
            try {
                filter = CQLtoFilter(constraint.getCqlText());
                 
            } catch (JAXBException ex) {
                ex.printStackTrace();
                throw new WebServiceException("JAXBException while parsing CQL query: " + ex.getMessage(),
                                             NO_APPLICABLE_CODE, "QueryConstraint");
            } catch (CQLException ex) {
                throw new WebServiceException("The CQL query is malformed: " + ex.getMessage() + '\n' 
                                                 + "syntax Error: " + ex.getSyntaxError(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
        } else if (constraint.getFilter() != null) {
            filter = constraint.getFilter();
            
        }
        return getLuceneQuery(filter);
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
    protected SpatialQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> JBlogicOps) throws WebServiceException {
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
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...   
            for (JAXBElement<? extends ComparisonOpsType> jb: binary.getComparisonOps()) {
            
                queryBuilder.append(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
                queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
            }
            
            // we treat logical Operators like AND, OR, ...
            for (JAXBElement<? extends LogicOpsType> jb: binary.getLogicOps()) {
            
                boolean writeOperator = true;
                
                SpatialQuery sq  = treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb);
                String subQuery  = sq.getQuery();
                Filter subFilter = sq.getSpatialFilter();
                    
                //if the sub spatial query contains both term search and spatial search we create a subQuery 
                if ((subFilter != null && !subQuery.equals("metafile:doc")) 
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
               
                if (writeOperator) {
                    queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
                } else {
                    writeOperator = true;
                }
            }
            
            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...   
            for (JAXBElement<? extends SpatialOpsType> jb: binary.getSpatialOps()) {
                
                //for the spatial filter we don't need to write into the lucene query 
                filters.add(treatSpatialOperator((JAXBElement<? extends SpatialOpsType>)jb));
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
                    
                if ((sq.getLogicalOperator() == SerialChainFilter.OR && subFilter != null && !subQuery.equals("metafile:doc")) ||
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
    protected String treatComparisonOperator(final JAXBElement<? extends ComparisonOpsType> JBComparisonOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        ComparisonOpsType comparisonOps = JBComparisonOps.getValue();
        
        if (comparisonOps instanceof PropertyIsLikeType ) {
            PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            String propertyName    = "";
            //we get the field
            if (pil.getPropertyName() != null) {
                propertyName = pil.getPropertyName().getContent();
                response.append(removePrefix(propertyName)).append(':');
            } else {
                throw new WebServiceException("An operator propertyIsLike must specified the propertyName.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                throw new WebServiceException("An operator propertyIsLike must specified the literal value.",
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
        } else if (comparisonOps instanceof PropertyIsNullType) {
             PropertyIsNullType pin = (PropertyIsNullType) comparisonOps;

            //we get the field
            if (pin.getPropertyName() != null) {
                response.append(removePrefix(pin.getPropertyName().getContent())).append(':').append("null");
            } else {
                throw new WebServiceException("An operator propertyIsNull must specified the propertyName.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
        } else if (comparisonOps instanceof PropertyIsBetweenType) {
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
        
        } else if (comparisonOps instanceof BinaryComparisonOpType) {
            
            BinaryComparisonOpType bc = (BinaryComparisonOpType) comparisonOps;
            String propertyName       = bc.getPropertyName();
            LiteralType literal       = bc.getLiteral();
            String operator           = JBComparisonOps.getName().getLocalPart(); 
            
            if (propertyName == null || literal == null) {
                throw new WebServiceException("A binary comparison operator must be constitued of a literal and a property name.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                            throw new WebServiceException("The service was unable to parse the Date: " + dateValue,
                                                          INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":[").append(dateValue).append(' ').append(" 30000101]");
                    } else {
                        throw new WebServiceException("PropertyIsGreaterThanOrEqualTo operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified") || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new WebServiceException("The service was unable to parse the Date: " + dateValue,
                                                         INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":{").append(dateValue).append(' ').append(" 30000101}");
                    } else {
                        throw new WebServiceException("PropertyIsGreaterThan operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                
                } else if (operator.equals("PropertyIsLessThan") ) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified") || propertyName.contains("date")) {
                        //if we are passed by CQL we must format the date
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new WebServiceException("The service was unable to parse the Date: " + dateValue,
                                                          INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":{00000101").append(' ').append(dateValue).append("}");
                    } else {
                        throw new WebServiceException("PropertyIsLessThan operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new WebServiceException("The service was unable to parse the Date: " + dateValue,
                                                          INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":[00000101").append(' ').append(dateValue).append("]");
                    } else {
                         throw new WebServiceException("PropertyIsLessThanOrEqualTo operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                } else {
                    throw new WebServiceException("Unkwnow comparison operator: " + operator,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
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
    protected Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> JBSpatialOps) throws WebServiceException {
        SpatialFilter spatialfilter = null;
        
        SpatialOpsType spatialOps = JBSpatialOps.getValue();
        
        if (spatialOps instanceof BBOXType) {
            BBOXType bbox       = (BBOXType) spatialOps;
            String propertyName = bbox.getPropertyName();
            String CRSName      = bbox.getSRS();
            
            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new WebServiceException("An operator BBOX must specified the propertyName.",
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            } else if (!propertyName.contains("BoundingBox")) {
                throw new WebServiceException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new WebServiceException("An operator BBOX must specified an envelope.",
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (CRSName == null) {
                throw new WebServiceException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                throw new WebServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (FactoryException e) {
                throw new WebServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (IllegalArgumentException e) {
                throw new WebServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                throw new WebServiceException("Unknow DistanceBuffer operator.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
           
            //we verify that all the parameters are specified
            if (dist.getPropertyName() == null) {
                 throw new WebServiceException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (units == null) {
                 throw new WebServiceException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (JBgeom == null || JBgeom.getValue() == null) {
                 throw new WebServiceException("An distanceBuffer operator must specified a geometric object.",
                                                  INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                    throw new WebServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (FactoryException e) {
                    throw new WebServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (IllegalArgumentException e) {
                    throw new WebServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
           
        } else if (spatialOps instanceof BinarySpatialOpType) {
            
            BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;
                        
            String propertyName = null;
            String operator     = JBSpatialOps.getName().getLocalPart();
            Object geometry     = null;
            
            // the propertyName
            if (binSpatial.getPropertyName() != null && binSpatial.getPropertyName().getValue() != null) {
                PropertyNameType p = binSpatial.getPropertyName().getValue();
                propertyName = p.getContent();
            }
                
            // geometric object: envelope    
            if (binSpatial.getEnvelope() != null && binSpatial.getEnvelope().getValue() != null) {
                geometry = binSpatial.getEnvelope().getValue();
            }
                
            
            if (binSpatial.getAbstractGeometry() != null && binSpatial.getAbstractGeometry().getValue() != null) {
                AbstractGeometryType ab =  binSpatial.getAbstractGeometry().getValue();
                
                // geometric object: point
                if (ab instanceof PointType) {
                    geometry     = (PointType) ab;
                 
                // geometric object: Line    
                } else if (ab instanceof LineStringType) {
                    geometry     = (LineStringType) ab;    
                
                } else if (ab == null) {
                   throw new IllegalArgumentException("null value in BinarySpatialOp type");
                
                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + ab.getClass().getSimpleName());
                }
            }
            
            if (propertyName == null && geometry == null) {
                throw new WebServiceException("An Binarary spatial operator must specified a propertyName and a geometry.",
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
            int filterType = SpatialFilter.valueOf(operator);
            if (filterType == -1) {
                throw new WebServiceException("Unknow FilterType: " + operator,
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
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
                throw new WebServiceException("Unknow Coordinate Reference System: " + CRSName,
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (FactoryException e) {
                throw new WebServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (IllegalArgumentException e) {
                throw new WebServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                              INVALID_PARAMETER_VALUE, "QueryConstraint");
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
}
