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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.text.ParseException;

// Apache Lucene dependencies
import org.apache.lucene.search.Filter;

// constellation dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.gml.v311.AbstractGeometryType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.LineStringType;
import org.constellation.gml.v311.PointType;
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.ogc.BinaryComparisonOpType;
import org.constellation.ogc.BinaryLogicOpType;
import org.constellation.ogc.ComparisonOpsType;
import org.constellation.ogc.FilterType;
import org.constellation.ogc.LiteralType;
import org.constellation.ogc.LogicOpsType;
import org.constellation.ogc.PropertyIsBetweenType;
import org.constellation.ogc.PropertyIsLikeType;
import org.constellation.ogc.PropertyIsNullType;
import org.constellation.ogc.SpatialOpsType;
import org.constellation.ogc.UnaryLogicOpType;
import org.constellation.ogc.AbstractIdType;
import org.constellation.ogc.BBOXType;
import org.constellation.ogc.BinarySpatialOpType;
import org.constellation.ogc.DistanceBufferType;
import org.constellation.ogc.PropertyNameType;
import org.constellation.cat.csw.QueryConstraint;
import org.constellation.lucene.filter.BBOXFilter;
import org.constellation.lucene.filter.BeyondFilter;
import org.constellation.lucene.filter.ContainsFilter;
import org.constellation.lucene.filter.CrossesFilter;
import org.constellation.lucene.filter.DWithinFilter;
import org.constellation.lucene.filter.DisjointFilter;
import org.constellation.lucene.filter.EqualsFilter;
import org.constellation.lucene.filter.IntersectFilter;
import org.constellation.lucene.filter.OverlapsFilter;
import org.constellation.lucene.filter.SpatialFilter;
import org.constellation.lucene.filter.SpatialFilterType;
import org.constellation.lucene.filter.TouchesFilter;
import org.constellation.lucene.filter.WithinFilter;
import static org.constellation.ows.OWSExceptionCode.*;

// Geotools dependencies
import org.geotoolkit.referencing.CRS;
import org.geotools.filter.text.cql2.CQLException;
import org.geotoolkit.geometry.GeneralEnvelope;

// MDWeb dependencies
import org.mdweb.model.schemas.Standard;

// GeoAPI dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 * A parser for filter 1.1.0 and CQL 2.0
 * 
 * @author Guilhem Legal
 */
public class SQLFilterParser extends FilterParser {
    
    /**
     * A map of variables (used in ebrim syntax).
     */
    private Map<String, QName> variables;
    
    /**
     * A map of prefix and their correspounding namespace(used in ebrim syntax).
     */
    private Map<String, String> prefixs;
    
    private int nbField;
    
    private boolean executeSelect;
    
    /**
     * Build a lucene request from the specified constraint
     * 
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public SQLQuery getQuery(final QueryConstraint constraint, Map<String, QName> variables, Map<String, String> prefixs) throws CstlServiceException {
        this.setVariables(variables);
        this.setPrefixs(prefixs);
        FilterType filter = null;
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            return new SQLQuery("Select identifier, catalog from Form where catalog != 'MDATA");
            
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new CstlServiceException("The query constraint must be in Filter or CQL but not both.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
        } else if (constraint.getCqlText() == null && constraint.getFilter() == null) {
            throw new CstlServiceException("The query constraint must contain a Filter or a CQL query.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
        }
        
        if (constraint.getCqlText() != null) {
            try {
                filter = CQLtoFilter(constraint.getCqlText());

            } catch (JAXBException ex) {
                ex.printStackTrace();
                throw new CstlServiceException("JAXBException while parsing CQL query: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE, "QueryConstraint");
            } catch (CQLException ex) {
                throw new CstlServiceException("The CQL query is malformed: " + ex.getMessage() + '\n' 
                                                 + "syntax Error: " + ex.getSyntaxError(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
        } else if (constraint.getFilter() != null) {
            filter = constraint.getFilter();
            
        }
        return getSqlQuery(filter);
    }
    
     /**
     * Build a lucene request from the specified Filter.
     * 
     * @param filter a Filter object build directly from the XML or from a CQL request
     */
    public SQLQuery getSqlQuery(final FilterType filter) throws CstlServiceException {
        
        SQLQuery response = null;
        executeSelect     = true;
        if (filter != null) { 
            // we treat logical Operators like AND, OR, ...
            if (filter.getLogicOps() != null) {
                response = treatLogicalOperator(filter.getLogicOps());
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...    
            } else if (filter.getComparisonOps() != null) {
                response = new SQLQuery(treatComparisonOperator(filter.getComparisonOps()));
                
            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...    
            } else if (filter.getSpatialOps() != null) {
                response = new SQLQuery(treatSpatialOperator(filter.getSpatialOps()));
                
            } else if (filter.getId() != null) {
                response = new SQLQuery(treatIDOperator(filter.getId()));
            }  
        }
        response.nbField = nbField - 1;
        if (executeSelect)
            response.createSelect();
        return response;
    }
    
