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
import org.constellation.dublincore.AbstractSimpleLiteral;

/**
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @author Guilhem Legal (Geomatys).
 */
public interface DCMIRecord extends AbstractRecord {
    
    /**
     * Gets the value of the dcElement property.
     * (unModifiable)
     */
    public List<? extends Object> getDCElement();
    
    public AbstractSimpleLiteral getIdentifier();
    
    public AbstractSimpleLiteral getTitle();
    
    public AbstractSimpleLiteral getType();
    
    public List<? extends AbstractSimpleLiteral> getSubject();
    
    public List<? extends AbstractSimpleLiteral> getFormat();
    
    public AbstractSimpleLiteral getModified();
    
    public List< ? extends AbstractSimpleLiteral> getAbstract();
    
    public List<? extends AbstractSimpleLiteral> getCreator();
    
    public AbstractSimpleLiteral getDistributor();
    
    public AbstractSimpleLiteral getLanguage();
    
    public List<? extends AbstractSimpleLiteral> getRelation();
    
    public List<? extends AbstractSimpleLiteral> getSource();
    
    public List<? extends AbstractSimpleLiteral> getCoverage();
    
    public AbstractSimpleLiteral getDate();
    
    public List<? extends AbstractSimpleLiteral> getRights();
    
    public AbstractSimpleLiteral getSpatial();
    
    public AbstractSimpleLiteral getReferences();
    
    public List<? extends AbstractSimpleLiteral> getPublisher();
    
    public List<? extends AbstractSimpleLiteral> getContributor();
    
    public AbstractSimpleLiteral getDescription();

}
