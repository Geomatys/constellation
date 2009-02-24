/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.map.ws.rs;

import org.constellation.query.wms.GetFeatureInfo;
import org.geotools.display.canvas.AbstractGraphicVisitor;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class TextGraphicVisitor extends AbstractGraphicVisitor{

    protected final GetFeatureInfo gfi;

    protected TextGraphicVisitor(GetFeatureInfo gfi){
        if(gfi == null){
            throw new NullPointerException("GetFeatureInfo Object can not be null");
        }
        this.gfi = gfi;
    }

    public abstract String getResult();

}
