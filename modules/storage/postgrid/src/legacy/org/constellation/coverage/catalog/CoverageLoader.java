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
package org.constellation.coverage.catalog;

import java.rmi.Remote;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;


/**
 * Un décodeur pouvant lire une image en particulier, localement où via les RMI. Lorsque l'image
 * est transmise sur le réseau via les RMI, elle peut dans certains cas être dégradée pour une
 * transmission plus compacte.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
interface CoverageLoader extends Remote {
    /**
     * Procède au chargement de l'image. Cette méthode peut être exécutée sur une machine distante,
     * qui ne renvoiera que le résultat final (une image potentiellement découpée, décimée et
     * transformée). Cette méthode retourne toujours la version geophysique de l'image
     * (<code>{@linkplain GridCoverage2D#geophysics geophysics}(true)</code>).
     *
     * @return Image lue.
     * @throws java.io.IOException si le fichier n'a pas été trouvé ou si une autre erreur
     *         d'entrés/sorties est survenue.
     * @throws javax.imageio.IIOException s'il n'y a pas de décodeur approprié pour l'image,
     *         ou si l'image n'est pas valide.
     * @throws java.rmi.RemoteException si un problème est survenu lors de la communication avec le
     *         serveur.
     */
    GridCoverage2D getCoverage() throws IOException;
}
