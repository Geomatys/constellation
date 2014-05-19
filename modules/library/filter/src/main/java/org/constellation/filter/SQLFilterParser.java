/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

// Geotoolkit dependencies
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.ogc.xml.v110.BinaryLogicOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.ogc.xml.v110.UnaryLogicOpType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.PropertyName;


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

    private static final DateFormat DATE_FORMATTER;
    static {
        DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SQLQuery getNullFilter(final List<QName> typeNames) {
        // TODO use typeNames
        return new SQLQuery("Select \"identifier\" from \"Storage\".\"Records\" where \"recordSet\" != 'MDATA");
    }
    
     /**
     * Build a lucene request from the specified Filter.
     * 
     * @param filter a Filter object build directly from the XML or from a CQL request
     */
    @Override
    protected SQLQuery getQuery(final FilterType filter, Map<String, QName> variables, Map<String, String> prefixs, final List<QName> typeNames) throws FilterParserException {
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
                response = new SQLQuery(treatComparisonOperator(filter.getComparisonOps().getValue()));
                
            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...    
            } else if (filter.getSpatialOps() != null) {
                response = new SQLQuery(treatSpatialOperator(filter.getSpatialOps()));
                
            // we treat time operator: TimeAfter, TimeBefore, TimeDuring, ...
            } else if (filter.getTemporalOps()!= null) {
                response = new SQLQuery(treatTemporalOperator(filter.getTemporalOps().getValue()));

            } else if (filter.getId() != null) {
                response = new SQLQuery(treatIDOperator(filter.getId()));
            }  
        }
        if (response != null) {
            response.nbField = nbField - 1;
            if (executeSelect)
                response.createSelect();
        }
        // TODO use typeNames
        return response;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected SQLQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws FilterParserException {
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
            
                final SQLQuery query = new SQLQuery(treatComparisonOperator(jb.getValue()));
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
                queryBuilder.append(treatComparisonOperator(reverseComparisonOperator(unary.getComparisonOps().getValue())));
                
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
        if ("()".equals(query)) {
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
    protected void addComparisonFilter(StringBuilder response, PropertyName propertyName, Object literalValue, String operator) throws FilterParserException {
        response.append('v').append(nbField).append(".\"path\" = '").append(transformSyntax(propertyName.getPropertyName())).append("' AND ");
        response.append('v').append(nbField).append(".\"value\" ").append(operator);
        if (isDateField(propertyName)) {
            literalValue = extractDateValue(literalValue);
        }
        if (literalValue != null) {
            literalValue = literalValue.toString();
        } else {
            literalValue = "null";
        }
        if (!"IS NULL ".equals(operator)) {
            response.append("'").append(literalValue).append("' ");
        }
        response.append(" AND v").append(nbField).append(".\"form\"=\"accessionNumber\" ");
        nbField++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String extractDateValue(final Object literal) throws FilterParserException {
        try {
            synchronized (DATE_FORMATTER) {
                final Date d;
                if (literal instanceof Date) {
                    d = (Date)literal;
                } else {
                    d = TemporalUtilities.parseDate(String.valueOf(literal));
                }
                return DATE_FORMATTER.format(d);
            }
        } catch (ParseException ex) {
            throw new FilterParserException(PARSE_ERROR_MSG + literal, INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String translateSpecialChar(final PropertyIsLike pil) {
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
     * Return a MDweb standard name representation from a namespace URI.
     * 
     * @param namespace
     * @return
     */
    private String getStandardFromNamespace(final String namespace) {
        if ("http://www.opengis.net/cat/wrs/1.0".equals(namespace))
            return "Web Registry Service v1.0";
        else if ("http://www.opengis.net/cat/wrs".equals(namespace))
            return "Web Registry Service v0.9";
        else if ("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5".equals(namespace))
            return "Ebrim v2.5";
        else if ("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0".equals(namespace))
            return "Ebrim v3.0";
        else 
            throw new IllegalArgumentException("unexpected namespace: " + namespace);
    }

    /**
     * Return a MDweb standard representation from a namespace URI or an abbreviated prefix.
     * @param prefix
     * @return
     */
    private String getStandardFromPrefix(final String prefix) {
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
