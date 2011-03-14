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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Apache Lucene dependencies
import org.apache.lucene.search.Filter;

// constellation dependencies
import org.constellation.ws.CstlServiceException;

import static org.constellation.metadata.CSWConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.filter.text.cql2.CQL;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.geometry.jts.SRIDGenerator.Version;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.LineStringType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SpatialFilterType;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.DistanceBufferType;
import org.geotoolkit.ogc.xml.v110.BinarySpatialOpType;
import org.geotoolkit.ogc.xml.v110.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LogicOpsType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.filter.FilterFactoryImpl;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.PolygonType;
import org.geotoolkit.ogc.xml.v110.LowerBoundaryType;
import org.geotoolkit.ogc.xml.v110.AbstractIdType;
import org.geotoolkit.ogc.xml.v110.BinaryComparisonOpType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.PropertyIsBetweenType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLikeType;
import org.geotoolkit.ogc.xml.v110.PropertyIsNotEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyIsGreaterThanOrEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLessThanType;
import org.geotoolkit.ogc.xml.v110.PropertyIsGreaterThanType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLessThanOrEqualToType;
import org.geotoolkit.ogc.xml.v110.UpperBoundaryType;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.lucene.filter.LuceneOGCFilter.*;

// GeoAPI dependencies
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.util.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

