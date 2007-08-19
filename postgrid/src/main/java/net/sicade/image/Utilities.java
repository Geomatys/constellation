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
 *    Lesser General Public License for more details.
 */
package net.sicade.image;

import java.awt.image.RenderedImage;
import javax.swing.JFrame;


/**
 * A set of utilities related to images.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Utilities {
    /**
     * Do not allows instantiation of this class.
     */
    private Utilities() {
    }

    /**
     * Display the specified image. This method is used mostly for debugging purpose.
     */
    @SuppressWarnings("deprecation")
    public static void show(final RenderedImage image, final String title) {
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new javax.media.jai.widget.ScrollingImagePanel(image, 400, 400));
        frame.pack();
        frame.setVisible(true);
    }
}
