/*
 *    Constellation - An open source and standard compliant SDI
 *    http://constellation.codehaus.org
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
package net.seagis.openoffice;

// OpenOffice dependencies
import com.sun.star.uno.XInterface;
import com.sun.star.beans.XPropertySet;


/**
 * Pont en Java de l'interface IDL pour le service {@code XCoverage3D} déclaré dans
 * {@code XObservations.idl}. Cette interface existe principalement pour satisfaire les
 * environnements IDE. Le fichier JAR final devrait plutôt inclure le fichier {@code .class}
 * généré par l'outil {@code javamaker} du SDK d'OpenOffice.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface XObservations extends XInterface {
    /**
     * Retourne la valeur d'un descripteur du paysage océanique à la position spatio-temporelle
     * specifiée.
     *
     * @param xOptions Propriétés fournies par OpenOffice.
     * @param descriptor Nom du descripteur du paysage océanique.
     * @param t La date à laquelle évaluer le descripteur.
     * @param x Longitude à laquelle évaluer le descripteur.
     * @param y Latitude à laquelle évaluer le descripteur.
     * @return La valeur du descripteur du paysage océanique (première bande seulement).
     */
    Object getDescriptorValue(XPropertySet xOptions, String descriptor, double t, double x, double y);

    /**
     * Retourne la coordonnée au centre du voxel le plus proche de la coordonnées spécifiée.
     *
     * @param xOptions Propriétés fournies par OpenOffice.
     * @param descriptor Nom du descripteur du paysage océanique.
     * @param t La date.
     * @param x Longitude, en degrés.
     * @param y Latitude, en degrés.
     * @return Coordonnées au centre du voxel le plus proche.
     */
    double[][] getVoxelCenter(XPropertySet xOptions, String descriptor, double t, double x, double y);
}
