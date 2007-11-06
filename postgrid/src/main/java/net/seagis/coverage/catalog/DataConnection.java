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
 */
package net.seagis.coverage.catalog;

import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.SortedSet;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.sql.SQLException;

import org.opengis.coverage.Coverage;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.util.NumberRange;

import net.seagis.util.DateRange;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.model.Operation;


/**
 * Connexion vers les données environnementales. Cette interface permet de voir les données comme
 * une matrice tri-dimensionnelle, et d'évaluer ses valeurs sans nécessairement transférer des
 * images via le réseau. En effet, cette interface peut être utilisée dans le contexte des RMI
 * (<cite>Remote Method Invocation</cite>), auquel cas le travail (chargement des images, calculs,
 * <cite>etc.</cite>) est effectué sur le serveur et seul le résultat est transféré sur le réseau.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
interface DataConnection extends Remote {
    /**
     * Retourne le système de référence des coordonnées selon lequel seront exprimées
     * l'{@linkplain #getEnvelope enveloppe}, les {@linkplain #evaluate coordonnées
     * des valeurs}, <cite>etc.</cite>
     *
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem() throws RemoteException;

    /**
     * Retourne l'enveloppe spatio-temporelle des données.
     *
     * @throws CatalogException si la base de données n'a pas pu être interrogée.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    Envelope getEnvelope() throws CatalogException, RemoteException;

    /**
     * Retourne la partie géographique de l'{@linkplain #getEnvelope enveloppe} des données.
     *
     * @throws CatalogException si la base de données n'a pas pu être interrogée.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException, RemoteException;

    /**
     * Returns the set of dates when a coverage is available.
     *
     * @return The set of dates.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException If an error occured while reading the database.
     * @throws RemoteException if a problem occured while communicating with the remote server.
     */
    SortedSet<Date> getAvailableTimes() throws CatalogException, SQLException, RemoteException;

    /**
     * Returns the set of altitudes where a coverage is available. If different images
     * have different set of altitudes, then this method returns only the altitudes
     * found in every images.
     *
     * @return The set of altitudes. May be empty, but will never be null.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException If an error occured while reading the database.
     * @throws RemoteException if a problem occured while communicating with the remote server.
     */
    SortedSet<Number> getAvailableElevations() throws CatalogException, SQLException, RemoteException;

    /**
     * Retourne la partie temporelle de l'{@linkplain #getEnvelope enveloppe} des données.
     *
     * @throws CatalogException si la base de données n'a pas pu être interrogée.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    DateRange getTimeRange() throws CatalogException, RemoteException;

    /**
     * Modifie la partie temporelle de l'{@linkplain #getEnvelope enveloppe} des données.
     * Toutes les images qui interceptent cette plage de temps seront pris en compte lors
     * du prochain appel de {@link #getEntries}.
     *
     * @return {@code true} si la plage de temps à changée, ou {@code false} si les valeurs
     *         spécifiées étaient les mêmes que la dernière fois.
     *
     * @throws CatalogException si la base de données n'a pas pu être interrogée.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    boolean setTimeRange(final Date startTime, final Date endTime) throws CatalogException, RemoteException;

    /**
     * Returns the vertical range of the data.
     *
     * @return The vertical range of the data.
     * @throws CatalogException if the vertical range can not be obtained.
     * @throws RemoteException if a problem occured while communicating with the remote server.
     */
    NumberRange getVerticalRange() throws CatalogException, RemoteException;

    /**
     * Sets the vertical range of the data.
     *
     * @param  minimum The minimal <var>z</var> value.
     * @param  maximum The maximal <var>z</var> value.
     * @return {@code true} if the vertical range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     * @throws CatalogException if the vertical range can not be obtained.
     * @throws RemoteException if a problem occured while communicating with the remote server.
     */
    boolean setVerticalRange(double minimum, double maximum) throws CatalogException, RemoteException;

    /**
     * Retourne la valeur d'une bande à une position interpolée dans l'ensemble des images de cette
     * table. L'ensemble des données est traité comme une matrice tri-dimensionnelle. La coordonnée
     * doit être exprimée selon le {@linkplain #getCoordinateReferenceSystem système de référence
     * des coordonnées de la table}. Ces coordonnées sont habituellement (mais pas obligatoirement):
     * <p>
     * <ul>
     *   <li>La longitude, en degrés décimaux par rapport au méridien de Greenwich</li>
     *   <li>La latitude, en degrés décimaux</li>
     *   <li>Le temps, en nombre de jours depuis le 1er janvier 1950 00:00 UTC.</li>
     * </ul>
     * <p>
     * La signature de cette méthode (à base de types primitifs seulement) vise à réduire les temps
     * de transfert sur le réseau dans le contexte des appels RMI.
     *
     * @throws CatalogException si un enregistrement de la base de données est invalide.
     * @throws SQLException     si la base de données n'a pas pu être interrogée pour une autre raison.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     * @throws IOException      si la lecture d'une image a échoué.
     */
    double evaluate(double x, double y, double t, short band) throws CatalogException, SQLException, IOException;

    /**
     * Retourne les coordonnées au centre du voxel le plus proche des coordonnées spécifiées.
     * Cette méthode recherche l'image la plus proche de la date spécifiée, puis recherche le
     * pixel qui contient la coordonnée géographique spécifiée. La date de milieu de l'image,
     * ainsi que les coordonnées géographiques au centre du pixel, sont retournées. Appeller
     * la méthode {@link #evaluate evaluate} avec les coordonnées retournées devrait permettre
     * d'obtenir une valeur non-interpollée.
     * <p>
     * La signature de cette méthode (à base de types primitifs seulement) vise à réduire les temps
     * de transfert sur le réseau dans le contexte des appels RMI. En particulier on retourne un
     * tableau de {@code double} plutôt qu'un objet {@link DirectPosition} afin d'éviter le transfert
     * d'un objet {@link CoordinateReferenceSystem} sur le réseau.
     *
     * @throws CatalogException si un enregistrement de la base de données est invalide.
     * @throws SQLException     si la base de données n'a pas pu être interrogée pour une autre raison.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     * @throws IOException      si la lecture d'une image a échoué.
     */
    double[] snap(double x, double y, double t) throws CatalogException, SQLException, IOException;

    /**
     * Retourne les couvertures utilisées par les méthodes {@code evaluate} pour le temps <var>t</var>
     * spécifié. L'ensemble retourné comprendra typiquement 0, 1 ou 2 éléments.
     *
     * @throws CatalogException si un enregistrement de la base de données est invalide.
     * @throws SQLException     si la base de données n'a pas pu être interrogée pour une autre raison.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     * @throws IOException      si la lecture d'une image a échoué.
     */
    List<Coverage> coveragesAt(DirectPosition position) throws CatalogException, SQLException, IOException;

    /**
     * Retourne la liste des images disponibles.
     *
     * @return Liste d'images qui interceptent la plage de temps et la région géographique d'intérêt.
     * @throws CatalogException si un enregistrement de la base de données est invalide.
     * @throws SQLException     si la base de données n'a pas pu être interrogée pour une autre raison.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    Set<CoverageReference> getEntries() throws CatalogException, SQLException, RemoteException;

    /**
     * Retourne une des images disponibles dans la plage de coordonnées spatio-temporelles
     * préalablement sélectionnées. Si plusieurs images interceptent la région et la plage
     * de temps (c'est-à-dire si {@link #getEntries} retourne un ensemble d'au moins deux
     * entrées), alors le choix de l'image se fera en utilisant un objet
     * {@link net.seagis.observation.coverage.CoverageComparator} par défaut.
     *
     * @return Une image choisie arbitrairement dans la région et la plage de date
     *         sélectionnées, ou {@code null} s'il n'y a pas d'image dans ces plages.
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si la base de données n'a pas pu être interrogée pour une autre raison.
     */
    CoverageReference getEntry() throws CatalogException, SQLException, RemoteException;

    /**
     * Retourne une nouvelle connexion vers les données pour l'opération spécifiées.
     * L'envelope spatio-temporelle restera la même.
     *
     * @param  operation  L'opération à appliquer, ou {@code null} si aucune.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    DataConnection newInstance(Operation operation) throws RemoteException;
}
