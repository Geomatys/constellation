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
import com.vividsolutions.jts.geom.Geometry;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// JAXB dependencies
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ws.CstlServiceException;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Lucene dependencies
import org.apache.lucene.search.Filter;

// geotools dependencies
import org.geotoolkit.gml.GMLUtilities;
import org.constellation.metadata.Parameters;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.geometry.jts.SRIDGenerator.Version;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.LineStringType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialFilterType;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.ogc.xml.v110.AbstractIdType;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.BinaryComparisonOpType;
import org.geotoolkit.ogc.xml.v110.BinaryLogicOpType;
import org.geotoolkit.ogc.xml.v110.BinarySpatialOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.DistanceBufferType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.PropertyIsBetweenType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLikeType;
import org.geotoolkit.ogc.xml.v110.PropertyIsNullType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.ogc.xml.v110.UnaryLogicOpType;

// GeoAPI dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import static org.geotoolkit.lucene.filter.LuceneOGCFilter.*;

/**
 * A parser for filter 1.1.0 and CQL 2.0
 * 
 * @author Guilhem Legal
 */
public class LuceneFilterParser extends FilterParser {

    private String defaultField = "metafile:doc";

    /**
     * Build a lucene request from the specified constraint
     * 
     * @param constraint a constraint expressed in CQL or FilterType
     */
    @Override
    public SpatialQuery getQuery(final QueryConstraint constraint, Map<String, QName> variables, Map<String, String> prefixs) throws CstlServiceException {
        FilterType filter = null;
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            final Filter nullFilter = null;
            return new SpatialQuery(defaultField, nullFilter, SerialChainFilter.AND);
            
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new CstlServiceException("The query constraint must be in Filter or CQL but not both.",
                                          INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
        } else if (constraint.getCqlText() == null && constraint.getFilter() == null) {
            throw new CstlServiceException("The query constraint must contain a Filter or a CQL query.",
                                         INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
        }
        
        if (constraint.getCqlText() != null) {
            try {
                filter = cqlToFilter(constraint.getCqlText());
                 
            } catch (JAXBException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new CstlServiceException("JAXBException while parsing CQL query: " + ex.getMessage(),
                                             NO_APPLICABLE_CODE, Parameters.QUERY_CONSTRAINT);
            } catch (CQLException ex) {
                throw new CstlServiceException("The CQL query is malformed: " + ex.getMessage() + '\n' 
                                                 + "syntax Error: " + ex.getSyntaxError(),
                                                 INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
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
    public SpatialQuery getLuceneQuery(final FilterType filter) throws CstlServiceException {
        
        SpatialQuery response   = null;
        //for ambigous purpose
        final Filter nullFilter = null;
        
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
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    @Override
    protected SpatialQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws CstlServiceException {
        final List<SpatialQuery> subQueries = new ArrayList<SpatialQuery>();
        final StringBuilder queryBuilder    = new StringBuilder();
        final LogicOpsType logicOps         = jbLogicOps.getValue();
        final String operator               = jbLogicOps.getName().getLocalPart();
        final List<Filter> filters          = new ArrayList<Filter>();
        
        if (logicOps instanceof BinaryLogicOpType) {
            final BinaryLogicOpType binary = (BinaryLogicOpType) logicOps;
            queryBuilder.append('(');
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...   
            for (JAXBElement<? extends ComparisonOpsType> jb: binary.getComparisonOps()) {
            
                queryBuilder.append(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
                queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
            }
            
            // we treat logical Operators like AND, OR, ...
            for (JAXBElement<? extends LogicOpsType> jb: binary.getLogicOps()) {
            
                boolean writeOperator = true;
                
                final SpatialQuery sq  = treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb);
                final String subQuery  = sq.getQuery();
                final Filter subFilter = sq.getSpatialFilter();
                    
                //if the sub spatial query contains both term search and spatial search we create a subQuery 
                if ((subFilter != null && !subQuery.equals(defaultField))
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
          final int pos = queryBuilder.length()- (operator.length() + 2);
          if (pos > 0)
            queryBuilder.delete(queryBuilder.length()- (operator.length() + 2), queryBuilder.length());
          
          queryBuilder.append(')');
                
        } else if (logicOps instanceof UnaryLogicOpType) {
            final UnaryLogicOpType unary = (UnaryLogicOpType) logicOps;
                       
                        
            // we treat comparison operator: PropertyIsLike, IsNull, IsBetween, ...    
            if (unary.getComparisonOps() != null) {
                queryBuilder.append(treatComparisonOperator(unary.getComparisonOps()));
                
            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...        
            } else if (unary.getSpatialOps() != null) {
                
                filters.add(treatSpatialOperator(unary.getSpatialOps()));
                
                
             // we treat logical Operators like AND, OR, ...
            } else if (unary.getLogicOps() != null) {
                final SpatialQuery sq  = treatLogicalOperator(unary.getLogicOps());
                final String subQuery  = sq.getQuery();
                final Filter subFilter = sq.getSpatialFilter();
                    
                if ((sq.getLogicalOperator() == SerialChainFilter.OR && subFilter != null && !subQuery.equals(defaultField)) ||
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
                final int[] filterType = {SerialChainFilter.NOT};
                spatialFilter = new SerialChainFilter(filters, filterType);
                if (query.equals("")) {
                    logicalOperand = SerialChainFilter.AND;
                } 
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
        
            
        final SpatialQuery response = new SpatialQuery(query, spatialFilter, logicalOperand);
        response.setSubQueries(subQueries);
        return response;
    }
    
    /**
     * Build a piece of lucene query with the specified Comparison filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    @Override
    protected String treatComparisonOperator(final JAXBElement<? extends ComparisonOpsType> jbComparisonOps) throws CstlServiceException {
        final StringBuilder response = new StringBuilder();
        
        final ComparisonOpsType comparisonOps = jbComparisonOps.getValue();
        
        if (comparisonOps instanceof PropertyIsLikeType ) {
            final PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            String propertyName    = "";
            //we get the field
            if (pil.getPropertyName() != null) {
                propertyName = pil.getPropertyName().getContent();
                response.append(removePrefix(propertyName)).append(':');
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the propertyName.",
                                             INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            
            //we get the value of the field
            if (pil.getLiteral() != null && pil.getLiteral() != null) {
                
                //we format the value by replacing the specified special char by the lucene special char
                String brutValue = pil.getLiteral();
                brutValue = brutValue.replace(pil.getWildCard(),    "*");
                brutValue = brutValue.replace(pil.getSingleChar(),  "?");
                brutValue = brutValue.replace(pil.getEscapeChar(),  "\\");
                
                //for a date we remove the '-'
                if (isDateField(propertyName)) {
                        brutValue = brutValue.replaceAll("-", "");
                        brutValue = brutValue.replace("Z", "");
                }
                
                response.append(brutValue);
                
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the literal value.",
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
        } else if (comparisonOps instanceof PropertyIsNullType) {
             final PropertyIsNullType pin = (PropertyIsNullType) comparisonOps;

            //we get the field
            if (pin.getPropertyName() != null) {
                response.append(removePrefix(pin.getPropertyName().getContent())).append(':').append("null");
            } else {
                throw new CstlServiceException("An operator propertyIsNull must specified the propertyName.",
                                             INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
        } else if (comparisonOps instanceof PropertyIsBetweenType) {
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
        
        } else if (comparisonOps instanceof BinaryComparisonOpType) {
            
            final BinaryComparisonOpType bc = (BinaryComparisonOpType) comparisonOps;
            final String propertyName       = bc.getPropertyName();
            final LiteralType literal       = bc.getLiteral();
            final String operator           = jbComparisonOps.getName().getLocalPart();
            
            if (propertyName == null || literal == null) {
                throw new CstlServiceException("A binary comparison operator must be constitued of a literal and a property name.",
                                             INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            } else {
                if (operator.equals("PropertyIsEqualTo")) {                
                    response.append(removePrefix(propertyName)).append(":\"").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsNotEqualTo")) {
                    
                   response.append("metafile:doc NOT ");
                   response.append(removePrefix(propertyName)).append(":\"").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsGreaterThanOrEqualTo")) {
                    if (isDateField(propertyName)) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue,
                                                          INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":[").append(dateValue).append(' ').append(" 30000101]");
                    } else {
                        throw new CstlServiceException("PropertyIsGreaterThanOrEqualTo operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, Parameters.QUERY_CONSTRAINT);
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (isDateField(propertyName)) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue,
                                                         INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":{").append(dateValue).append(' ').append(" 30000101}");
                    } else {
                        throw new CstlServiceException("PropertyIsGreaterThan operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, Parameters.QUERY_CONSTRAINT);
                    }
                
                } else if (operator.equals("PropertyIsLessThan") ) {
                    if (isDateField(propertyName)) {
                        //if we are passed by CQL we must format the date
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue,
                                                          INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":{00000101").append(' ').append(dateValue).append("}");
                    } else {
                        throw new CstlServiceException("PropertyIsLessThan operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, Parameters.QUERY_CONSTRAINT);
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (isDateField(propertyName)) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue,
                                                          INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
                        }
                        dateValue = dateValue.replaceAll("-", "");
                        dateValue = dateValue.replace("Z", "");
                        response.append(removePrefix(propertyName)).append(":[00000101").append(' ').append(dateValue).append("]");
                    } else {
                         throw new CstlServiceException("PropertyIsLessThanOrEqualTo operator works only on Date field. " + operator,
                                                      OPERATION_NOT_SUPPORTED, Parameters.QUERY_CONSTRAINT);
                    }
                } else {
                    throw new CstlServiceException("Unkwnow comparison operator: " + operator,
                                                 INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
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
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    @Override
    protected Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> jbSpatialOps) throws CstlServiceException {
        LuceneOGCFilter spatialfilter = null;
        
        final SpatialOpsType spatialOps = jbSpatialOps.getValue();
        
        if (spatialOps instanceof BBOXType) {
            final BBOXType bbox       = (BBOXType) spatialOps;
            final String propertyName = bbox.getPropertyName();
            final String crsName      = bbox.getSRS();
            
            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new CstlServiceException("An operator BBOX must specified the propertyName.",
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            } else if (!propertyName.contains("BoundingBox")) {
                throw new CstlServiceException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new CstlServiceException("An operator BBOX must specified an envelope.",
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            if (crsName == null) {
                throw new CstlServiceException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                             INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            
            //we transform the EnvelopeEntry in bbox filter
            spatialfilter = wrap(FF.bbox(GEOMETRY_PROPERTY, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), crsName));
                
            
        } else if (spatialOps instanceof DistanceBufferType) {
            
            final DistanceBufferType dist = (DistanceBufferType) spatialOps;
            final double distance         = dist.getDistance();
            final String units            = dist.getDistanceUnits();
            final JAXBElement jbGeom      = dist.getAbstractGeometry();
            final String operator         = jbSpatialOps.getName().getLocalPart();
           
            //we verify that all the parameters are specified
            if (dist.getPropertyName() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            if (units == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            if (jbGeom == null || jbGeom.getValue() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified a geometric object.",
                                                  INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
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
                    geometry = GMLUtilities.toJTS(gmlPoint);

                } else if (gml instanceof LineStringType) {
                    final LineStringType gmlLine =  (LineStringType) gml;
                    crsName  = gmlLine.getSrsName();
                    geometry = GMLUtilities.toJTS(gmlLine);

                } else if (gml instanceof EnvelopeEntry) {
                    final EnvelopeEntry gmlEnvelope = (EnvelopeEntry) gml;
                    crsName  = gmlEnvelope.getSrsName();
                    geometry = GMLUtilities.toJTS(gmlEnvelope);
                }

                if (operator.equals("DWithin")) {
                    spatialfilter = wrap(FF.dwithin(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else if (operator.equals("Beyond")) {
                    spatialfilter = wrap(FF.beyond(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else {
                    throw new CstlServiceException("Unknow DistanceBuffer operator.",
                            INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
                }
                
            } catch (NoSuchAuthorityCodeException e) {
                    throw new CstlServiceException(UNKNOW_CRS_ERROR_MSG + crsName,
                                                     INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                    throw new CstlServiceException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                    throw new CstlServiceException(INCORRECT_BBOX_DIM_ERROR_MSG + e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
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
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            
            SpatialFilterType filterType = null;
            try {
                filterType = SpatialFilterType.valueOf(operator);
            } catch (IllegalArgumentException ex) {
                LOGGER.severe("unknow spatial filter Type");
            }
            if (filterType == null) {
                throw new CstlServiceException("Unknow FilterType: " + operator,
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            
            String crsName = "undefined CRS";
            try {
                Geometry filterGeometry = null;
                if (gmlGeometry instanceof EnvelopeEntry) {

                    //we transform the EnvelopeEntry in GeneralEnvelope
                    final EnvelopeEntry gmlEnvelope = (EnvelopeEntry)gmlGeometry;
                    crsName                   = gmlEnvelope.getSrsName();
                    filterGeometry            = GMLUtilities.toJTS(gmlEnvelope);

                } else if (gmlGeometry instanceof PointType) {
                    final PointType gmlPoint  = (PointType) gmlGeometry;
                    crsName                   = gmlPoint.getSrsName();
                    filterGeometry            = GMLUtilities.toJTS(gmlPoint);

                } else if (gmlGeometry instanceof LineStringType) {
                    final LineStringType gmlLine =  (LineStringType) gmlGeometry;
                    crsName                = gmlLine.getSrsName();
                    filterGeometry         = GMLUtilities.toJTS(gmlLine);
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
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                throw new CstlServiceException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                throw new CstlServiceException(INCORRECT_BBOX_DIM_ERROR_MSG+ e.getMessage(),
                                              INVALID_PARAMETER_VALUE, Parameters.QUERY_CONSTRAINT);
            }
            
        }
        
        return spatialfilter;
    }
    
    private String treatIDOperator(final List<JAXBElement<? extends AbstractIdType>> jbIdsOps) {
        final StringBuilder response = new StringBuilder();
        
        //TODO
        if (true)
            throw new UnsupportedOperationException("Not supported yet.");
            
        return response.toString();
    }
    
    /**
     * Remove the prefix on propertyName.
     */
    private String removePrefix(String s) {
        final int i = s.indexOf(':');
        if ( i != -1) {
            s = s.substring(i + 1, s.length());
        }
        return s;
    }
}
