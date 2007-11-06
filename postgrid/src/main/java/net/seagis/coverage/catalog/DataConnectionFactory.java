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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.seagis.coverage.catalog;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import net.seagis.catalog.CatalogException;


/**
 * Fournit des instances de {@link DataConnection} à la demande.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
interface DataConnectionFactory extends Remote {
    /**
     * Le nom sous lequel sera enregistré ce service dans {@link java.rmi.Naming}.
     */
    String REGISTRY_NAME = "DataConnectionFactory";

    /**
     * Construit une nouvelle connexion vers les données de la couche spécifiée.
     *
     * @throws CatalogException si la table n'a pas pu être construite pour la couche spécifiée.
     * @throws SQLException si la connexion à la base de données a échoué.
     * @throws RemoteException si la connexion n'a pas pu être établie.
     */
    DataConnection connectLayer(final String layer) throws CatalogException, SQLException, RemoteException;

    /**
     * Retourne la couverture de données pour le descripteur spécifié. Appeller cette méthode est
     * équivalent à exécuter le code suivant:
     *
     * <blockquote><pre>
     * return database.getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
     * </pre></blockquote>
     *
     * Toutefois, faire exécuter ce code sur le serveur RMI distant permet d'éviter que le client
     * n'ouvre une connexion à la base de données.
     *
     * @throws CatalogException si la table n'a pas pu être construite pour la couche spécifiée.
     * @throws SQLException si la connexion à la base de données a échoué.
     * @throws RemoteException si la connexion n'a pas pu être établie.
     */
    GridCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException, RemoteException;
}