    /**
     * Build a piece of lucene query with the specified Logical filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected SQLQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> JBlogicOps) throws CstlServiceException {
        List<SQLQuery> subQueries     = new ArrayList<SQLQuery>();
        StringBuilder queryBuilder    = new StringBuilder();
        LogicOpsType logicOps         = JBlogicOps.getValue();
        String operator               = JBlogicOps.getName().getLocalPart();
        List<Filter> filters          = new ArrayList<Filter>();
        nbField                       = 1;
        
        if (logicOps instanceof BinaryLogicOpType) {
            BinaryLogicOpType binary = (BinaryLogicOpType) logicOps;
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...   
            for (JAXBElement<? extends ComparisonOpsType> jb: binary.getComparisonOps()) {
            
                SQLQuery query = new SQLQuery(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
                if (operator.equalsIgnoreCase("OR")) {
                    query.nbField = nbField -1;
                    query.createSelect();
                    queryBuilder.append('(').append(query.getQuery());
                    queryBuilder.append(") UNION ");
                     executeSelect = false;
                } else {
                    
                    queryBuilder.append(query.getQuery());
                    queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
                }
            }
            
            // we treat logical Operators like AND, OR, ...
            for (JAXBElement<? extends LogicOpsType> jb: binary.getLogicOps()) {
            
                boolean writeOperator = true;
                
                SQLQuery query   = treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb);
                String subQuery  = query.getQuery();
                Filter subFilter = query.getSpatialFilter();
                    
                //if the sub spatial query contains both term search and spatial search we create a subQuery 
                if ((subFilter != null && !subQuery.equals("")) 
                    || query.getSubQueries().size() != 0) {
                        
                    subQueries.add(query);
                    writeOperator = false;
                } else {
                        
                    if (subQuery.equals("")) {
                        writeOperator = false;
                    } else  {
                        if (operator.equalsIgnoreCase("OR")) {
                            query.nbField = nbField -1;
                            query.createSelect();
                            queryBuilder.append('(').append(query.getQuery());
                            queryBuilder.append(") UNION ");
                            executeSelect = false;
                        } else {
                            queryBuilder.append(subQuery);
                        }
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
                
                boolean writeOperator = true;
                //for the spatial filter we don't need to write into the lucene query 
                filters.add(treatSpatialOperator((JAXBElement<? extends SpatialOpsType>)jb));
                writeOperator = false;
                
                if (writeOperator) {
                    queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
                } else {
                    writeOperator = true;
                }
            }
                
          // we remove the last Operator and add a ') '
          int pos;
          if (operator.equalsIgnoreCase("OR"))  
              pos = queryBuilder.length()- (10);
          else
              pos = queryBuilder.length()- (operator.length() + 2);
          
          if (pos > 0)
            queryBuilder.delete(pos, queryBuilder.length());
          
                
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
                SQLQuery sq  = treatLogicalOperator(unary.getLogicOps());
                String subQuery  = sq.getQuery();
                Filter subFilter = sq.getSpatialFilter();
                    
               /* if ((sq.getLogicalOperator() == SerialChainFilter.OR && subFilter != null && !subQuery.equals("")) ||
                    (sq.getLogicalOperator() == SerialChainFilter.NOT)) {
                    subQueries.add(sq);
                   
                  } else {*/

