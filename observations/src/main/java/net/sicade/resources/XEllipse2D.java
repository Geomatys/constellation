/*
 * Sicade - Syst�mes int�gr�s de connaissances
 *          pour l'aide � la d�cision en environnement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.resources;

// J2SE dependencies
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.geom.Ellipse2D;


/**
 * Une ellipse {@linkplain Serializable enregistrable}. Cette classe n'est que temporaire.
 * Elle sera suprim�e quand (et si) Sun donne suite au RFE #4093004.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class XEllipse2D extends Ellipse2D.Double implements Serializable {
    /**
     * Construit une ellipse par d�faut.
     */
    public XEllipse2D() {
        super();
    }

    /**
     * Construit une ellipse avec la dimension sp�cifi�e.
     */
    public XEllipse2D(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Proc�de � la lecture d'une ellispe.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        x      = in.readDouble();
        y      = in.readDouble();
        width  = in.readDouble();
        height = in.readDouble();
    }

    /**
     * Proc�de � l'enregistrement d'une ellispe.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(width);
        out.writeDouble(height);
    }
}
