/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Institut de Recherche pour le Développement
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

package net.seagis.cat.csw;

import java.util.List;
import net.seagis.dublincore.AbstractSimpleLiteral;

/**
 *
 * @author Mehdi Sidhoum.
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
    
    public List<? extends Object> getSubject();
    
    public AbstractSimpleLiteral getFormat();
    
    public AbstractSimpleLiteral getModified();
    
    public AbstractSimpleLiteral getAbstract();
    
    public AbstractSimpleLiteral getCreator();
    
    public AbstractSimpleLiteral getDistributor();
    
    public AbstractSimpleLiteral getLanguage();
    
    public AbstractSimpleLiteral getRelation();
    
    public AbstractSimpleLiteral getSource();
    
    public AbstractSimpleLiteral getCoverage();
    
    public AbstractSimpleLiteral getDate();
    
    public AbstractSimpleLiteral getRights();
    
    public AbstractSimpleLiteral getSpatial();
    
    public AbstractSimpleLiteral getReferences();
    
    public AbstractSimpleLiteral getPublisher();
    
    public AbstractSimpleLiteral getContributor();
    
    public AbstractSimpleLiteral getDescription();

}
