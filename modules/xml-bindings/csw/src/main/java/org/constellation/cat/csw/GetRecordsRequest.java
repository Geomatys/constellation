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
package org.constellation.cat.csw;

import java.util.List;
import javax.xml.namespace.QName;
import org.constellation.ogc.FilterType;


/**
 * An interface containing the common methods to the different version of the operation GetRecords.
 * 
 */
public interface GetRecordsRequest extends AbstractCswRequest {

    
    /**
     * Gets the value of the requestId property.
     */
    public String getRequestId();

    /**
     * Sets the value of the requestId property.
     * 
     */
    public void setRequestId(String value);

    /**
     * Gets the value of the outputSchema property.
     */
    public String getOutputSchema();

    /**
     * Sets the value of the outputSchema property.
     * 
     */
    public void setOutputSchema(String value);

    /**
     * Gets the value of the startPosition property.
     * 
     */
    public Integer getStartPosition();
    
    /**
     * Sets the value of the startPosition property.
     * 
     */
    public void setStartPosition(Integer value);

    /**
     * Gets the value of the maxRecords property.
     * 
     */
    public Integer getMaxRecords();

    /**
     * Sets the value of the maxRecords property.
     * 
     */
    public void setMaxRecords(Integer value);
    
    /**
     * Get the service version number.
     */
    public String getVersion();
    
    /**
     * Set the service version number.
     */
    public void setVersion(String version);
    
    /**
     * Set the typeNames field of the Query part.
     */
    public void setTypeNames(List<QName> typenames);
    
    /**
     * Remove all the Query constraint.
     */
    public void removeConstraint();
    
    /**
     * replace the Query constraint by a new COnstraint with the specified CQL text.
     */
    public void setCQLConstraint(String CQLQuery);
    
    /**
     * This method set a query constraint by a filter.
     * @param filter FilterType
     */
    public void setFilterConstraint(FilterType filter);

}
