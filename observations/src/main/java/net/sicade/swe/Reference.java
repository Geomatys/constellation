/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General License for more details.
 */

package net.sicade.swe;

import java.util.List;

/**
 * Une reference
 *
 * @author legal
 */
interface Reference {
    
    List<String> getNilReason();

    String getRemoteSchema();

    String getActuate();

    String getArcrole();

    String getHref();

    String getRole();

    String getShow();

    String getTitle();

    String getType();

    java.lang.Boolean getOwns();
    
}
