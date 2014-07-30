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

import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.search.Filter;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.filter.FilterFactoryImpl;
import org.geotoolkit.filter.SpatialFilterType;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.geometry.jts.SRIDGenerator.Version;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.LineString;
import org.geotoolkit.gml.xml.Point;
import org.geotoolkit.gml.xml.Polygon;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.ogc.xml.v110.AbstractIdType;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.BinaryComparisonOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.LowerBoundaryType;
import org.geotoolkit.ogc.xml.v110.PropertyIsBetweenType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.ogc.xml.v110.TemporalOpsType;
import org.geotoolkit.ogc.xml.v110.UpperBoundaryType;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.BinaryTemporalOperator;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.TEquals;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotoolkit.lucene.filter.LuceneOGCFilter.GEOMETRY_PROPERTY;
import static org.geotoolkit.lucene.filter.LuceneOGCFilter.wrap;
import static org.geotoolkit.ogc.xml.FilterXmlFactory.buildPropertyIsEquals;
import static org.geotoolkit.ogc.xml.FilterXmlFactory.buildPropertyIsGreaterThan;
import static org.geotoolkit.ogc.xml.FilterXmlFactory.buildPropertyIsGreaterThanOrEqualTo;
import static org.geotoolkit.ogc.xml.FilterXmlFactory.buildPropertyIsLessThan;
import static org.geotoolkit.ogc.xml.FilterXmlFactory.buildPropertyIsLessThanOrEqualTo;
import static org.geotoolkit.ogc.xml.FilterXmlFactory.buildPropertyIsNotEquals;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;

// JAXB dependencies
// Apache Lucene dependencies
// Geotoolkit dependencies
// GeoAPI dependencies
// JTS dependencies


