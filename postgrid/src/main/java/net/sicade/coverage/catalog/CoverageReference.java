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
package net.sicade.coverage.catalog;

import java.util.Date;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import net.sicade.catalog.Element;

import org.opengis.coverage.SampleDimension;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.image.io.IIOListeners;
import org.geotools.coverage.CoverageStack;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.util.NumberRange;

import net.sicade.util.DateRange;


/**
 * Méta-données concernant une image, et éventuellement une référence vers l'image elle-même.
 * Un objet {@code CoverageReference} permet d'obtenir quelques propriétés sur une image telles
 * que sa date et sa couverture géographique, sans nécessiter une connexion à l'image elle-même.
 * L'image ne sera téléchargée que la première fois où elle sera demandée, lors d'un appel à la
 * méthode {@link #getCoverage}.
 * <p>
 * Les objets {@code CoverageReference} sont imutables et sécuritaires dans un environnement
 * multi-threads.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface CoverageReference extends Element, CoverageStack.Element {
    /**
     * Clé sous laquelle mémoriser l'objet {@code CoverageReference} source dans les propriétés de
     * {@link GridCoverage2D}. Cette propriété permet de retrouver l'objet {@code CoverageReference}
     * qui a produit un objet {@code GridCoverage2D}. Exemple:
     *
     * <blockquote><pre>
     * CoverageReference reference = ...
     * GridCoverage2D    coverage  = reference.{@linkplain #getCoverage getCoverage}(null);
     * CoverageReference source    = (CoverageReference) coverage.getProperty(CoverageReference.SOURCE_KEY);
     * assert source == reference;
     * </pre></blockquote>
     */
    String SOURCE_KEY = "net.sicade.observation.CoverageReference";

    /**
     * Retourne la couche à laquelle appartient cette image.
     */
    Layer getLayer();

    /**
     * Retourne le format de cette image.
     */
    Format getFormat();

    /**
     * Retourne le chemin de l'image, ou {@code null} si le fichier n'est pas accessible localement.
     * Dans ce dernier cas, {@link #getURL} devra être utilisé à la place.
     */
    File getFile();

    /**
     * Retourne l'URL de l'image, ou {@code null} si le fichier n'est pas accessible ni localement,
     * ni à travers un réseau.
     */
    URL getURL();

    /**
     * Retourne le système de référence des coordonnées de l'image. En général, ce système de
     * référence aura trois dimensions (la dernière dimension étant le temps), soit dans l'ordre:
     * <p>
     * <ul>
     *   <li>Les longitudes, en degrés selon l'ellipsoïde WGS 1984.</li>
     *   <li>Les latitudes,  en degrés selon l'ellipsoïde WGS 1984.</li>
     *   <li>Le temps, en jours juliens depuis le 01/01/1950 00:00 UTC.</li>
     * </ul>
     * <p>
     * Bien que toutes les images provenant d'une même {@linkplain Layer couche} ont en
     * général le même système de référence des coordonnées, ce n'est pas toujours le cas.
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Retourne les coordonnées spatio-temporelles de l'image. Le système de référence des
     * coordonnées utilisé est {@linkplain #getCoordinateReferenceSystem celui de l'image}.
     */
    Envelope getEnvelope();

    /**
     * Retourne la plage de temps couverte par l'image, selon les unités de l'axe temporel.
     * Cette méthode est fournit principalement afin de supporter l'interface
     * {@link org.geotools.coverage.CoverageStack.Element}. Pour les autres usage,
     * la méthode {@link #getTimeRange} peut être une alternative plus pratique.
     */
    NumberRange getZRange();

    /**
     * Retourne la plage de temps couverte par l'image. Cette plage sera délimitée
     * par des objets {@link Date}. Appeler cette méthode équivaut à n'extraire que
     * la partie temporelle de l'{@linkplain #getEnvelope enveloppe} et à transformer
     * les coordonnées si nécessaire.
     */
    DateRange getTimeRange();

    /**
     * Retourne les coordonnées géographiques de la région couverte par l'image. Les coordonnées
     * seront exprimées en degrés de longitudes et de latitudes selon l'ellipsoïde WGS 1984.
     * Appeler cette méthode équivaut parfois à n'extraire que la partie horizontale de
     * l'{@linkplain #getEnvelope enveloppe} et à transformer les coordonnées si nécessaire.
     * Toutefois dans certains cas cette méthode peut retourner une région géographique plus
     * grande que l'{@linkplain #getEnvelope enveloppe}, par exemple comme un effet des
     * transformations de coordonnées ou encore parce que l'image (et par conséquence son
     * {@linkplain #getEnvelope enveloppe}) sera découpée au moment de la lecture.
     */
    GeographicBoundingBox getGeographicBoundingBox();

    /**
     * Retourne des informations sur la géométrie de l'image. Ces informations comprennent notamment
     * la taille de l'image (en pixels) ainsi que la transformation à utiliser pour passer des
     * coordonnées pixels vers les coordonnées selon le {@linkplain #getCoordinateReferenceSystem
     * système de référence de l'image}. Cette transformation sera le plus souvent affine.
     */
    GridGeometry2D getGridGeometry();

    /**
     * Retourne les bandes de l'image. Cette méthode retourne toujours la version geophysique des
     * bandes (<code>{@linkplain GridSampleDimension#geophysics geophysics}(true)</code>), ce qui
     * est cohérent avec le type d'image retourné par {@link #getCoverage getCoverage(...)}.
     *
     * @return La liste des catégories géophysiques pour chaque bande de l'image.
     *         La longueur de ce tableau sera égale au nombre de bandes.
     */
    SampleDimension[] getSampleDimensions();

    /**
     * Retourne l'image correspondant à cette entrée. Cette méthode retourne toujours la version
     * geophysique de l'image (<code>{@linkplain GridCoverage2D#geophysics geophysics}(true)</code>).
     * <p>
     * Si l'image avait déjà été lue précédemment et qu'elle n'a pas encore été réclamée par le
     * ramasse-miette, alors l'image existante sera retournée sans qu'une nouvelle lecture du
     * fichier ne soit nécessaire. Si au contraire l'image n'était pas déjà en mémoire, alors
     * un décodage du fichier sera nécessaire.
     * <p>
     * Certaines implémentations peuvent utiliser en interne les RMI (<cite>Remote Method Invocation</cite>).
     * Dans ce dernier cas, cette méthode effectuera le découpage géographique et appliquera d'eventuelles
     * opérations (par exemple un calcul de gradient) sur le serveur; seul le résultat sera envoyé à travers
     * le réseau vers le client. Il est toutefois possible que la qualité du résultat soit dégradée pour une
     * transmission plus compacte sur le réseau.
     *
     * @param  listeners Liste des objets à informer des progrès de la lecture ainsi que des
     *         éventuels avertissements, ou {@code null} s'il n'y en a pas.
     * @return Image lue, ou {@code null} si l'utilisateur a {@linkplain #abort interrompu la lecture}.
     * @throws IOException si le fichier n'a pas été trouvé ou si une autre erreur d'entrés/sorties
     *         est survenue.
     * @throws IIOException s'il n'y a pas de décodeur approprié pour l'image, ou si l'image n'est
     *         pas valide.
     * @throws RemoteException si un problème est survenu lors de la communication avec le serveur.
     */
    GridCoverage2D getCoverage(IIOListeners listeners) throws IOException;

    /**
     * Annule la lecture de l'image. Cette méthode peut être appelée à partir de n'importe quel
     * thread.  Si la méthode {@link #getCoverage getCoverage(...)} était en train de lire une
     * image dans un autre thread, elle s'arrêtera et retournera {@code null}.
     */
    void abort();


    /**
     * Une référence qui délègue son travail à une autre instance de {@link CoverageReference}.
     * L'implémentation par défaut redirige tous les appels des méthodes vers l'objet {@link
     * CoverageReference} qui a été spécifié lors de la construction. Les classes dérivées
     * vont typiquement redéfinir quelques méthodes afin d'ajouter ou de modifier certaines
     * fonctionalitées.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Proxy extends net.sicade.catalog.Proxy implements CoverageReference {
        /**
         * Pour compatibilités entre les enregistrements binaires de différentes versions.
         */
        private static final long serialVersionUID = 1679051552440633120L;

        /**
         * Référence enveloppée par ce proxy.
         */
        private final CoverageReference ref;

        /**
         * Construit un proxy qui redirigera tous les appels vers la référence spécifiée.
         */
        protected Proxy(final CoverageReference ref) {
            this.ref = ref;
            if (ref == null) {
                throw new NullPointerException();
            }
        }

        public CoverageReference         getBackingElement()            {return ref;}
        public Layer                     getLayer()                     {return ref.getLayer();}
        public Format                    getFormat()                    {return ref.getFormat();}
        public File                      getFile()                      {return ref.getFile();}
        public URL                       getURL()                       {return ref.getURL();}
        public GridGeometry2D            getGridGeometry()              {return ref.getGridGeometry();}
        public CoordinateReferenceSystem getCoordinateReferenceSystem() {return ref.getCoordinateReferenceSystem();}
        public Envelope                  getEnvelope()                  {return ref.getEnvelope();}
        public NumberRange               getZRange()                    {return ref.getZRange();}
        public DateRange                 getTimeRange()                 {return ref.getTimeRange();}
        public GeographicBoundingBox     getGeographicBoundingBox()     {return ref.getGeographicBoundingBox();}
        public SampleDimension[]         getSampleDimensions()          {return ref.getSampleDimensions();}
        public void                      abort()                        {       ref.abort();}
        public GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
            return ref.getCoverage(listeners);
        }
    }
}
