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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.text.ParseException;

// Apache Lucene dependencies
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.lucene.search.Filter;

// constellation dependencies
import org.constellation.ws.CstlServiceException;
import static org.constellation.metadata.CSWConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.ogc.xml.v110.BinaryLogicOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLikeType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.ogc.xml.v110.UnaryLogicOpType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.schemas.Standard;


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

    private static final DateFormat dateFormatter;
    static {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SQLQuery getNullFilter() {
        return new SQLQuery("Select \"identifier\", \"catalog\" from \"Forms\" where \"catalog\" != 'MDATA");
    }
    
     /**
     * Build a lucene request from the specified Filter.
     * 
     * @param filter a Filter object build directly from the XML or from a CQL request
     */
    @Override
    protected SQLQuery getQuery(final FilterType filter, Map<String, QName> variables, Map<String, String> prefixs) throws CstlServiceException {
        this.variables    = variables;
        this.prefixs      = prefixs;
        executeSelect     = true;
        SQLQuery response = null;
        if (filter != null) { 
            // we treat logical Operators like AND, OR, ...
            if (filter.getLogicOps() != null) {
                response = treatLogicalOperator(filter.getLogicOps());
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...    
            } else if (filter.getComparisonOps() != null) {
                nbField                          = 1;
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
     * {@inheritDoc}
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
                if ((subFilter != null && !subQuery.isEmpty()) || !query.getSubQueries().isEmpty()) {
                        
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
                queryBuilder.append(treatComparisonOperator(reverseComparisonOperator(unary.getComparisonOps())));
                
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
        final Filter spatialFilter = getSpatialFilterFromList(logicalOperand, filters);
        final SQLQuery response    = new SQLQuery(query, spatialFilter);
        response.setSubQueries(subQueries);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addComparisonFilter(StringBuilder response, String propertyName, String literalValue, String operator) {
        response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName)).append("' AND ");
        response.append('v').append(nbField).append(".\"value\" ").append(operator);
        if (!operator.equals("IS NULL ")) {
            response.append("'").append(literalValue).append("' ");
        }
        response.append(" AND v").append(nbField).append(".\"form\"=\"identifier\" ");
        nbField++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addDateComparisonFilter(StringBuilder response, String propertyName, String literalValue, String operator) throws CstlServiceException {
        if (isDateField(propertyName)) {
            final String dateValue = extractDateValue(literalValue);
            addComparisonFilter(response, propertyName, dateValue, operator);
        } else {
            throw new CstlServiceException(operator + " operator works only on Date field.",
                    OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String extractDateValue(String literal) throws CstlServiceException {
        try {
            synchronized (dateFormatter) {
                final Date d = TemporalUtilities.parseDate(literal);
                return dateFormatter.format(d);
            }
        } catch (ParseException ex) {
            throw new CstlServiceException(PARSE_ERROR_MSG + literal, INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String translateSpecialChar(PropertyIsLikeType pil) {
        return translateSpecialChar(pil, "%", "%", "\\");
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
}
