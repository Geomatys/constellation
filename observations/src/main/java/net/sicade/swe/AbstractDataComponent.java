/*
 * Sicade - Systémes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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

package net.sicade.swe;

import org.opengis.annotation.UML;
import static org.opengis.annotation.Specification.*;
import static org.opengis.annotation.Obligation.*;

/**
 * Base type for all data components. 
 *
 * @author legal
 */
@UML(identifier="AbstractDataComponent", specification=UNSPECIFIED)
public interface AbstractDataComponent {
    
    /**
     * Points to semantics information defining the precise nature of the component
     */
    @UML(identifier="definition", obligation=OPTIONAL, specification=UNSPECIFIED )
    String getDefinition();
    
    /**
     * Specifies if the value of a component stays fixed in time or is variable. Default is variable.
     */
    @UML(identifier="fixed", obligation=OPTIONAL, specification=UNSPECIFIED )
    boolean isFixed();
    
}