// JTS dependencies
import com.vividsolutions.jts.geom.Geometry;
import org.geotoolkit.util.logging.Logging;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsNotEqualTo;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class FilterParser {

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
        final Object newFilter = CQL.toFilter(cqlQuery, new FilterFactoryImpl());

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
        return CQL.toCQL(filter);
    }

    /**
     * Build a request from the specified constraint
     *
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public Object getQuery(final QueryConstraint constraint, final Map<String, QName> variables, final Map<String, String> prefixs) throws CstlServiceException {
        //if the constraint is null we make a null filter
        if (constraint == null)  {
            return getNullFilter();
        } else {
            final FilterType filter = getFilterFromConstraint(constraint);
            return getQuery(filter, variables, prefixs);
        }
    }

    /**
     * Return a filter matching for all the records.
     * 
     * @return a filter matching for all the records.
     */
    protected abstract Object getNullFilter();
    
    protected abstract Object getQuery(final FilterType constraint, final Map<String, QName> variables, final Map<String, String> prefixs) throws CstlServiceException;

    /**
     * Build a piece of query with the specified logical filter.
     *
     * @param jbLogicOps A logical filter.
     * @return
     * @throws CstlServiceException
     */
    protected abstract Object treatLogicalOperator(final JAXBElement<? extends LogicOpsType> jbLogicOps) throws CstlServiceException;
    
    /**
     * Build a piece of query with the specified Comparison filter.
     *
     * @param jbComparisonOps A comparison filter.
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected String treatComparisonOperator(final ComparisonOpsType comparisonOps) throws CstlServiceException {
        final StringBuilder response = new StringBuilder();

        if (comparisonOps instanceof PropertyIsLike ) {
            final PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            final PropertyName propertyName;
            //we get the field
            if (pil.getPropertyName() != null && pil.getLiteral() != null) {
                propertyName = (PropertyName) pil.getExpression();
            } else {
                throw new CstlServiceException("An operator propertyIsLike must specified the propertyName and a literal value.",
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
                throw new CstlServiceException("An operator propertyIsNull must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
        } else if (comparisonOps instanceof PropertyIsBetweenType) {
            final PropertyIsBetweenType pib = (PropertyIsBetweenType) comparisonOps;
            final PropertyName propertyName = (PropertyName) pib.getExpression();
            final LowerBoundaryType low     = pib.getLowerBoundary();
            final LiteralType lowLit        = low.getLiteral();
            final UpperBoundaryType upp     = pib.getUpperBoundary();
            final LiteralType uppLit        = upp.getLiteral();
            if (propertyName == null || lowLit == null || uppLit == null) {
                throw new CstlServiceException("A PropertyIsBetween operator must be constitued of a lower boundary containing a literal, "
                                             + "an upper boundary containing a literal and a property name.", INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else {
                addDateComparisonFilter(response, propertyName, lowLit.getStringValue(), ">=");
                addDateComparisonFilter(response, propertyName, uppLit.getStringValue(), "<=");
            }

        } else if (comparisonOps instanceof BinaryComparisonOperator) {

            final BinaryComparisonOperator bc = (BinaryComparisonOperator) comparisonOps;
            final PropertyName propertyName   = (PropertyName) bc.getExpression1();
            final Literal literal             = (Literal) bc.getExpression2();

            if (propertyName == null || literal == null) {
                throw new CstlServiceException("A binary comparison operator must be constitued of a literal and a property name.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else {
                final String literalValue = (String) literal.getValue();

                if (bc instanceof PropertyIsEqualTo) {
                    addComparisonFilter(response, propertyName, literalValue, "=");

                } else if (bc instanceof PropertyIsNotEqualTo) {
                    addComparisonFilter(response, propertyName, literalValue, "!=");

                } else if (bc instanceof PropertyIsGreaterThanOrEqualTo) {
                    addDateComparisonFilter(response, propertyName, literalValue, ">=");

                } else if (bc instanceof PropertyIsGreaterThan) {
                    addDateComparisonFilter(response, propertyName, literalValue, ">");

                } else if (bc instanceof  PropertyIsLessThan) {
                    addDateComparisonFilter(response, propertyName, literalValue, "<");

                } else if (bc instanceof PropertyIsLessThanOrEqualTo) {
                    addDateComparisonFilter(response, propertyName, literalValue, "<=");

                } else {
                    final String operator = bc.getClass().getSimpleName();
                    throw new CstlServiceException("Unkwnow comparison operator: " + operator,
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
     */
    protected abstract void addComparisonFilter(final StringBuilder response, final PropertyName propertyName, final String literalValue, final String operator);

    /**
     * Add to the StringBuilder a piece of query with the specified operator for a date property.
     *
     * @param response A stringBuilder containing the query.
     * @param propertyName The name of the date property to filter.
     * @param literalValue The value of the filter.
     * @param operator The comparison operator.
     * @throws CstlServiceException
     */
    protected abstract void addDateComparisonFilter(final StringBuilder response, final PropertyName propertyName, final String literalValue, final String operator) throws CstlServiceException;

    /**
     * Extract and format a date representation from the specified String.
     * If the string is not a well formed date it will raise an exception.
     *
     * @param literal A Date representation.
     * @return A formatted date representation.
     * @throws CstlServiceException if the specified string can not be parsed.
     */
    protected abstract String extractDateValue(final String literal) throws CstlServiceException;

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
     * @throws CstlServiceException
     */
    protected FilterType getFilterFromConstraint(final QueryConstraint constraint) throws CstlServiceException {
        
        //The null case must be trreated before calling this method
        if (constraint == null)  {
            throw new IllegalArgumentException("The null case must be already treated!");

        // both constraint type are filled we throw an exception
        } else if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new CstlServiceException("The query constraint must be in Filter or CQL but not both.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        
        // none constraint type are filled we throw an exception
        } else if (constraint.getCqlText() == null && constraint.getFilter() == null) {
            throw new CstlServiceException("The query constraint must contain a Filter or a CQL query.",
                    INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
        
        // for a CQL request we transform it in Filter
        } else if (constraint.getCqlText() != null) {
            try {
                return cqlToFilter(constraint.getCqlText());

            } catch (JAXBException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new CstlServiceException("JAXBException while parsing CQL query: " + ex.getMessage(), NO_APPLICABLE_CODE, QUERY_CONSTRAINT);
            } catch (CQLException ex) {
                throw new CstlServiceException("The CQL query is malformed: " + ex.getMessage() + '\n'
                                                 + "syntax Error: " + ex.getSyntaxError(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
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
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected Filter treatSpatialOperator(final JAXBElement<? extends SpatialOpsType> jbSpatialOps) throws CstlServiceException {
        LuceneOGCFilter spatialfilter   = null;
        final SpatialOpsType spatialOps = jbSpatialOps.getValue();

        if (spatialOps instanceof BBOXType) {
            final BBOXType bbox       = (BBOXType) spatialOps;
            final String propertyName = bbox.getPropertyName();
            final String crsName      = bbox.getSRS();

            //we verify that all the parameters are specified
            if (propertyName == null) {
                throw new CstlServiceException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } else if (!propertyName.contains("BoundingBox")) {
                throw new CstlServiceException("An operator the propertyName BBOX must be geometry valued. The property :" + propertyName + " is not.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new CstlServiceException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (crsName == null) {
                throw new CstlServiceException("An operator BBOX must specified a CRS (coordinate Reference system) fot the envelope.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            //we transform the EnvelopeType in GeneralEnvelope
            spatialfilter = wrap(FF.bbox(GEOMETRY_PROPERTY, bbox.getMinX(), bbox.getMinY(),bbox.getMaxX(),bbox.getMaxY(),crsName));

        } else if (spatialOps instanceof DistanceBufferType) {

            final DistanceBufferType dist = (DistanceBufferType) spatialOps;
            final double distance         = dist.getDistance();
            final String units            = dist.getDistanceUnits();
            final JAXBElement jbGeom      = dist.getAbstractGeometry();
            final String operator         = jbSpatialOps.getName().getLocalPart();

            //we verify that all the parameters are specified
            if (dist.getPropertyName() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (units == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified the ditance units.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            if (jbGeom == null || jbGeom.getValue() == null) {
                 throw new CstlServiceException("An distanceBuffer operator must specified a geometric object.",
                                                  INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
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
                    geometry = GeometrytoJTS.toJTS(gmlPoint);

                } else if (gml instanceof LineStringType) {
                    final LineStringType gmlLine =  (LineStringType) gml;
                    crsName  = gmlLine.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlLine);

                } else if (gml instanceof EnvelopeType) {
                    final EnvelopeType gmlEnvelope = (EnvelopeType) gml;
                    crsName  = gmlEnvelope.getSrsName();
                    geometry = GeometrytoJTS.toJTS(gmlEnvelope);
                }

                if ("DWithin".equals(operator)) {
                    spatialfilter = wrap(FF.dwithin(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else if ("Beyond".equals(operator)) {
                    spatialfilter = wrap(FF.beyond(GEOMETRY_PROPERTY,FF.literal(geometry),distance, units));
                } else {
                    throw new CstlServiceException("Unknow DistanceBuffer operator.",
                            INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
                }

            } catch (NoSuchAuthorityCodeException e) {
                    throw new CstlServiceException(UNKNOW_CRS_ERROR_MSG + crsName,
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                    throw new CstlServiceException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                     INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                    throw new CstlServiceException(INCORRECT_BBOX_DIM_ERROR_MSG+ e.getMessage(),
                                                      INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

        } else if (spatialOps instanceof BinarySpatialOpType) {

            final BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;

            String propertyName = null;
            String operator     = jbSpatialOps.getName().getLocalPart();
            operator            = operator.toUpperCase();
            Object gmlGeometry  = null;

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

                } else if (ab instanceof PolygonType) {
                    gmlGeometry     = (PolygonType) ab;

                } else if (ab == null) {
                   throw new IllegalArgumentException("null value in BinarySpatialOp type");

                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + ab.getClass().getSimpleName());
                }
            }

            if (propertyName == null && gmlGeometry == null) {
                throw new CstlServiceException("An Binarary spatial operator must specified a propertyName and a geometry.",
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
            SpatialFilterType filterType = null;
            try {
                filterType = SpatialFilterType.valueOf(operator);
            } catch (IllegalArgumentException ex) {
                LOGGER.severe("unknow spatial filter Type");
            }
            if (filterType == null) {
                throw new CstlServiceException("Unknow FilterType: " + operator,
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }

            String crsName = "undefined CRS";
            try {
                Geometry filterGeometry = null;
                if (gmlGeometry instanceof EnvelopeType) {

                    //we transform the EnvelopeType in GeneralEnvelope
                    final EnvelopeType gmlEnvelope = (EnvelopeType)gmlGeometry;
                    crsName                   = gmlEnvelope.getSrsName();
                    filterGeometry            = GeometrytoJTS.toJTS(gmlEnvelope);

                } else if (gmlGeometry instanceof AbstractGeometryType) {

                    crsName                   = ((AbstractGeometryType)gmlGeometry).getSrsName();
                    filterGeometry            = GeometrytoJTS.toJTS((AbstractGeometryType)gmlGeometry);

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
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (FactoryException e) {
                throw new CstlServiceException(FACTORY_BBOX_ERROR_MSG + e.getMessage(),
                                                 INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            } catch (IllegalArgumentException e) {
                throw new CstlServiceException(INCORRECT_BBOX_DIM_ERROR_MSG + e.getMessage(),
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
     * @throws CstlServiceException
     */
    protected ComparisonOpsType reverseComparisonOperator(final ComparisonOpsType c) throws CstlServiceException {
        String operator;
        if (c != null) {
            operator = c.getClass().getSimpleName();
        } else {
            operator = "null";
        }
        if (c instanceof BinaryComparisonOpType) {
            final BinaryComparisonOpType bc = (BinaryComparisonOpType) c;

            if (c instanceof PropertyIsEqualTo) {
                return new PropertyIsNotEqualToType(bc.getLiteral(), new PropertyNameType(bc.getPropertyName()), Boolean.TRUE);

            } else if (c instanceof  PropertyIsNotEqualTo) {
                return new PropertyIsEqualToType(bc.getLiteral(), new PropertyNameType(bc.getPropertyName()), Boolean.TRUE);

            } else if (c instanceof PropertyIsGreaterThanOrEqualTo) {
                return new PropertyIsLessThanType(bc.getLiteral(), new PropertyNameType(bc.getPropertyName()), Boolean.TRUE);

            } else if (c instanceof PropertyIsGreaterThan) {
                return new PropertyIsLessThanOrEqualToType(bc.getLiteral(), new PropertyNameType(bc.getPropertyName()), Boolean.TRUE);

            } else if (c instanceof PropertyIsLessThan) {
                return new PropertyIsGreaterThanOrEqualToType(bc.getLiteral(), new PropertyNameType(bc.getPropertyName()), Boolean.TRUE);

            } else if (c instanceof PropertyIsLessThanOrEqualTo) {
                return new  PropertyIsGreaterThanType(bc.getLiteral(), new PropertyNameType(bc.getPropertyName()), Boolean.TRUE);

            } else {
                throw new CstlServiceException("Unkwnow comparison operator: " + operator,
                        INVALID_PARAMETER_VALUE, QUERY_CONSTRAINT);
            }
        } else {
                throw new CstlServiceException("Unsupported combinaison NOT + " + operator,
                        OPERATION_NOT_SUPPORTED, QUERY_CONSTRAINT);
            }
    }
}
