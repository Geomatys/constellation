/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.portrayal;

import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.VisitDef;

import java.awt.*;
import java.awt.image.BufferedImage;

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
    BufferedImage portray(final SceneDef sdef,
            final ViewDef vdef,
            final CanvasDef cdef)
            throws PortrayalException;

    /**
     * Portray a set of Layers over a given geographic extent with a given
     * resolution in the provided output.
     * @param sdef A structure which defines the scene.
     * @param vdef A structure which defines the view.
     * @param cdef A structure which defines the canvas.
     * @param odef A structure which defines the output.
     *
     * @throws PortrayalException For errors during portrayal, TODO: common examples?
     */
    void portray(final SceneDef sdef,
            final ViewDef vdef,
            final CanvasDef cdef,
            final OutputDef odef)
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
    void visit(final SceneDef sdef,
            final ViewDef vdef,
            final CanvasDef cdef,
            final VisitDef visitDef)
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


    /**
     *  Creates a blank image fill with given color. This is useful for
     *  several OGC web services which need to return blank image/tile
     *  when an exception occurred
     *
     * @param color The color of output image.
     * @param dim   The dimension in pixels of the generated image.
     * @return      An image with all pixel at the same color.
     */
    BufferedImage writeBlankImage(Color color, Dimension dim);
}