                if (!subQuery.equals("")) {
                    queryBuilder.append(subQuery);
                }
                if (subFilter != null) {
                    filters.add(sq.getSpatialFilter());
                }
                  //}
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
        
            
        SQLQuery response = new SQLQuery(query, spatialFilter);
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
    protected String treatComparisonOperator(final JAXBElement<? extends ComparisonOpsType> JBComparisonOps) throws CstlServiceException {
        StringBuilder response = new StringBuilder();
        
        ComparisonOpsType comparisonOps = JBComparisonOps.getValue();
        
        if (comparisonOps instanceof PropertyIsLikeType ) {
            PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            String propertyName    = "";
            //we get the field
            if (pil.getPropertyName() != null) {
                propertyName = pil.getPropertyName().getContent();
                response.append('v').append(nbField).append(".path ='").append(transformSyntax(propertyName)).append("' AND ");
                response.append('v').append(nbField).append("value LIKE '");
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
            //we get the value of the field
            if (pil.getLiteral() != null && pil.getLiteral() != null) {
                
                //we format the value by replacing the specified special char by the lucene special char
                String brutValue = pil.getLiteral();
                brutValue = brutValue.replace(pil.getWildCard(),    "%");
                brutValue = brutValue.replace(pil.getSingleChar(),  "%"); //TODO find this in SQL
                brutValue = brutValue.replace(pil.getEscapeChar(),  "\\");// SAME
                
                //for a date we remove the '-'
                if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        brutValue = brutValue.replaceAll("-", "");
                        brutValue = brutValue.replace("Z", "");
                }
                
                response.append(brutValue).append("' ");
                response.append(" AND v").append(nbField).append(".form=identifier ");
                
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the literal value.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
        } else if (comparisonOps instanceof PropertyIsNullType) {
             PropertyIsNullType pin = (PropertyIsNullType) comparisonOps;

            //we get the field
            if (pin.getPropertyName() != null) {
                response.append('v').append(nbField).append(".path = '").append(transformSyntax(pin.getPropertyName().getContent())).append("' AND ");
                response.append('v').append(nbField).append(".value IS NULL ");
                response.append(" AND v").append(nbField).append(".form=identifier ");
            } else {
                throw new CstlServiceException("An operator propertyIsNull must specified the propertyName.",
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
                throw new CstlServiceException("A binary comparison operator must be constitued of a literal and a property name.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } else {
                if (operator.equals("PropertyIsEqualTo")) {                
                    response.append('v').append(nbField).append(".path = '").append(transformSyntax(propertyName)).append("' AND ");
                    response.append('v').append(nbField).append(".value='").append(literal.getStringValue()).append("' ");
                    response.append(" AND v").append(nbField).append(".form=identifier ");
                
                } else if (operator.equals("PropertyIsNotEqualTo")) {
                    
                   response.append('v').append(nbField).append(".path = '").append(transformSyntax(propertyName)).append("' AND ");
                   response.append('v').append(nbField).append(".value != '").append(literal.getStringValue()).append("' ");
                   response.append(" AND v").append(nbField).append(".form=identifier ");
                
                } else if (operator.equals("PropertyIsGreaterThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replace("Z", "");
                        response.append('v').append(nbField).append(".path = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".value >= '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".form=identifier ");
                    } else {
                        throw new CstlServiceException("PropertyIsGreaterThanOrEqualTo operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified") || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replace("Z", "");
                        response.append('v').append(nbField).append(".path = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".value > '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".form=identifier ");
                    } else {
                        throw new CstlServiceException("PropertyIsGreaterThan operator works only on Date field. " + operator,
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
                            throw new CstlServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replace("Z", "");
                        response.append('v').append(nbField).append(".path = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".value < '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".form=identifier ");
                    } else {
                        throw new CstlServiceException("PropertyIsLessThan operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")) {
                        String dateValue = literal.getStringValue();
                        try {
                            if (dateValue.indexOf("CEST") != -1)
                                dateValue = createDate(dateValue);
                        } catch( ParseException ex) {
                            throw new CstlServiceException("The service was unable to parse the Date: " + dateValue,
                                                             INVALID_PARAMETER_VALUE, "QueryConstraint");
                        }
                        dateValue = dateValue.replace("Z", "");
                        response.append('v').append(nbField).append(".path = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".value <= '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".form=identifier ");
                    } else {
                         throw new CstlServiceException("PropertyIsLessThanOrEqualTo operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, "QueryConstraint");
                    }
                } else {
                    throw new CstlServiceException("Unkwnow comparison operator: " + operator,
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint");
                }
            }
        }
        nbField++;
        return response.toString();
    }
    
    /**
     * Build a piece of lucene query with the specified Spatial filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> JBSpatialOps) throws CstlServiceException {
        SpatialFilter spatialfilter = null;
        
        SpatialOpsType spatialOps = JBSpatialOps.getValue();
        
        if (spatialOps instanceof BBOXType) {
            BBOXType bbox       = (BBOXType) spatialOps;
            String propertyName = bbox.getPropertyName();
            String CRSName      = bbox.getSRS();
            
            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new CstlServiceException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } else if (!propertyName.contains("BoundingBox")) {
                throw new CstlServiceException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new CstlServiceException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (CRSName == null) {
                throw new CstlServiceException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
            //we transform the EnvelopeEntry in GeneralEnvelope
            double min[] = {bbox.getMinX(), bbox.getMinY()};
            double max[] = {bbox.getMaxX(), bbox.getMaxY()};
            try {
                GeneralEnvelope envelope      = new GeneralEnvelope(min, max);
                CoordinateReferenceSystem crs = CRS.decode(CRSName, true);
                envelope.setCoordinateReferenceSystem(crs);
                spatialfilter = new BBOXFilter(envelope, CRSName);
                
            } catch (NoSuchAuthorityCodeException e) {
                throw new CstlServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (FactoryException e) {
                throw new CstlServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (IllegalArgumentException e) {
                throw new CstlServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
        } else if (spatialOps instanceof DistanceBufferType) {
            
            DistanceBufferType dist = (DistanceBufferType) spatialOps;
            double distance         = dist.getDistance();
            String units            = dist.getDistanceUnits();
            JAXBElement JBgeom      = dist.getAbstractGeometry();
            String operator         = JBSpatialOps.getName().getLocalPart();
           
            //we verify that all the parameters are specified
            if (dist.getPropertyName() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (units == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            if (JBgeom == null || JBgeom.getValue() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified a geometric object.",
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
                if (operator.equals("DWithin")) {
                    spatialfilter = new DWithinFilter(geometry, CRSName, distance, units);
                } else if (operator.equals("Beyond")) {
                    spatialfilter = new BeyondFilter(geometry, CRSName, distance, units);
                } else {
                    throw new CstlServiceException("Unknow DistanceBuffer operator.",
                            INVALID_PARAMETER_VALUE, "QueryConstraint");
                }
               
            } catch (NoSuchAuthorityCodeException e) {
                    throw new CstlServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (FactoryException e) {
                    throw new CstlServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (IllegalArgumentException e) {
                    throw new CstlServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
           
        } else if (spatialOps instanceof BinarySpatialOpType) {
            
            BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;
                        
            String propertyName = null;
            String operator     = JBSpatialOps.getName().getLocalPart();
            operator            = operator.toUpperCase();
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
                throw new CstlServiceException("An Binarary spatial operator must specified a propertyName and a geometry.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            SpatialFilterType filterType = null;
            try {
                filterType = SpatialFilterType.valueOf(operator);
            } catch (IllegalArgumentException ex) {
                logger.severe("unknow spatial filter Type");
            }
            if (filterType == null) {
                throw new CstlServiceException("Unknow FilterType: " + operator,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            }
            
            String CRSName = "undefined CRS";
            try {
                Object filterGeometry = null;
                if (geometry instanceof EnvelopeEntry) {

                    //we transform the EnvelopeEntry in GeneralEnvelope
                    EnvelopeEntry GMLenvelope = (EnvelopeEntry)geometry;
                    CRSName                   = GMLenvelope.getSrsName();
                    filterGeometry            = GMLenvelopeToGeneralEnvelope(GMLenvelope);

                } else if (geometry instanceof PointType) {
                    PointType GMLpoint        = (PointType) geometry;
                    CRSName                   = GMLpoint.getSrsName();
                    filterGeometry            = GMLpointToGeneralDirectPosition(GMLpoint);

                } else if (geometry instanceof LineStringType) {
                    LineStringType GMLline =  (LineStringType) geometry;
                    CRSName                = GMLline.getSrsName();
                    filterGeometry         = GMLlineToline2d(GMLline);
                }

                switch (filterType) {
                    case CONTAINS   : spatialfilter = new ContainsFilter(filterGeometry, CRSName);  break;
                    case CROSSES    : spatialfilter = new CrossesFilter(filterGeometry, CRSName);   break;
                    case DISJOINT   : spatialfilter = new DisjointFilter(filterGeometry, CRSName);  break;
                    case EQUALS     : spatialfilter = new EqualsFilter(filterGeometry, CRSName);    break;
                    case INTERSECTS : spatialfilter = new IntersectFilter(filterGeometry, CRSName); break;
                    case OVERLAPS   : spatialfilter = new OverlapsFilter(filterGeometry, CRSName);  break;
                    case TOUCHES    : spatialfilter = new TouchesFilter(filterGeometry, CRSName);   break;
                    case WITHIN     : spatialfilter = new WithinFilter(filterGeometry, CRSName);    break;
                }

            } catch (NoSuchAuthorityCodeException e) {
                throw new CstlServiceException("Unknow Coordinate Reference System: " + CRSName,
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (FactoryException e) {
                throw new CstlServiceException("Factory exception while parsing spatial filter BBox: " + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint");
            } catch (IllegalArgumentException e) {
                throw new CstlServiceException("The dimensions of the bounding box are incorrect: " + e.getMessage(),
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
     * Format the propertyName from ebrim syntax to mdweb syntax.
     */
    private String transformSyntax(String s) {
        if (s.indexOf(':') != -1) {
            String prefix = s.substring(0, s.indexOf(':'));
            s = s.replace(prefix, getStandardFromPrefix(prefix));
        }
        // we replace the variableName
        for (String varName : variables.keySet()) {
            QName var =  variables.get(varName);
            String mdwebVar = getStandardFromNamespace(var.getNamespaceURI()) + ':' + var.getLocalPart();
            s = s.replace("$" + varName,  mdwebVar);
        }
        // we replace the ebrim separator /@ by :
        s = s.replace("/@", ":");
        return s;
    }
    
    private String getStandardFromNamespace(String namespace) {
        if (namespace.equals("http://www.opengis.net/cat/wrs/1.0"))
            return Standard.WRS.getName();
        else if (namespace.equals("http://www.opengis.net/cat/wrs"))
            return Standard.WRS_V09.getName();
        else if (namespace.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5"))
            return Standard.EBRIM_V2_5.getName();
        else if (namespace.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"))
            return Standard.EBRIM_V3.getName();
        else 
            throw new IllegalArgumentException("unexpected namespace: " + namespace);
    }
    
    private String getStandardFromPrefix(String prefix) {
        if (prefixs != null) {
            String namespace = prefixs.get(prefix);
            return getStandardFromNamespace(namespace);
        } 
        return null;
    }

    public void setVariables(Map<String, QName> variables) {
        this.variables = variables;
    }

    public void setPrefixs(Map<String, String> prefixs) {
        this.prefixs = prefixs;
    }
}
