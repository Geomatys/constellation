/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
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
package net.sicade.observation;

import java.util.Date;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import net.sicade.util.DateRange;


/**
 * Un �l�ment � une certaine position spatio-temporelle. Cette position est g�n�ralement ponctuelle,
 * mais il peut s'agir aussi d'une trajectoire. Par exemple il peut s'agir de la position d'une
 * p�che � la senne, ou la forme geom�trique repr�sentant la disposition d'une ligne de palangre.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface LocatedElement extends Element {
    /**
     * Retourne une cha�ne de caract�res d�crivant la position de cet �l�ment.
     * Cette cha�ne de caract�res peut �tre utilis�e comme un identifiant plus
     * d�taill� que le {@linkplain #getName nom} � des fins d'interfaces utilisateurs.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    String getLocation() throws CatalogException;

    /**
     * Retourne une coordonn�e repr�sentative de cet �l�ment, en degr�s de longitude et de latitude.
     * Cette m�thode peut retourner {@code null} si aucune coordonn�es repr�sentative n'est trouv�e.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    Point2D getCoordinate() throws CatalogException;

    /**
     * Retourne une date repr�sentative de cet �l�ment. Dans le cas des observations qui
     * s'�tendent sur une certaine p�riode de temps, �a pourrait �tre par exemple la date
     * du milieu. Cette m�thode peut retourner {@code null} si aucune date n'est associ�e
     * � cet �l�ment.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    Date getTime() throws CatalogException;

    /**
     * Retourne la plage de temps de cet �l�ment. Les composantes de la plage retourn�e seront du
     * type {@link Date}. Cette m�thode peut retourner {@code null} si aucune plage de temps n'est
     * associ� � cet �l�ment.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    DateRange getTimeRange() throws CatalogException;

    /**
     * Retourne la forme g�om�trique reliant toutes les positions visit�es par cet �l�ment,
     * dans leur ordre chronologique.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    Shape getPath() throws CatalogException;

    /**
     * V�rifie si cet �l�ment intercepte le rectangle sp�cifi�.
     * La r�ponse retourn�e par cette m�thode n'est qu'� titre indicatif.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    boolean intersects(final Rectangle2D rect) throws CatalogException;
}
