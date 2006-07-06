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
import java.rmi.RemoteException;
import java.sql.SQLException;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.DynamicCoverage;


/**
 * Fournit des instances de {@link DataConnection} � la demande.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface DataConnectionFactory extends Remote {
    /**
     * Le nom sous lequel sera enregistr� ce service dans {@link java.rmi.Naming}.
     */
    String REGISTRY_NAME = "DataConnectionFactory";

    /**
     * Construit une nouvelle connexion vers les donn�es de la s�rie sp�cifi�e.
     *
     * @throws CatalogException si la table n'a pas pu �tre construite pour la s�rie sp�cifi�e.
     * @throws SQLException si la connexion � la base de donn�es a �chou�.
     * @throws RemoteException si la connexion n'a pas pu �tre �tablie.
     */
    DataConnection connectSeries(final String series) throws CatalogException, SQLException, RemoteException;

    /**
     * Retourne la couverture de donn�es pour le descripteur sp�cifi�. Appeller cette m�thode est
     * �quivalent � ex�cuter le code suivant:
     *
     * <blockquote><pre>
     * return database.getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
     * </pre></blockquote>
     *
     * Toutefois, faire ex�cuter ce code sur le serveur RMI distant permet d'�viter que le client
     * n'ouvre une connexion � la base de donn�es.
     *
     * @throws CatalogException si la table n'a pas pu �tre construite pour la s�rie sp�cifi�e.
     * @throws SQLException si la connexion � la base de donn�es a �chou�.
     * @throws RemoteException si la connexion n'a pas pu �tre �tablie.
     */
    DynamicCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException, RemoteException;
}
