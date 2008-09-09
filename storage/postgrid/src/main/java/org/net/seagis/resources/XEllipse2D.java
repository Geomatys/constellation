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
package net.seagis.resources;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.geom.Ellipse2D;


/**
 * Une ellipse {@linkplain Serializable enregistrable}. Cette classe n'est que temporaire.
 * Elle sera suprimée quand (et si) Sun donne suite au RFE #4093004.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class XEllipse2D extends Ellipse2D.Double implements Serializable {
    /**
     * Construit une ellipse par défaut.
     */
    public XEllipse2D() {
        super();
    }

    /**
     * Construit une ellipse avec la dimension spécifiée.
     */
    public XEllipse2D(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Procède à la lecture d'une ellispe.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        x      = in.readDouble();
        y      = in.readDouble();
        width  = in.readDouble();
        height = in.readDouble();
    }

    /**
     * Procède à l'enregistrement d'une ellispe.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(width);
        out.writeDouble(height);
    }
}
