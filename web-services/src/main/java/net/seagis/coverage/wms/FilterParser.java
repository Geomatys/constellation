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

package net.seagis.coverage.wms;

import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import net.seagis.cat.csw.QueryConstraintType;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.ogc.AbstractIdType;
import net.seagis.ogc.BBOXType;
import net.seagis.ogc.BinaryComparisonOpType;
import net.seagis.ogc.BinaryLogicOpType;
import net.seagis.ogc.BinarySpatialOpType;
import net.seagis.ogc.ComparisonOpsType;
import net.seagis.ogc.DistanceBufferType;
import net.seagis.ogc.FilterType;
import net.seagis.ogc.LiteralType;
import net.seagis.ogc.LogicOpsType;
import net.seagis.ogc.PropertyIsBetweenType;
import net.seagis.ogc.PropertyIsLikeType;
import net.seagis.ogc.PropertyIsNullType;
import net.seagis.ogc.PropertyNameType;
import net.seagis.ogc.SpatialOpsType;
import net.seagis.ogc.UnaryLogicOpType;
import net.seagis.ows.v100.OWSWebServiceException;
import static net.seagis.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class FilterParser {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("net.seagis.covrage.wms");
    
     /**
     * The version of the service
     */
    private ServiceVersion version;
    
    /**
     * Build a new FilterParser with the specified version.
     */
    public FilterParser(ServiceVersion version) {
        this.version = version;
    }
    
     /**
     * Build a lucene request from the specified constraint
     * 
     * @param constraint a constraint expressed in CQL or FilterType
     */
    public String getLuceneQuery(QueryConstraintType constraint) throws WebServiceException {
        
        StringBuilder query = new StringBuilder("");
        if (constraint.getCqlText() != null && constraint.getFilter() != null) {
            throw new OWSWebServiceException("The query constraint must be in Filter or CQL but not both.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
        }
        if (constraint.getCqlText() != null) {
            throw new OWSWebServiceException("CQL are not yet supported.",
                                             INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            
        } else if (constraint.getFilter() != null) {
            FilterType filter = constraint.getFilter();
            
            // we treat logical Operators like AND, OR, ...
            if (filter.getLogicOps() != null) {
                query.append(treatLogicalOperator(filter.getLogicOps()));
            
            // we treat directly comparison operator: PropertyIsLike, IsNull, ISBetween,...    
            } else if (filter.getComparisonOps() != null) {
                query.append(treatComparisonOperator(filter.getComparisonOps()));
                
            // we treat spatial constraint    
            } else if (filter.getSpatialOps() != null) {
                query.append(treatSpatialOperator(filter.getSpatialOps()));
                
            } else if (filter.getId() != null) {
                query.append(treatIDOperator(filter.getId()));
            }  
        }
        return query.toString();
    }
    
    /**
     * Build a piece of lucene query with the specified Logical filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private String treatLogicalOperator(JAXBElement<? extends LogicOpsType> JBlogicOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        LogicOpsType logicOps = JBlogicOps.getValue();
        String operator = JBlogicOps.getName().getLocalPart();
        
        if (logicOps instanceof BinaryLogicOpType) {
            BinaryLogicOpType binary = (BinaryLogicOpType) logicOps;
            response.append('(');
            for (JAXBElement<?> jb: binary.getComparisonOpsOrSpatialOpsOrLogicOps()) {
                
                if (jb.getValue() instanceof LogicOpsType) {
                    
                    response.append(treatLogicalOperator((JAXBElement<? extends LogicOpsType>)jb));
                    
                } else if (jb.getValue() instanceof ComparisonOpsType) {
                    
                    response.append(treatComparisonOperator((JAXBElement<? extends ComparisonOpsType>)jb));
                
                } else if (jb.getValue() instanceof SpatialOpsType) {
                    
                   response.append(treatSpatialOperator((JAXBElement<? extends SpatialOpsType>)jb)); 
                    
                } else {
                    
                    throw new IllegalArgumentException("unknow BinaryLogicalOp:" + jb.getValue().getClass().getSimpleName()); 
                }
                
                response.append(" ").append(operator.toUpperCase()).append(" ");
            }
          
          // we remove the last Operator and add a )  
          response.delete(response.length()- (operator.length() + 2), response.length());
          response.append(')');
                
        } else if (logicOps instanceof UnaryLogicOpType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        return response.toString();
    }
    
    /**
     * Build a piece of lucene query with the specified Comparison filter.
     * 
     * @param JBlogicOps
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private String treatComparisonOperator(JAXBElement<? extends ComparisonOpsType> JBComparisonOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        ComparisonOpsType comparisonOps = JBComparisonOps.getValue();
        
        if (comparisonOps instanceof PropertyIsLikeType ) {
            PropertyIsLikeType pil = (PropertyIsLikeType) comparisonOps;
            
            //we get the field
            if (pil.getPropertyName() != null) {
                response.append(removePrefix(pil.getPropertyName().getContent())).append(':');
            } else {
                throw new OWSWebServiceException("An operator propertyIsLike must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //we get the value of the field
            if (pil.getLiteral() != null && pil.getLiteral().getStringValue() != null) {
                
                //we format the value by replacing the specified special char by the lucene special char
                String brutValue = pil.getLiteral().getStringValue();
                brutValue = brutValue.replace(pil.getWildCard(),    "*");
                brutValue = brutValue.replace(pil.getSingleChar(),  "?");
                brutValue = brutValue.replace(pil.getEscapeChar(),  "\\");
                // lucene does not accept '*' on first character
                while (brutValue.charAt(0) == '*') {
                    brutValue = brutValue.substring(1);
                }
                response.append(brutValue);
                
            } else {
                throw new OWSWebServiceException("An operator propertyIsLike must specified the literal value.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
        } else if (comparisonOps instanceof PropertyIsNullType) {
             PropertyIsNullType pin = (PropertyIsNullType) comparisonOps;

            //we get the field
            if (pin.getPropertyName() != null) {
                response.append(removePrefix(pin.getPropertyName().getContent())).append(':').append("null");
            } else {
                throw new OWSWebServiceException("An operator propertyIsNull must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
        } else if (comparisonOps instanceof PropertyIsBetweenType) {
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
        
        } else if (comparisonOps instanceof BinaryComparisonOpType) {
            
            BinaryComparisonOpType bc = (BinaryComparisonOpType) comparisonOps;
            String propertyName       = null;
            LiteralType literal       = null;
            String operator           = JBComparisonOps.getName().getLocalPart(); 
            for (Object obj: bc.getExpressionOrLiteralOrPropertyName()) {
                if (obj instanceof LiteralType) {
                    literal      = (LiteralType) obj;
                } else if (obj instanceof String) {
                    propertyName = (String) obj;
                } else {
                    throw new IllegalArgumentException("BinaryComparisonOpType parameter not known: " + obj.getClass().getSimpleName());
                }
            }
            if (propertyName == null || literal == null) {
                throw new OWSWebServiceException("A binary comparison operator must be contitued of a literal and a property name.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            } else {
                if (operator.equals("PropertyIsEqualTo")) {                
                    response.append(removePrefix(propertyName)).append(":\"").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsNotEqualTo")) {
                    
                   response.append("metafile:doc NOT ");
                   response.append(removePrefix(propertyName)).append(":").append(literal.getStringValue()).append('"');
                
                } else if (operator.equals("PropertyIsGreaterThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue().replace("-", "");
                        response.append(removePrefix(propertyName)).append(":[").append(dateValue).append(' ').append(" 30000101]");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                
                } else if (operator.equals("PropertyIsGreaterThan")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue().replace("-", "");
                        response.append(removePrefix(propertyName)).append(":{").append(dateValue).append(' ').append(" 30000101}");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                
                } else if (operator.equals("PropertyIsLessThan") ) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue().replace("-", "");
                        response.append(removePrefix(propertyName)).append(":{00000101").append(' ').append(dateValue).append("}");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                    
                } else if (operator.equals("PropertyIsLessThanOrEqualTo")) {
                    if (propertyName.contains("Date") || propertyName.contains("Modified")) {
                        String dateValue = literal.getStringValue().replace("-", "");
                        response.append(removePrefix(propertyName)).append(":[00000101").append(' ').append(dateValue).append("]");
                    } else {
                        throw new IllegalArgumentException("not supported yet no date range");
                    }
                } else {
                    throw new OWSWebServiceException("Unkwnow operator: " + operator,
                                                     INVALID_PARAMETER_VALUE, "QueryConstraint", version);
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
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private String treatSpatialOperator(JAXBElement<? extends SpatialOpsType> JBSpatialOps) throws WebServiceException {
        StringBuilder response = new StringBuilder();
        
        SpatialOpsType spatialOps = JBSpatialOps.getValue();
        
        if (spatialOps instanceof BBOXType) {
            BBOXType bbox       = (BBOXType) spatialOps;
            String propertyName = bbox.getPropertyName();
            if (propertyName == null) {
                throw new OWSWebServiceException("An operator BBOX must specified the propertyName.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            if (bbox.getEnvelope() == null && bbox.getEnvelopeWithTimePeriod() == null) {
                throw new OWSWebServiceException("An operator BBOX must specified an envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
            
        } else if (spatialOps instanceof DistanceBufferType) {
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
            
        } else if (spatialOps instanceof BinarySpatialOpType) {
            
            BinarySpatialOpType binSpatial = (BinarySpatialOpType) spatialOps;
            List<JAXBElement<?>> objects   = binSpatial.getAbstractGeometryOrAbstractGeometricPrimitiveOrPoint();
            
            String propertyName    = null;
            String operator        = JBSpatialOps.getName().getLocalPart();
            EnvelopeEntry envelope = null;
            
            for (JAXBElement<?> jb: objects) {
                if (jb.getValue() instanceof PropertyNameType) {
                    PropertyNameType p = (PropertyNameType) jb.getValue();
                    propertyName = p.getContent();
                } else if (jb.getValue() instanceof EnvelopeEntry) {
                    envelope     = (EnvelopeEntry) jb.getValue();
                } else {
                    throw new IllegalArgumentException("unknow BinarySpatialOp type:" + jb.getValue().getClass().getSimpleName());
                }
            }
            
            if (propertyName == null && envelope == null) {
                throw new OWSWebServiceException("An Binarary spatial operator must specified a propertyName and an envelope.",
                                                 INVALID_PARAMETER_VALUE, "QueryConstraint", version);
            }
            
            //TODO
            throw new UnsupportedOperationException("Not supported yet.");
            
        }
        
        return response.toString();
    }
    
    private String treatIDOperator(List<JAXBElement<? extends AbstractIdType>> JBIdsOps) {
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
