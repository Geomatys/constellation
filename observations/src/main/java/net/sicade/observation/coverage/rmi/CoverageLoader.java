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
package net.sicade.observation.coverage.rmi;

import java.rmi.Remote;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;


/**
 * Un d�codeur pouvant lire une image en particulier, localement o� via les RMI. Lorsque l'image
 * est transmise sur le r�seau via les RMI, elle peut dans certains cas �tre d�grad�e pour une
 * transmission plus compacte.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface CoverageLoader extends Remote {
    /**
     * Proc�de au chargement de l'image. Cette m�thode peut �tre ex�cut�e sur une machine distante,
     * qui ne renvoiera que le r�sultat final (une image potentiellement d�coup�e, d�cim�e et
     * transform�e). Cette m�thode retourne toujours la version geophysique de l'image
     * (<code>{@linkplain GridCoverage2D#geophysics geophysics}(true)</code>).
     *
     * @return Image lue.
     * @throws java.io.IOException si le fichier n'a pas �t� trouv� ou si une autre erreur
     *         d'entr�s/sorties est survenue.
     * @throws javax.imageio.IIOException s'il n'y a pas de d�codeur appropri� pour l'image,
     *         ou si l'image n'est pas valide.
     * @throws java.rmi.RemoteException si un probl�me est survenu lors de la communication avec le
     *         serveur.
     */
    GridCoverage2D getCoverage() throws IOException;
}
