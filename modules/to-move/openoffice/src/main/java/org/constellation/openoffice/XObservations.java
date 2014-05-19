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
package org.constellation.openoffice;

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
