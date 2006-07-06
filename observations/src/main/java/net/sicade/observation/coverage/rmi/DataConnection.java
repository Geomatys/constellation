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

// J2SE dependencies
import java.util.Set;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.sql.SQLException;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.CoverageReference;


/**
 * Connexion vers les donn�es environnementales. Cette interface permet de voir les donn�es comme
 * une matrice tri-dimensionnelle, et d'�valuer ses valeurs sans n�cessairement transf�rer des
 * images via le r�seau. En effet, cette interface peut �tre utilis�e dans le contexte des RMI
 * (<cite>Remote Method Invocation</cite>), auquel cas le travail (chargement des images, calculs,
 * <cite>etc.</cite>) est effectu� sur le serveur et seul le r�sultat est transf�r� sur le r�seau.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface DataConnection extends Remote {
    /**
     * Retourne le syst�me de r�f�rence des coordonn�es selon lequel seront exprim�es
     * l'{@linkplain #getEnvelope enveloppe}, les {@linkplain #evaluate coordonn�es
     * des valeurs}, <cite>etc.</cite>
     *
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem() throws RemoteException;

    /**
     * Retourne l'enveloppe spatio-temporelle des donn�es.
     *
     * @throws CatalogException si la base de donn�es n'a pas pu �tre interrog�e.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     */
    Envelope getEnvelope() throws CatalogException, RemoteException;

    /**
     * Retourne la partie g�ographique de l'{@linkplain #getEnvelope enveloppe} des donn�es.
     *
     * @throws CatalogException si la base de donn�es n'a pas pu �tre interrog�e.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException, RemoteException;

    /**
     * Retourne la partie temporelle de l'{@linkplain #getEnvelope enveloppe} des donn�es.
     *
     * @throws CatalogException si la base de donn�es n'a pas pu �tre interrog�e.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     */
    DateRange getTimeRange() throws CatalogException, RemoteException;

    /**
     * Retourne la valeur d'une bande � une position interpol�e dans l'ensemble des images de cette
     * table. L'ensemble des donn�es est trait� comme une matrice tri-dimensionnelle. La coordonn�e
     * doit �tre exprim�e selon le {@linkplain #getCoordinateReferenceSystem syst�me de r�f�rence
     * des coordonn�es de la table}. Ces coordonn�es sont habituellement (mais pas obligatoirement):
     * <p>
     * <ul>
     *   <li>La longitude, en degr�s d�cimaux par rapport au m�ridien de Greenwich</li>
     *   <li>La latitude, en degr�s d�cimaux</li>
     *   <li>Le temps, en nombre de jours depuis le 1er janvier 1950 00:00 UTC.</li>
     * </ul>
     * <p>
     * La signature de cette m�thode (� base de types primitifs seulement) vise � r�duire les temps
     * de transfert sur le r�seau dans le contexte des appels RMI.
     *
     * @throws CatalogException si un enregistrement de la base de donn�es est invalide.
     * @throws SQLException     si la base de donn�es n'a pas pu �tre interrog�e pour une autre raison.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     * @throws IOException      si la lecture d'une image a �chou�.
     */
    double evaluate(double x, double y, double t, short band) throws CatalogException, SQLException, IOException;

    /**
     * Retourne les coordonn�es au centre du voxel le plus proche des coordonn�es sp�cifi�es.
     * Cette m�thode recherche l'image la plus proche de la date sp�cifi�e, puis recherche le
     * pixel qui contient la coordonn�e g�ographique sp�cifi�e. La date de milieu de l'image,
     * ainsi que les coordonn�es g�ographiques au centre du pixel, sont retourn�es. Appeller
     * la m�thode {@link #evaluate evaluate} avec les coordonn�es retourn�es devrait permettre
     * d'obtenir une valeur non-interpoll�e.
     * <p>
     * La signature de cette m�thode (� base de types primitifs seulement) vise � r�duire les temps
     * de transfert sur le r�seau dans le contexte des appels RMI. En particulier on retourne un
     * tableau de {@code double} plut�t qu'un objet {@link DirectPosition} afin d'�viter le transfert
     * d'un objet {@link CoordinateReferenceSystem} sur le r�seau.
     *
     * @throws CatalogException si un enregistrement de la base de donn�es est invalide.
     * @throws SQLException     si la base de donn�es n'a pas pu �tre interrog�e pour une autre raison.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     * @throws IOException      si la lecture d'une image a �chou�.
     */
    double[] snap(double x, double y, double t) throws CatalogException, SQLException, IOException;

    /**
     * Retourne les couvertures utilis�es par les m�thodes {@code evaluate} pour le temps <var>t</var>
     * sp�cifi�. L'ensemble retourn� comprendra typiquement 0, 1 ou 2 �l�ments.
     *
     * @throws CatalogException si un enregistrement de la base de donn�es est invalide.
     * @throws SQLException     si la base de donn�es n'a pas pu �tre interrog�e pour une autre raison.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     * @throws IOException      si la lecture d'une image a �chou�.
     */
    List<Coverage> coveragesAt(double t) throws CatalogException, SQLException, IOException;

    /**
     * Retourne la liste des images disponibles.
     *
     * @return Liste d'images qui interceptent la plage de temps et la r�gion g�ographique d'int�r�t.
     * @throws CatalogException si un enregistrement de la base de donn�es est invalide.
     * @throws SQLException     si la base de donn�es n'a pas pu �tre interrog�e pour une autre raison.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     */
    Set<CoverageReference> getEntries() throws CatalogException, SQLException, RemoteException;

    /**
     * Retourne une nouvelle connexion vers les donn�es pour l'op�ration sp�cifi�es.
     * L'envelope spatio-temporelle restera la m�me.
     *
     * @param  operation  L'op�ration � appliquer pour conserver la m�me.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
     */
    DataConnection newInstance(Operation operation) throws RemoteException;
}
