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

// J2SE dependencies

import org.apache.lucene.search.Filter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.ogc.xml.v110.AndType;
import org.geotoolkit.ogc.xml.v110.BinaryLogicOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.OrType;
import org.geotoolkit.ogc.xml.v110.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.ogc.xml.v110.TemporalOpsType;
import org.geotoolkit.ogc.xml.v110.UnaryLogicOpType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.PropertyName;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;

// JAXB dependencies
// Lucene dependencies
// GeoAPI dependencies
// geotoolkit dependencies



/**
 * A parser for filter 1.1.0 and CQL 2.0
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneFilterParser extends FilterParser {

    private static final String DEFAULT_FIELD = "metafile:doc";

    private static final DateFormat LUCENE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    static {
        LUCENE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpatialQuery getNullFilter(final List<QName> typeNames) {
        final Filter nullFilter = null;
        if (typeNames == null || typeNames.isEmpty()) {
            return new SpatialQuery(DEFAULT_FIELD, nullFilter, SerialChainFilter.AND);
        } else {
            final String query = getTypeQuery(typeNames);
            return new SpatialQuery(query, nullFilter, SerialChainFilter.AND);
        }
    }
    
    private String getTypeQuery(final List<QName> typeNames) {
        if (typeNames != null && !typeNames.isEmpty()) {
            StringBuilder query = new StringBuilder();
            for (QName typeName : typeNames) {
                query.append("objectType:").append(typeName.getLocalPart()).append(" OR ");
            }
            final int length = query.length();
            query.delete(length -3, length);
            return query.toString();
        }
        return "";
    }
    
    private Object getTypeFilter(final List<QName> typeNames) {
        if (typeNames != null && !typeNames.isEmpty()) {
            if (typeNames.size() == 1) {
                final QName typeName = typeNames.get(0);
                return new PropertyIsEqualToType(new LiteralType(typeName.getLocalPart()), new PropertyNameType("objectType"), Boolean.FALSE);
            } else {
                final List<Object> operators = new ArrayList<Object>();
                for (QName typeName : typeNames) {
                    operators.add(new PropertyIsEqualToType(new LiteralType(typeName.getLocalPart()), new PropertyNameType("objectType"), Boolean.FALSE));
                }
                return new OrType(operators.toArray());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpatialQuery getQuery(final FilterType queryFilter, final Map<String, QName> variables, final Map<String, String> prefixs, final List<QName> typeNames) throws FilterParserException {

        final Object typeFilter =  getTypeFilter(typeNames);
        final FilterType filter;
        if (typeFilter != null) {
            filter = new FilterType(new AndType(queryFilter, typeFilter));
        } else {
            filter = queryFilter;
        }
        SpatialQuery response = null;
        if (filter != null) {
            // we treat logical Operators like AND, OR, ...
            if (filter.getLogicOps() != null) {
                response = treatLogicalOperator(filter.getLogicOps());

            // we treat directly comparison operator: PropertyIsLike, IsNull, IsBetween, ...
            } else if (filter.getComparisonOps() != null) {
                response = new SpatialQuery(treatComparisonOperator(filter.getComparisonOps().getValue()), null, SerialChainFilter.AND);

            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...
            } else if (filter.getSpatialOps() != null) {
                response = new SpatialQuery("", treatSpatialOperator(filter.getSpatialOps()), SerialChainFilter.AND);

            // we treat time operator: TimeAfter, TimeBefore, TimeDuring, ...
            } else if (filter.getTemporalOps()!= null) {
                response = new SpatialQuery(treatTemporalOperator(filter.getTemporalOps().getValue()), null, SerialChainFilter.AND);

            } else if (filter.getId() != null && !filter.getId().isEmpty()) {
                response = new SpatialQuery(treatIDOperator(filter.getId()), null, SerialChainFilter.AND);
            }
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpatialQuery treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws FilterParserException {
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

                queryBuilder.append(treatComparisonOperator(jb.getValue()));
                queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
            }
            
            // we treat temporal constraint : TAfter, TBefore, ...
            for (JAXBElement<? extends TemporalOpsType> jb: binary.getTemporalOps()) {

                queryBuilder.append(treatTemporalOperator(jb.getValue()));
                queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
            }

            // we treat logical Operators like AND, OR, ...
            for (JAXBElement<? extends LogicOpsType> jb: binary.getLogicOps()) {

                boolean writeOperator = true;

                final SpatialQuery sq  = treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb);
                final String subQuery  = sq.getQuery();
                final Filter subFilter = sq.getSpatialFilter();

                //if the sub spatial query contains both term search and spatial search we create a subQuery
                if ((subFilter != null && !subQuery.equals(DEFAULT_FIELD))
                    || !sq.getSubQueries().isEmpty()
                    || (sq.getLogicalOperator() == SerialChainFilter.NOT && sq.getSpatialFilter() == null)) {
                    subQueries.add(sq);
                    writeOperator = false;
                } else {

                    if (subQuery.isEmpty()) {
                        writeOperator = false;
                    } else  {
                        queryBuilder.append(subQuery);
                    }
                    if (subFilter != null) {
                        filters.add(subFilter);
                    }
                }

                if (writeOperator) {
                    queryBuilder.append(" ").append(operator.toUpperCase()).append(" ");
                }
            }

            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...
            for (JAXBElement<? extends SpatialOpsType> jb: binary.getSpatialOps()) {

                //for the spatial filter we don't need to write into the lucene query
                filters.add(treatSpatialOperator((JAXBElement<? extends SpatialOpsType>)jb));
            }
            
            // we remove the last Operator and add a ') '
            final int pos = queryBuilder.length()- (operator.length() + 2);
            if (pos > 0) {
                queryBuilder.delete(queryBuilder.length()- (operator.length() + 2), queryBuilder.length());
            }

            queryBuilder.append(')');

        } else if (logicOps instanceof UnaryLogicOpType) {
            final UnaryLogicOpType unary = (UnaryLogicOpType) logicOps;

            // we treat comparison operator: PropertyIsLike, IsNull, IsBetween, ...
            if (unary.getComparisonOps() != null) {
                queryBuilder.append(treatComparisonOperator(unary.getComparisonOps().getValue()));

            // we treat spatial constraint : BBOX, Beyond, Overlaps, ...
            } else if (unary.getSpatialOps() != null) {

                filters.add(treatSpatialOperator(unary.getSpatialOps()));


             // we treat logical Operators like AND, OR, ...
            } else if (unary.getLogicOps() != null) {
                final SpatialQuery sq  = treatLogicalOperator(unary.getLogicOps());
                final String subQuery  = sq.getQuery();
                final Filter subFilter = sq.getSpatialFilter();

                if ((sq.getLogicalOperator() == SerialChainFilter.OR && subFilter != null && !subQuery.equals(DEFAULT_FIELD)) ||
                    (sq.getLogicalOperator() == SerialChainFilter.NOT)) {
                    subQueries.add(sq);

                } else {

                    if (!subQuery.isEmpty()) {
                        queryBuilder.append(subQuery);
                    }
                    if (subFilter != null) {
                        filters.add(sq.getSpatialFilter());
                    }
                }
            }
        }

        String query = queryBuilder.toString();
        if ("()".equals(query)) {
            query = "";
        }
        
        int logicalOperand          = SerialChainFilter.valueOf(operator);
        final Filter spatialFilter  = getSpatialFilterFromList(logicalOperand, filters);

        // here the logical operand NOT is contained in the spatial filter
        if (query.isEmpty() && logicalOperand == SerialChainFilter.NOT) {
            logicalOperand = SerialChainFilter.AND;
        }
        final SpatialQuery response = new SpatialQuery(query, spatialFilter, logicalOperand);
        response.setSubQueries(subQueries);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addComparisonFilter(final StringBuilder response, final PropertyName propertyName, final Object literalValue, final String operator) throws FilterParserException {
        final String literal;
        final boolean isDate   = isDateField(propertyName);
        final boolean isNumber = literalValue instanceof Number;
        if (isNumber) {
            literal = literalValue.toString();
        } else if ( isDate && !"LIKE".equals(operator)) {
            literal = extractDateValue(literalValue);
        } else if ( literalValue == null) {
            literal = "null";
        } else {
            literal = literalValue.toString();
        }
        final char open;
        final char close;
        if (operator.indexOf('=') != -1) {
            open = '[';
            close = ']';
        } else {
            open = '{';
            close = '}';
        }
        if ("!=".equals(operator)) {
            response.append("metafile:doc NOT ");
        }
        response.append(removePrefix(propertyName.getPropertyName())).append(":");

        if ("LIKE".equals(operator)) {
            response.append('(').append(literal).append(')');
        } else if ("IS NULL ".equals(operator)) {
            response.append(literal);
        } else if ("<=".equals(operator) || "<".equals(operator)) {
            final String lowerBound;
            if (isDate) {
                lowerBound = "00000101000000 \"";
            } else if (isNumber){
                lowerBound = "-2147483648 TO ";
            } else {
                lowerBound = "0 \"";
            }
            if (isNumber) {
                response.append(open).append(lowerBound).append(literal).append(close);
            } else {
                response.append(open).append(lowerBound).append(literal).append('\"').append(close);
            }
        } else if (">=".equals(operator) || ">".equals(operator)) {
            final String upperBound;
            if (isDate) {
                upperBound = "\" 30000101000000";
            } else if (isNumber){
                upperBound = " TO 2147483648";
            } else {
                upperBound = "\" z";
            }

            if (isNumber) {
                response.append(open).append(literal).append(upperBound).append(close);
            } else {
                response.append(open).append('\"').append(literal).append(upperBound).append(close);
            }

            // Equals
        } else {
            if (isNumber) {
                // we make this because of an issue wiht numeric fields and equals method T
                // TODO ind a better way or fix
                response.append("[").append(literal).append(" TO ").append(literal).append("]");
            } else {
                response.append("\"").append(literal).append('"');
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String extractDateValue(final Object literal) throws FilterParserException {
        try {
            synchronized(LUCENE_DATE_FORMAT) {
                if (literal instanceof Date) {
                    return LUCENE_DATE_FORMAT.format((Date)literal);
                } else {
                    return LUCENE_DATE_FORMAT.format(TemporalUtilities.parseDate(String.valueOf(literal)));
                }
            }
        } catch (ParseException ex) {
            throw new FilterParserException(PARSE_ERROR_MSG + literal,
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String translateSpecialChar(final PropertyIsLike pil) {
        return translateSpecialChar(pil, "*", "?", "\\");
    }

    /**
     * Remove the prefix on propertyName.
     */
    private String removePrefix(String s) {
        if (s != null) {
            final int i = s.lastIndexOf(':');
            if ( i != -1) {
                s = s.substring(i + 1, s.length());
            }
        }
        return s;
    }
}
