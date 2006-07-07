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
package net.sicade.openoffice;

// OpenOffice dependencies
import com.sun.star.uno.XInterface;
import com.sun.star.beans.XPropertySet;


/**
 * Pont en Java de l'interface IDL pour le service {@code XCoverage3D} d�clar� dans
 * {@code XObservations.idl}. Cette interface existe principalement pour satisfaire les
 * environnements IDE. Le fichier JAR final devrait plut�t inclure le fichier {@code .class}
 * g�n�r� par l'outil {@code javamaker} du SDK d'OpenOffice.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface XObservations extends XInterface {
    /**
     * Retourne la valeur d'un descripteur du paysage oc�anique � la position spatio-temporelle
     * specifi�e.
     *
     * @param xOptions Propri�t�s fournies par OpenOffice.
     * @param descriptor Nom du descripteur du paysage oc�anique.
     * @param t La date � laquelle �valuer le descripteur.
     * @param x Longitude � laquelle �valuer le descripteur.
     * @param y Latitude � laquelle �valuer le descripteur.
     * @return La valeur du descripteur du paysage oc�anique (premi�re bande seulement).
     */
    Object getDescriptorValue(XPropertySet xOptions, String descriptor, double t, double x, double y);

    /**
     * Retourne la coordonn�e au centre du voxel le plus proche de la coordonn�es sp�cifi�e.
     *
     * @param xOptions Propri�t�s fournies par OpenOffice.
     * @param descriptor Nom du descripteur du paysage oc�anique.
     * @param t La date.
     * @param x Longitude, en degr�s.
     * @param y Latitude, en degr�s.
     * @return Coordonn�es au centre du voxel le plus proche.
     */
    double[][] getVoxelCenter(XPropertySet xOptions, String descriptor, double t, double x, double y);
}
