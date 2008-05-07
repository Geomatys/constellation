/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.filter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import net.seagis.coverage.web.ExpressionType;
import net.seagis.ogc.AndType;
import net.seagis.ogc.ArithmeticOperatorsType;
import net.seagis.ogc.BBOXType;
import net.seagis.ogc.ComparisonOperatorsType;
import net.seagis.ogc.FeatureIdType;
import net.seagis.ogc.GmlObjectIdType;
import net.seagis.ogc.IdCapabilitiesType;
import net.seagis.ogc.LiteralType;
import net.seagis.ogc.LowerBoundaryType;
import net.seagis.ogc.OrType;
import net.seagis.ogc.PropertyIsBetweenType;
import net.seagis.ogc.PropertyIsEqualToType;
import net.seagis.ogc.PropertyIsGreaterThanOrEqualToType;
import net.seagis.ogc.PropertyIsGreaterThanType;
import net.seagis.ogc.PropertyIsLessThanOrEqualToType;
import net.seagis.ogc.PropertyIsLessThanType;
import net.seagis.ogc.PropertyIsLikeType;
import net.seagis.ogc.PropertyIsNotEqualToType;
import net.seagis.ogc.PropertyIsNullType;
import net.seagis.ogc.PropertyNameType;
import net.seagis.ogc.ScalarCapabilitiesType;
import net.seagis.ogc.SortPropertyType;
import net.seagis.ogc.SpatialCapabilitiesType;
import net.seagis.ogc.SpatialOperatorType;
import net.seagis.ogc.SpatialOperatorsType;
import net.seagis.ogc.UpperBoundaryType;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.capability.ArithmeticOperators;
import org.opengis.filter.capability.ComparisonOperators;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.capability.Functions;
import org.opengis.filter.capability.GeometryOperand;
import org.opengis.filter.capability.IdCapabilities;
import org.opengis.filter.capability.Operator;
import org.opengis.filter.capability.ScalarCapabilities;
import org.opengis.filter.capability.SpatialCapabilities;
import org.opengis.filter.capability.SpatialOperator;
import org.opengis.filter.capability.SpatialOperators;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.geometry.Geometry;



/**
 * This class is not yet utilisable. We must see if we want to align with opengis filter interface.
 * 
 * @author Guilhem Legal
 */
public class FilterFactoryImpl implements FilterFactory {

    private final Logger logger = Logger.getLogger("net.seagis.filter");
    
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
    
    public FeatureId featureId(String id) {
        return new FeatureIdType(id);
    }

    public GmlObjectId gmlObjectId(String id) {
        return new GmlObjectIdType(id);
    }

    public And and(Filter f, Filter g) {
        return new AndType(f, g);
    }

    public And and(List<Filter> f) {
        return new AndType(f);
    }

    public Or or(Filter f, Filter g) {
        return new OrType(f, g);
    }

    public Or or(List<Filter> f) {
        return new OrType(f);
    }