/**
 * Abstract class used to parse OGC filter and transform them into the specific implementation filter language.
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class FilterParser {

    protected static final String QUERY_CONSTRAINT = "QueryConstraint";
    
    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.metadata");

    protected static final String PARSE_ERROR_MSG = "The service was unable to parse the Date: ";

    protected static final String UNKNOW_CRS_ERROR_MSG = "Unknow Coordinate Reference System: ";

    protected static final String INCORRECT_BBOX_DIM_ERROR_MSG = "The dimensions of the bounding box are incorrect: ";

    protected static final String FACTORY_BBOX_ERROR_MSG = "Factory exception while parsing spatial filter BBox: ";
    
   /**
     * Build a Filter with the specified CQL query
     * 
     * @param cqlQuery A well-formed CQL query .
     */
    public static FilterType cqlToFilter(final String cqlQuery) throws CQLException, JAXBException {
        final FilterType result;
        final Object newFilter = CQL.parseFilter(cqlQuery, new FilterFactoryImpl());

        if (!(newFilter instanceof FilterType)) {
            result = new FilterType(newFilter);
        } else {
            result = (FilterType) newFilter;
        }
        return result;
    }

    /**
     * Build a CQL query with the specified Filter.
     *
     * @param filter A well-formed Filter .
     */
    public static String filterToCql(final FilterType filter) throws CQLException, JAXBException {
        return CQL.write(filter);
    }

    /**
     * Build a request from the specified constraint
     *
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public Object getQuery(final QueryConstraint constraint, final Map<String, QName> variables, final Map<String, String> prefixs, final List<QName> typeNames) throws FilterParserException {
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            return getNullFilter(typeNames);
        } else {
            final FilterType filter = getFilterFromConstraint(constraint);
            return getQuery(filter, variables, prefixs, typeNames);
        }
    }

    /**
     * Return a filter matching for all the records.
     * 
     * @return a filter matching for all the records.
     */
    protected abstract Object getNullFilter(final List<QName> typeNames);
    
    protected abstract Object getQuery(final FilterType constraint, final Map<String, QName> variables, final Map<String, String> prefixs, final List<QName> typeNames) throws FilterParserException;

    /**
     * Build a piece of query with the specified logical filter.
     *
     * @param jbLogicOps A logical filter.
     * @return
     * @throws FilterParserException
     */
    protected abstract Object treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws FilterParserException;
    
    /**
     * Build a piece of query with the specified Comparison filter.
     *
     * @param jbComparisonOps A comparison filter.
     * @return
     * @throws org.constellation.coverage.web.FilterParserException
     */
    protected String treatComparisonOperator(final ComparisonOpsType comparisonOps) throws FilterParserException {
        final StringBuilder response = new StringBuilder();

        if (comparisonOps instanceof PropertyIsLike ) {
            final PropertyIsLike pil = (PropertyIsLike) comparisonOps;
            final PropertyName propertyName;
            //we get the field
            if (pil.getExpression() != null && pil.getLiteral() != null) {
                propertyName = (PropertyName) pil.getExpression();
            } else {
                throw new FilterParserException("An operator propertyIsLike must specified the propertyName and a literal value.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            //we format the value by replacing the specified special char by the lucene special char
            final String brutValue = translateSpecialChar(pil);
            addComparisonFilter(response, propertyName, brutValue, "LIKE");


        } else if (comparisonOps instanceof PropertyIsNull) {
             final PropertyIsNull pin = (PropertyIsNull) comparisonOps;

            //we get the field
            if (pin.getExpression() != null) {
                addComparisonFilter(response, (PropertyName) pin.getExpression(), null, "IS NULL ");
            } else {
                throw new FilterParserException("An operator propertyIsNull must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
        } else if (comparisonOps instanceof PropertyIsBetweenType) {
            final PropertyIsBetweenType pib = (PropertyIsBetweenType) comparisonOps;
            final PropertyName propertyName = (PropertyName) pib.getExpression();
            final LowerBoundaryType low     = pib.getLowerBoundary();
            Literal lowLit = null;
            if (low != null) {
                lowLit = low.getLiteral();
            } 
            final UpperBoundaryType upp     = pib.getUpperBoundary();
            Literal uppLit = null;
            if (upp != null) {
                uppLit = upp.getLiteral();
            }
            if (propertyName == null || lowLit == null || uppLit == null) {
                throw new FilterParserException("A PropertyIsBetween operator must be constitued of a lower boundary containing a literal, "
                                             + "an upper boundary containing a literal and a property name.", INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else {
                addComparisonFilter(response, propertyName, (String)lowLit.getValue(), ">=");
                addComparisonFilter(response, propertyName, (String)uppLit.getValue(), "<=");
            }

        } else if (comparisonOps instanceof BinaryComparisonOperator) {

            final BinaryComparisonOperator bc = (BinaryComparisonOperator) comparisonOps;
            final PropertyName propertyName   = (PropertyName) bc.getExpression1();
            final Literal literal             = (Literal) bc.getExpression2();

            if (propertyName == null || literal == null) {
                throw new FilterParserException("A binary comparison operator must be constitued of a literal and a property name.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else {
                final Object literalValue = literal.getValue();

                if (bc instanceof PropertyIsEqualTo) {
                    addComparisonFilter(response, propertyName, literalValue, "=");

                } else if (bc instanceof PropertyIsNotEqualTo) {
                    addComparisonFilter(response, propertyName, literalValue, "!=");

                } else if (bc instanceof PropertyIsGreaterThanOrEqualTo) {
                    addComparisonFilter(response, propertyName, literalValue, ">=");

                } else if (bc instanceof PropertyIsGreaterThan) {
                    addComparisonFilter(response, propertyName, literalValue, ">");

                } else if (bc instanceof  PropertyIsLessThan) {
                    addComparisonFilter(response, propertyName, literalValue, "<");

                } else if (bc instanceof PropertyIsLessThanOrEqualTo) {
                    addComparisonFilter(response, propertyName, literalValue, "<=");

                } else {
                    final String operator = bc.getClass().getSimpleName();
                    throw new FilterParserException("Unkwnow comparison operator: " + operator,
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }
            }
        }
        return response.toString();
    }
    
    /**
     * Build a piece of query with the specified Comparison filter.
     *
     * @param jbComparisonOps A comparison filter.
     * @return
     * @throws org.constellation.coverage.web.FilterParserException
     */
    protected String treatTemporalOperator(final TemporalOpsType temporalOps) throws FilterParserException {
        final StringBuilder response = new StringBuilder();

        if (temporalOps instanceof BinaryTemporalOperator) {

            final BinaryTemporalOperator bc = (BinaryTemporalOperator) temporalOps;
            final PropertyName propertyName = (PropertyName) bc.getExpression1();
            final Literal literal           = (Literal) bc.getExpression2();

            if (propertyName == null || literal == null) {
                throw new FilterParserException("A binary temporal operator must be constitued of a TimeObject and a property name.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else {
                final Object literalValue = literal.getValue();

                if (bc instanceof TEquals) {
                    addComparisonFilter(response, propertyName, literalValue, "=");

                } else if (bc instanceof After) {
                    addComparisonFilter(response, propertyName, literalValue, ">");

                } else if (bc instanceof Before) {
                    addComparisonFilter(response, propertyName, literalValue, "<");

                } else if (bc instanceof During) {
                    
                    throw new FilterParserException("TODO during", INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                } else {
                    final String operator = bc.getClass().getSimpleName();
                    throw new FilterParserException("Unsupported temporal operator: " + operator,
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }
            }
        }
        return response.toString();
    }

    /**
     * Add to the StringBuilder a piece of query with the specified operator.
     *
     * @param response A stringBuilder containing the query.
     * @param propertyName The name of the property to filter.
     * @param literalValue The value of the filter.
     * @param operator The comparison operator.
     *
     * @throws FilterParserException
     */
    protected abstract void addComparisonFilter(final StringBuilder response, final PropertyName propertyName, final Object literalValue, final String operator) throws FilterParserException;

    /**
     * Extract and format a date representation from the specified String.
     * If the string is not a well formed date it will raise an exception.
     *
     * @param literal A Date representation.
     * @return A formatted date representation.
     * @throws FilterParserException if the specified string can not be parsed.
     */
    protected abstract String extractDateValue(final Object literal) throws FilterParserException;

    /**
     * Replace The special character in a literal value for a propertyIsLike filter,
     * with the implementation specific value.
     *
     * @param pil A propertyIsLike filter.
     * @param wildchar The character replacing the filter wildChar.
     * @param SingleChar The character replacing the filter singleChar.
     * @param escapeChar The character replacing the filter escapeChar.
     *
     * @return A formatted value.
     */
    protected String translateSpecialChar(final PropertyIsLike pil, final String wildchar, final String singleChar, final String escapeChar) {
        String brutValue = pil.getLiteral();
        brutValue = brutValue.replace(pil.getWildCard(), wildchar);
        brutValue = brutValue.replace(pil.getSingleChar(), singleChar);
        brutValue = brutValue.replace(pil.getEscape(), escapeChar);

        //for a date we remove the '-'
        if (isDateField((PropertyName) pil.getExpression())) {
            brutValue = brutValue.replaceAll("-", "");
            brutValue = brutValue.replace("Z", "");
        }
        return brutValue;
    }

    /**
     *  Replace The special character in a literal value for a propertyIsLike filter.
     *
     * @param pil propertyIsLike filter.
     * @return A formatted value.
     */
    protected abstract String translateSpecialChar(final PropertyIsLike pil);

    /**
     * Return a piece of query for An Id filter.
     *
     * @param jbIdsOps an Id filter
     * @return a piece of query.
     */
    protected String treatIDOperator(final List<JAXBElement<? extends AbstractIdType>> jbIdsOps) {
        //TODO
        if (true) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return "";
    }

    /**
     * Extract a OCG filter from the query constraint of the received request.
     * 
     * @param constraint
     * @return
     * @throws FilterParserException
     */
    protected FilterType getFilterFromConstraint(final QueryConstraint constraint) throws FilterParserException {
        
        //The null case must be trreated before calling this method
        if (constraint == null)  {
            throw new IllegalArgumentException("The null case must be already treated!");

        // both constraint type are filled we throw an exception
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new FilterParserException("The query constraint must be in Filter or CQL but not both.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        
        // none constraint type are filled we throw an exception
        } else if ((constraint.getCqlText() == null || constraint.getCqlText().isEmpty()) && constraint.getFilter() == null) {
            throw new FilterParserException("The query constraint must contain a Filter or a CQL query (not empty).",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        
        // for a CQL request we transform it in Filter
        } else if (constraint.getCqlText() != null) {
            try {
                return cqlToFilter(constraint.getCqlText());

            } catch (JAXBException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new FilterParserException("JAXBException while parsing CQL query: " + ex.getMessage(), NO_APPLICABLE_CODE, QUERY_CONSTRAINT);
            } catch (CQLException ex) {
                throw new FilterParserException("The CQL query is malformed: " + ex.getMessage(), INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (UnsupportedOperationException ex) {
                throw new FilterParserException("The CQL query is not supported: " + ex.getMessage(), INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            
        // for a filter we return directly it
        } else {
            return constraint.getFilter();
        }
    }

    /**
     * Return A single Filter concatening the list of specified Filter.
     *
     * @param logicalOperand A logical operator.
     * @param filters A List of lucene filter.
     *
     * @return A single Filter.
     */
    protected Filter getSpatialFilterFromList(final int logicalOperand, final List<Filter> filters) {

        Filter spatialFilter = null;
        if (filters.size() == 1) {

            if (logicalOperand == SerialChainFilter.NOT) {
                final int[] filterType = {SerialChainFilter.NOT};
                spatialFilter = new SerialChainFilter(filters, filterType);
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
        return spatialFilter;
    }

    /**
     * Build a lucene Filter query with the specified Spatial filter.
     *
     * @param JBlogicOps a spatial filter.
     * @return
     * @throws org.constellation.coverage.web.FilterParserException
     */
    protected Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> jbSpatialOps) throws FilterParserException {
        LuceneOGCFilter spatialfilter   = null;
        final SpatialOpsType spatialOps = jbSpatialOps.getValue();

        if (spatialOps instanceof BBOXType) {
            final BBOXType bbox       = (BBOXType) spatialOps;
            final String propertyName = bbox.getPropertyName();
            final String crsName      = bbox.getSRS();

            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new FilterParserException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else if (!propertyName.contains("BoundingBox")) {
                throw new FilterParserException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new FilterParserException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (crsName == null) {
                throw new FilterParserException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            //we transform the EnvelopeType in GeneralEnvelope
            spatialfilter = wrap(FF.bbox(GEOMETRY_PROPERTY, bbox.getMinX(), bbox.getMinY(),bbox.getMaxX(),bbox.getMaxY(),crsName));

        } else if (spatialOps instanceof DistanceBufferOperator) {

            final DistanceBufferOperator dist = (DistanceBufferOperator) spatialOps;
            final double distance             = dist.getDistance();
            final String units                = dist.getDistanceUnits();
            final Expression geom             = dist.getExpression2();
            final String operator             = jbSpatialOps.getName().getLocalPart();

            //we verify that all the parameters are specified
            if (dist.getExpression1() == null) {
                 throw new FilterParserException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (units == null) {
                 throw new FilterParserException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (geom == null) {
                 throw new FilterParserException("An distanceBuffer operator must specified a geometric object.",
                                                  INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            Geometry geometry = null;
            //String propName  = dist.getPropertyName().getPropertyName();
            String crsName   = null;

            // we transform the gml geometry in treatable geometry
            try {
                if (geom instanceof AbstractGeometry) {
                    final Point gmlGeom = (Point) geom;
                    crsName  = gmlGeom.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlGeom);

                } else if (geom instanceof Envelope) {
                    final Envelope gmlEnvelope = (Envelope) geom;
                    crsName  = gmlEnvelope.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlEnvelope);
                }
                
                if (geometry != null) {
                    final int srid = SRIDGenerator.toSRID(crsName, Version.V1);
                    geometry.setSRID(srid);
                }

                if ("DWithin".equals(operator)) {
                    spatialfilter = wrap(FF.dwithin(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else if ("Beyond".equals(operator)) {
                    spatialfilter = wrap(FF.beyond(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else {
                    throw new FilterParserException("Unknow DistanceBuffer operator.",
                            INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }

            } catch (NoSuchAuthorityCodeException e) {
                    throw new FilterParserException(UNKNOW_CRS_ERROR_MSG + crsName,
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                    throw new FilterParserException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                    throw new FilterParserException(INCORRECT_BBOX_DIM_ERROR_MSG+ e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

        } else if (spatialOps instanceof BinarySpatialOperator) {

            final BinarySpatialOperator binSpatial = (BinarySpatialOperator) spatialOps;

            String propertyName = null;
            String operator     = jbSpatialOps.getName().getLocalPart();
            operator            = operator.toUpperCase();
            Object gmlGeometry  = null;

            // the propertyName
            if (binSpatial.getExpression1() != null) {
                propertyName = ((PropertyName)binSpatial.getExpression1()).getPropertyName();
            }

            // geometric object: envelope
            if (binSpatial.getExpression2() instanceof Envelope) {
                gmlGeometry = binSpatial.getExpression2();
            }


            if (binSpatial.getExpression2() instanceof AbstractGeometry) {
                final AbstractGeometry ab =  (AbstractGeometry)binSpatial.getExpression2();

                // supported geometric object: point, line, polygon :
                if (ab instanceof Point || ab instanceof LineString || ab instanceof Polygon) {
                    gmlGeometry = ab;

                } else if (ab == null) {
                   throw new IllegalArgumentException("null value in BinarySpatialOp type");

                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + ab.getClass().getSimpleName());
                }
            }

            if (propertyName == null && gmlGeometry == null) {
                throw new FilterParserException("An Binarary spatial operator must specified a propertyName and a geometry.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            SpatialFilterType filterType = null;
            try {
                filterType = SpatialFilterType.valueOf(operator);
            } catch (IllegalArgumentException ex) {
                LOGGER.severe("unknow spatial filter Type");
            }
            if (filterType == null) {
                throw new FilterParserException("Unknow FilterType: " + operator,
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            String crsName = "undefined CRS";
            try {
                Geometry filterGeometry = null;
                if (gmlGeometry instanceof Envelope) {

                    //we transform the EnvelopeType in GeneralEnvelope
                    final Envelope gmlEnvelope = (Envelope)gmlGeometry;
                    crsName                    = gmlEnvelope.getSrsName();
                    filterGeometry             = GeometrytoJTS.toJTS(gmlEnvelope);

                } else if (gmlGeometry instanceof AbstractGeometry) {
                    final AbstractGeometry gmlGeom = (AbstractGeometry)gmlGeometry;
                    crsName                        = gmlGeom.getSrsName();
                    filterGeometry                 = GeometrytoJTS.toJTS(gmlGeom);

                }
                
                if (filterGeometry != null) {
                    final int srid = SRIDGenerator.toSRID(crsName, Version.V1);
                    filterGeometry.setSRID(srid);
                }

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
                throw new FilterParserException(UNKNOW_CRS_ERROR_MSG + crsName,
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                throw new FilterParserException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                throw new FilterParserException(INCORRECT_BBOX_DIM_ERROR_MSG + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

        }

        return spatialfilter;
    }
    

    /**
     * Return true is the specified property has to be treated as a date Field.
     *
     * @param propertyName A property name extract from a filter.
     * @return true is the specified property has to be treated as a date Field.
     */
    protected boolean isDateField(final PropertyName pName) {
        if (pName != null && pName.getPropertyName() != null) {
            String propertyName = pName.getPropertyName();
            final int semicolonPos = propertyName.lastIndexOf(':');
            if (semicolonPos != -1) {
                propertyName = propertyName.substring(semicolonPos + 1);
            }
            return propertyName.contains("Date") || propertyName.contains("Modified")  || propertyName.contains("date")
                || propertyName.equalsIgnoreCase("TempExtent_begin") || propertyName.equalsIgnoreCase("TempExtent_end");
        }
        return false;
    }

    /**
     * In the case of a NOT operator containing a comparison operator, the easiest way is to
     * reverse the comparison operator.
     * example: NOT PropertyIsLessOrEqualsThan => PropertyIsGreaterThan
     *          NOT PropertyIsLessThan         => PropertyIsGreaterOrEqualsThan
     *
     * @param c The comparison operator to reverse.
     * @return The reversed comparison Operator
     * 
     * @throws FilterParserException
     */
    protected ComparisonOpsType reverseComparisonOperator(final ComparisonOpsType c) throws FilterParserException {
        String operator;
        if (c != null) {
            operator = c.getClass().getSimpleName();
        } else {
            operator = "null";
        }
        if (c instanceof BinaryComparisonOpType) {
            final BinaryComparisonOpType bc = (BinaryComparisonOpType) c;

            if (c instanceof PropertyIsEqualTo) {
                return (ComparisonOpsType) buildPropertyIsNotEquals("1.1.0",  bc.getPropertyName(), bc.getLiteral(), Boolean.TRUE);

            } else if (c instanceof  PropertyIsNotEqualTo) {
                return (ComparisonOpsType) buildPropertyIsEquals("1.1.0", bc.getPropertyName(), bc.getLiteral(), Boolean.TRUE);

            } else if (c instanceof PropertyIsGreaterThanOrEqualTo) {
                return (ComparisonOpsType) buildPropertyIsLessThan("1.1.0", bc.getPropertyName(), bc.getLiteral(), Boolean.TRUE);

            } else if (c instanceof PropertyIsGreaterThan) {
                return (ComparisonOpsType) buildPropertyIsLessThanOrEqualTo("1.1.0", bc.getPropertyName(), bc.getLiteral(), Boolean.TRUE);

            } else if (c instanceof PropertyIsLessThan) {
                return (ComparisonOpsType) buildPropertyIsGreaterThanOrEqualTo("1.1.0", bc.getPropertyName(), bc.getLiteral(), Boolean.TRUE);

            } else if (c instanceof PropertyIsLessThanOrEqualTo) {
                return (ComparisonOpsType) buildPropertyIsGreaterThan("1.1.0", bc.getPropertyName(), bc.getLiteral(), Boolean.TRUE);

            } else {
                throw new FilterParserException("Unkwnow comparison operator: " + operator,
                        INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
        } else {
                throw new FilterParserException("Unsupported combinaison NOT + " + operator,
                        OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
            }
    }
}
