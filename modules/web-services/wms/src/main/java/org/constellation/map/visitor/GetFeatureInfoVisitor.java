/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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
package org.constellation.map.visitor;

import org.geotoolkit.display.canvas.GraphicVisitor;

/**
 * Extend graphic visitor with and additional getResult method to grab
 * the getFeatureInfo response object.
 *
 * @author Johann Sorel (Geomatys)
 */
public interface GetFeatureInfoVisitor extends GraphicVisitor{

    /**
     * After visiting, return the getfeatureInfo result.
     *
     * @return Object
     */
    public Object getResult();

    /**
     * @return result mime type.
     */
    public String getMimeType();

}