    public Not not(Filter f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Id id(Set<? extends Identifier> ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PropertyName property(String name) {
        return new PropertyNameType(name);
    }

    public PropertyIsBetween between(Expression expr, Expression lower, Expression upper) {
        return new PropertyIsBetweenType( (ExpressionType)    expr, 
                                          (LowerBoundaryType) lower, 
                                          (UpperBoundaryType) upper);
    }

    public PropertyIsEqualTo equals(Expression expr1, Expression expr2) {
        return new PropertyIsEqualToType((LiteralType) expr2, (PropertyNameType) expr1, null);
    }

    public PropertyIsEqualTo equal(Expression expr1, Expression expr2, boolean matchCase) {
        return new PropertyIsEqualToType((LiteralType) expr2, (PropertyNameType) expr1, matchCase);
    }

    public PropertyIsNotEqualTo notEqual(Expression expr1, Expression expr2, boolean matchCase) {
        return new PropertyIsNotEqualToType((LiteralType) expr2, (PropertyNameType) expr1, matchCase);
    }

    public PropertyIsGreaterThan greater(Expression expr1, Expression expr2) {
        return new PropertyIsGreaterThanType((LiteralType) expr2, (PropertyNameType) expr1, null);
    }

    public PropertyIsGreaterThanOrEqualTo greaterOrEqual(Expression expr1, Expression expr2) {
        return new PropertyIsGreaterThanOrEqualToType((LiteralType) expr2, (PropertyNameType) expr1, null);
    }

    public PropertyIsLessThan less(Expression expr1, Expression expr2) {
        return new PropertyIsLessThanType((LiteralType) expr2, (PropertyNameType) expr1, null);
    }

    public PropertyIsLessThanOrEqualTo lessOrEqual(Expression expr1, Expression expr2) {
        return new PropertyIsLessThanOrEqualToType((LiteralType) expr2, (PropertyNameType) expr1, null);
    }

    public PropertyIsLike like(Expression expr, String pattern) {
        return like(expr, pattern, "*", "?", "\\");
    }

    public PropertyIsLike like(Expression expr, String pattern, String wildcard, String singleChar, String escape) {
        return new PropertyIsLikeType(expr, pattern, wildcard, singleChar, escape);
    }

    public PropertyIsNull isNull(Expression expr) {
        return new PropertyIsNullType((PropertyNameType)expr);
    }

    public BBOX bbox(String propertyName, double minx, double miny, double maxx, double maxy, String srs) {
        return new BBOXType(propertyName, minx, miny, maxx, maxy, srs);
    }

    public Beyond beyond(String propertyName, Geometry geometry, double distance, String units) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Contains contains(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Crosses crosses(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Disjoint disjoint(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DWithin dwithin(String propertyName, Geometry geometry, double distance, String units) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Equals equals(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Intersects intersects(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Overlaps overlaps(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Touches touches(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Within within(String propertyName, Geometry geometry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Add add(Expression expr1, Expression expr2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Divide divide(Expression expr1, Expression expr2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Multiply multiply(Expression expr1, Expression expr2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Subtract subtract(Expression expr1, Expression expr2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Function function(String name, Expression[] args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Function function(String name, Expression arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Function function(String name, Expression arg1, Expression arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Function function(String name, Expression arg1, Expression arg2, Expression arg3) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Literal literal(Object obj) {
        if (obj instanceof Date) {
            Date d = (Date) obj;
            obj = dateFormat.format(d);
        }
        return new LiteralType(obj);
    }

    public Literal literal(byte b) {
        return new LiteralType(b);
    }

    public Literal literal(short s) {
        return new LiteralType(s);
    }

    public Literal literal(int i) {
        return new LiteralType(i);
    }

    public Literal literal(long l) {
        return new LiteralType(l);
    }

    public Literal literal(float f) {
        return new LiteralType(f);
    }

    public Literal literal(double d) {
        return new LiteralType(d);
    }

    public Literal literal(char c) {
        return new LiteralType(c);
    }

    public Literal literal(boolean b) {
        return new LiteralType(b);
    }

    public SortBy sort(String propertyName, SortOrder order) {
        return new SortPropertyType(propertyName, order);
    }

    public Operator operator(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SpatialOperator spatialOperator(String name, GeometryOperand[] geometryOperands) {
        return new SpatialOperatorType(name, geometryOperands);
    }

    public FunctionName functionName(String name, int nargs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Functions functions(FunctionName[] functionNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SpatialOperators spatialOperators(SpatialOperator[] spatialOperators) {
       return new SpatialOperatorsType( spatialOperators );
    }

    public ComparisonOperators comparisonOperators(Operator[] comparisonOperators) {
        return new ComparisonOperatorsType(comparisonOperators);
    }

    public ArithmeticOperators arithmeticOperators(boolean simple, Functions functions) {
         return new ArithmeticOperatorsType(simple, functions);
    }

    public ScalarCapabilities scalarCapabilities(ComparisonOperators comparison, ArithmeticOperators arithmetic, boolean logical) {
        return new ScalarCapabilitiesType(comparison, arithmetic, logical);
    }

    public SpatialCapabilities spatialCapabilities(GeometryOperand[] geometryOperands, SpatialOperators spatial) {
        return new SpatialCapabilitiesType(geometryOperands, spatial);
    }

    public IdCapabilities idCapabilities(boolean eid, boolean fid) {
        return new IdCapabilitiesType(eid, fid);
    }

    public FilterCapabilities capabilities(String version, ScalarCapabilities scalar, SpatialCapabilities spatial, IdCapabilities id) {
        return new net.seagis.ogc.FilterCapabilities(scalar, spatial, id);
    }

    
}
