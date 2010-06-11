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
import javax.xml.namespace.QName;
import java.text.ParseException;

// Apache Lucene dependencies
import org.apache.lucene.search.Filter;

// constellation dependencies
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.csw.xml.QueryConstraint;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.metadata.CSWConstants.*;

// Geotoolkit dependencies

// MDWeb dependencies

import org.geotoolkit.lucene.filter.SerialChainFilter;

import org.geotoolkit.ogc.xml.v110.AbstractIdType;

import org.geotoolkit.ogc.xml.v110.BinaryComparisonOpType;
import org.geotoolkit.ogc.xml.v110.BinaryLogicOpType;

import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;

import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.PropertyIsBetweenType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLikeType;
import org.geotoolkit.ogc.xml.v110.PropertyIsNullType;

import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.ogc.xml.v110.UnaryLogicOpType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.mdweb.model.schemas.Standard;

;

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
    @Override
    public SQLQuery getQuery(final QueryConstraint constraint, Map<String, QName> variables, Map<String, String> prefixs) throws CstlServiceException {
        this.setVariables(variables);
        this.setPrefixs(prefixs);
        FilterType filter = null;
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            return new SQLQuery("Select \"identifier\", \"catalog\" from \"Forms\" where \"catalog\" != 'MDATA");
        } else {
            filter = getFilterFromConstraint(constraint);
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
        if (response != null) {
            response.nbField = nbField - 1;
            if (executeSelect)
                response.createSelect();
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
    protected SQLQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws CstlServiceException {
        final List<SQLQuery> subQueries  = new ArrayList<SQLQuery>();
        final StringBuilder queryBuilder = new StringBuilder();
        final LogicOpsType logicOps      = jbLogicOps.getValue();
        final String operator            = jbLogicOps.getName().getLocalPart();
        final List<Filter> filters       = new ArrayList<Filter>();
        nbField                          = 1;
        
        if (logicOps instanceof BinaryLogicOpType) {
            final BinaryLogicOpType binary = (BinaryLogicOpType) logicOps;
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...   
            for (JAXBElement<? extends ComparisonOpsType> jb: binary.getComparisonOps()) {
            
                final SQLQuery query = new SQLQuery(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
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
                
                final SQLQuery query   = treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb);
                final String subQuery  = query.getQuery();
                final Filter subFilter = query.getSpatialFilter();
                    
                //if the sub spatial query contains both term search and spatial search we create a subQuery 
                if ((subFilter != null && !subQuery.isEmpty())
                    || query.getSubQueries().size() != 0) {
                        
                    subQueries.add(query);
                    writeOperator = false;
                } else {
                        
                    if (subQuery.isEmpty()) {
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
              pos = queryBuilder.length()- 10;
          else
              pos = queryBuilder.length()- (operator.length() + 2);
          
          if (pos > 0)
            queryBuilder.delete(pos, queryBuilder.length());
          
                
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
                final SQLQuery sq  = treatLogicalOperator(unary.getLogicOps());
                final String subQuery  = sq.getQuery();
                final Filter subFilter = sq.getSpatialFilter();
                    
               /* if ((sq.getLogicalOperator() == SerialChainFilter.OR && subFilter != null && !subQuery.isEmpty()) ||
                    (sq.getLogicalOperator() == SerialChainFilter.NOT)) {
                    subQueries.add(sq);
                   
                  } else {*/

                if (!subQuery.isEmpty()) {
                    queryBuilder.append(subQuery);
                }
                if (subFilter != null) {
                    filters.add(sq.getSpatialFilter());
                }
            }
        }
        
        String query = queryBuilder.toString();
        if (query.equals("()")) {
            query = "";
        }

        final int logicalOperand   = SerialChainFilter.valueOf(operator);
        final Filter spatialFilter = getSpatialFilterFromList(logicalOperand, filters, query);
        final SQLQuery response    = new SQLQuery(query, spatialFilter);
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
                response.append('v').append(nbField).append(".\"path\" ='").append(transformSyntax(propertyName)).append("' AND ");
                response.append('v').append(nbField).append("\"value\" LIKE '");
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            
            //we get the value of the field
            if (pil.getLiteral() != null && pil.getLiteral() != null) {
                
                //we format the value by replacing the specified special char by the lucene special char
                String brutValue = pil.getLiteral();
                brutValue = brutValue.replace(pil.getWildCard(),    "%");
                brutValue = brutValue.replace(pil.getSingleChar(),  "%"); //TODO find this in SQL
                brutValue = brutValue.replace(pil.getEscapeChar(),  "\\");// SAME
                
                //for a date we remove the '-'
                if (isDateField(propertyName)) {
                        brutValue = brutValue.replaceAll("-", "");
                        brutValue = brutValue.replace("Z", "");
                }
                
                response.append(brutValue).append("' ");
                response.append(" AND v").append(nbField).append(".\"form\"=\"identifier\" ");
                
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the literal value.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
        } else if (comparisonOps instanceof PropertyIsNullType) {
             final PropertyIsNullType pin = (PropertyIsNullType) comparisonOps;

            //we get the field
            if (pin.getPropertyName() != null) {
                response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(pin.getPropertyName().getContent())).append("' AND ");
                response.append('v').append(nbField).append(".\"value\" IS NULL ");
                response.append(" AND v").append(nbField).append(".\"form\"=\"identifier\" ");
            } else {
                throw new CstlServiceException("An operator propertyIsNull must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
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
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else {
                if (operator.equals("PropertyIsEqualTo")) {                
                    response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
                    response.append('v').append(nbField).append(".\"value\"='").append(literal.getStringValue()).append("' ");
                    response.append(" AND v").append(nbField).append(".\"form\"=\"identifier\" ");
                
                } else if (operator.equals("PropertyIsNotEqualTo")) {
                    
                   response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
                   response.append('v').append(nbField).append(".\"value\" != '").append(literal.getStringValue()).append("' ");
                   response.append(" AND v").append(nbField).append(".form=identifier ");
                
                } else if (operator.equals("PropertyIsGreaterThanOrEqualTo")) {
                    if (isDateField(propertyName)) {
                        String dateValue = literal.getStringValue();
                        try {
                            dateValue = TemporalUtilities.parseDate(dateValue).toString();
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue, INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                        }
                        response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".\"value\" >= '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".\"form\"=\"identifier\" ");
                    } else {
                        throw new CstlServiceException("PropertyIsGreaterThanOrEqualTo operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (isDateField(propertyName)) {
                        String dateValue = literal.getStringValue();
                        try {
                            dateValue = TemporalUtilities.parseDate(dateValue).toString();
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue, INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                        }
                        response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".\"value\" > '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".\"form\"=\"identifier\" ");
                    } else {
                        throw new CstlServiceException("PropertyIsGreaterThan operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
                    }
                
                } else if (operator.equals("PropertyIsLessThan") ) {
                    if (isDateField(propertyName)) {
                        //if we are passed by CQL we must format the date
                        String dateValue = literal.getStringValue();
                        try {
                            dateValue = TemporalUtilities.parseDate(dateValue).toString();
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue, INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                        }
                        response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".\"value\" < '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".form=identifier ");
                    } else {
                        throw new CstlServiceException("PropertyIsLessThan operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (isDateField(propertyName)) {
                        String dateValue = literal.getStringValue();
                        try {
                            dateValue = TemporalUtilities.parseDate(dateValue).toString();
                        } catch( ParseException ex) {
                            throw new CstlServiceException(PARSE_ERROR_MSG + dateValue, INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                        }
                        response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
                        response.append('v').append(nbField).append(".\"value\" <= '").append(dateValue).append("' ");
                        response.append(" AND v").append(nbField).append(".form=identifier ");
                    } else {
                         throw new CstlServiceException("PropertyIsLessThanOrEqualTo operator works only on Date field. " + operator,
                                                          OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
                    }
                } else {
                    throw new CstlServiceException("Unkwnow comparison operator: " + operator,
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }
            }
        }
        nbField++;
        return response.toString();
    }
    
    private String treatIDOperator(final List<JAXBElement<? extends AbstractIdType>> jbIdsOps) {
        //TODO
        if (true) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return new StringBuilder().toString();
    }
    
    /**
     * Format the propertyName from ebrim syntax to mdweb syntax.
     */
    private String transformSyntax(String s) {
        if (s.indexOf(':') != -1) {
            final String prefix = s.substring(0, s.lastIndexOf(':'));
            s = s.replace(prefix, getStandardFromPrefix(prefix));
        }
        // we replace the variableName
        for (String varName : variables.keySet()) {
            final QName var       =  variables.get(varName);
            final String mdwebVar = getStandardFromNamespace(var.getNamespaceURI()) + ':' + var.getLocalPart();
            s = s.replace("$" + varName,  mdwebVar);
        }
        // we replace the ebrim separator /@ by :
        s = s.replace("/@", ":");
        return s;
    }

    /**
     * Return a MDweb standard representation from a namespace URI.
     * 
     * @param namespace
     * @return
     */
    private String getStandardFromNamespace(String namespace) {
        if ("http://www.opengis.net/cat/wrs/1.0".equals(namespace))
            return Standard.WRS.getName();
        else if ("http://www.opengis.net/cat/wrs".equals(namespace))
            return Standard.WRS_V09.getName();
        else if ("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5".equals(namespace))
            return Standard.EBRIM_V2_5.getName();
        else if ("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0".equals(namespace))
            return Standard.EBRIM_V3.getName();
        else 
            throw new IllegalArgumentException("unexpected namespace: " + namespace);
    }

    /**
     * Return a MDweb standard representation from a namespace URI or an abbreviated prefix.
     * @param prefix
     * @return
     */
    private String getStandardFromPrefix(String prefix) {
        if (prefixs != null) {
            final String namespace = prefixs.get(prefix);
            if (namespace == null) {
                return getStandardFromNamespace(prefix);
            } else {
                return getStandardFromNamespace(namespace);
            }
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
