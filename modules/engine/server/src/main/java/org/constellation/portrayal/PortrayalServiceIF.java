/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.portrayal;

import java.awt.Dimension;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.geotoolkit.display.canvas.GraphicVisitor;
import org.geotoolkit.display.exception.PortrayalException;

/**
 * The interface for portrayal methods provided by Constellation.
 * <p>
 * Portrayal services generally involve generating imagery of sets of layers 
 * with a given style over a certain extent at a certain resolution. However,
 * there are also methods to pass a visitor to a scene 
 * </p>
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public interface PortrayalServiceIF {

    /**
     * Portray a set of Layers over a given geographic extent with a given
     * resolution yielding a {@code BufferedImage} of the scene.
     * @param sdef A structure which defines the scene.
     * @param vdef A structure which defines the view.
     * @param cdef A structure which defines the canvas.
     *
     * @return A rendered image of the scene, in the chosen view and for the
     *           given canvas.
     * @throws PortrayalException For errors during portrayal, TODO: common examples?
     */
    BufferedImage portray(final Portrayal.SceneDef sdef,
            final Portrayal.ViewDef vdef,
            final Portrayal.CanvasDef cdef)
            throws PortrayalException;

    /**
     * Apply the Visitor to all the
     * {@link org.opengis.display.primitive.Graphic} objects which lie within
     * the {@link java.awt.Shape} in the given scene.
     * <p>
     * The visitor could be an extension of the AbstractGraphicVisitor class in
     * this same package.
     * </p>
     *
     * TODO: why are the last two arguments not final?
     *
     * @see AbstractGraphicVisitor
     */
    void visit(final Portrayal.SceneDef sdef,
            final Portrayal.ViewDef vdef,
            final Portrayal.CanvasDef cdef,
            Shape selectedArea,
            GraphicVisitor visitor)
            throws PortrayalException;

    /**
     * Creates an image of the given {@link Exception}. This is useful for
     * several OGC web services which need to record that an exception has
     * occurred but only return an image in the message exchange protocol.
     *
     * TODO: document how the size of the text should be chosen.
     *
     * @param e      The exception to document in the generated image.
     * @param dim    The dimension in pixels of the generated image.
     * @return       An image of the exception message text. TODO: verify this.
     */
    BufferedImage writeInImage(Exception e, Dimension dim);
}
