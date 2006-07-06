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
package net.sicade.observation.coverage;

// J2SE dependencies
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.lang.reflect.UndeclaredThrowableException;
import java.awt.Component;
import java.awt.geom.Point2D;
import java.sql.SQLException;

// OpenGIS dependencies
import org.opengis.coverage.PointOutsideCoverageException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.gui.swing.ExceptionMonitor;
import org.geotools.coverage.SpatioTemporalCoverage3D;

// Sicade dependencies
import net.sicade.observation.Station;
import net.sicade.observation.Measurement;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.MeasurementTable;
import net.sicade.observation.gui.DescriptorChooser;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.XArray;


/**
 * Remplit une table des {@linkplain Observation observations} à partir des données satellitaires.
 * La table {@code "Measurements"} contient les valeurs de descripteurs du paysage océanique tels
 * que la température, chlorophylle-<var>a</var>, hauteur de l'eau, <cite>etc.</cite> aux positions
 * des observations. Lorsque {@code MeasurementTableFiller} trouve une données environnementale à une
 * position d'une observation, il ajoute un enregistrement à la table {@code "Measurements"}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class MeasurementTableFiller implements Runnable {
    /**
     * {@code true} si on veut seulement tester cette classe sans écrire
     * dans la base de données. Note: une connexion en lecture aux bases
     * de données est tout de même nécessaire.
     */
    private static final boolean TEST_ONLY = false;

    /**
     * Mis à {@code true} quand l'utilisateur a demandé à annuler l'exécution de {@link #execute}.
     */
    private volatile boolean cancel;

    /**
     * Ensemble des stations concernées.
     */
    private final Set<Station> stations = new LinkedHashSet<Station>();

    /**
     * Ensemble des descripteurs à utiliser pour remplir la table des mesures.
     */
    private final Set<Descriptor> descriptors = new LinkedHashSet<Descriptor>();

    /**
     * La table des mesures à modifier.
     */
    private final MeasurementTable measures;

    /**
     * Composante graphique qui a lancé ce calcul, ou {@code null} si aucun.
     * Utilisé uniquement pour l'affichage éventuel d'un message d'erreur.
     */
    private transient Component owner;

    /**
     * Construit un objet qui procèdera au remplissage de la table des mesures spécifiée.
     */
    public MeasurementTableFiller(final MeasurementTable measures) {
        this.measures = measures;
    }

    /**
     * Utilise un ensemble de stations par défaut pour lesquelles on voudra calculer les
     * descripteurs du paysage océanique. Cet ensemble est constitué de stations pour lesquelles
     * {@link MeasurementTable} pourrait avoir des données.
     *
     * @throws CatalogException si l'interrogation de la base de données a échouée.
     */
    public synchronized void addDefaultStations() throws CatalogException {
        Descriptor.LOGGER.info("Obtient l'ensemble des stations.");
        try {
            addStations(measures.getStations());
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Ajoute des stations à l'ensemble de celles pour lesquelles on voudra calculer les
     * descripteurs du paysage océanique.
     */
    public synchronized void addStations(final Collection<Station> stations) {
        this.stations.addAll(stations);
    }

    /**
     * Retire des stations de l'ensemble de celles pour lesquelles on voudra calculer les
     * descripteurs du paysage océanique.
     */
    public synchronized void removeStations(final Collection<Station> stations) {
        this.stations.removeAll(stations);
    }

    /**
     * Ajoute des descripteurs à l'ensemble de ceux qui seront à évaluer pour chaque station.
     */
    public synchronized void addDescriptors(final Collection<Descriptor> descriptors) {
        this.descriptors.addAll(descriptors);
    }

    /**
     * Retire des descripteurs à l'ensemble de ceux qui seront à évaluer pour chaque station.
     */
    public synchronized void removeDescriptors(final Collection<Descriptor> descriptors) {
        this.descriptors.removeAll(descriptors);
    }

    /**
     * Classe les éléments du tableau spécifié en ordre croissant.
     */
    private static void sort(final Object[] array) throws CatalogException {
        try {
            Arrays.sort(array);
        } catch (UndeclaredThrowableException exception) {
            final Throwable cause = exception.getUndeclaredThrowable();
            if (cause instanceof CatalogException) {
                throw (CatalogException) cause;
            }
            throw exception;
        }
    }

    /**
     * Lance le remplissage de la table {@code "Measurements"}.
     *
     * @throws CatalogException si un problème est survenu lors des accès au catalogue.
     */
    public synchronized void execute() throws CatalogException {
        if (descriptors.isEmpty()) {
            Descriptor.LOGGER.warning("L'ensemble des descripteurs est vide.");
            return;
        }
        if (stations.isEmpty()) {
            Descriptor.LOGGER.warning("L'ensemble des stations est vide.");
            return;
        }
        final LinkedList<Descriptor> remaining = new LinkedList<Descriptor>(descriptors);
        /*
         * On traitera ensemble tous les descripteurs qui correspondent à la même série d'images,
         * pour éviter de charger en mémoire les même images plusieurs fois.  On évite de traiter
         * en même temps des séries d'images différentes pour éviter de consommer trop de mémoire
         * lors de la création du tableau de paires stations-descripteurs plus bas.
         *
         * Note: Nous cachons la variable de classe 'descriptors', qui contenait l'ensemble
         *       des descripteurs,  par une variable locale de même nom qui ne contient que
         *       les descripteurs de la même série. Ca ne change rien pour l'algorithme qui
         *       suit,  excepté que cela peut jouer sur les performances et la consommation
         *       de mémoire.
         */
        while (!remaining.isEmpty()) {
            final Series series = remaining.getFirst().getPhenomenon();
            final List<Descriptor> descriptors = new ArrayList<Descriptor>(remaining.size());
            for (final Iterator<Descriptor> it=remaining.iterator(); it.hasNext();) {
                final Descriptor descriptor = it.next();
                if (Utilities.equals(descriptor.getPhenomenon(), series)) {
                    descriptors.add(descriptor);
                    it.remove();
                }
            }
            if (descriptors.isEmpty())   throw new AssertionError(series);
            assert Collections.disjoint(descriptors, remaining) : series;
            assert this.descriptors.containsAll(descriptors)    : series;
            Descriptor.LOGGER.info("Traitement de la série \"" + series.getName() +
                                   "\" (" + descriptors.size() + " descripteurs)");
            /*
             * La variable locale 'descriptors' ne contient maintenant qu'un sous-ensemble des
             * descripteurs pour une même série. On voudra évaluer ces descripteurs aux positions
             * des stations, mais pas nécessairement dans l'ordre chronologique des stations.
             * L'ordre dépendra aussi des décalages temporelles des descripteurs, car l'objectif
             * est de traiter toutes les stations qui correspondant à une même image (incluant les
             * stations 10 jours plus tard mais qui souhaite les valeurs environnementales 10 jours
             * auparavant) avant de passer à l'image suivante.
             */
            StationDescriptorPair[] pairs = new StationDescriptorPair[descriptors.size() * stations.size()];
            final Map<Descriptor,SpatioTemporalCoverage3D> coverages = new IdentityHashMap<Descriptor,SpatioTemporalCoverage3D>();
            int index = 0;
            for (final Descriptor descriptor : descriptors) {
                if (coverages.put(descriptor, new SpatioTemporalCoverage3D(null, descriptor.getCoverage())) != null) {
                    throw new AssertionError(descriptor);
                }
                measures.setObservable(descriptor);
                for (final Station station : stations) {
                    measures.setStation(station);
                    try {
                        if (measures.exists()) {
                            continue;
                        }
                    } catch (SQLException exception) {
                        throw new ServerException(exception);
                    }
                    pairs[index++] = new StationDescriptorPair(station, descriptor);
                }
            }
            pairs = XArray.resize(pairs, index);
            sort(pairs);
            Descriptor.LOGGER.info("Évaluation de " + index + " valeurs.");
            /*
             * Maintenant que l'on connait toutes les paires de descripteurs et de stations,
             * procède à l'extraction des valeurs dans leur ordre chronologique.
             */
            cancel = false;
            float[] values = null;
            for (index=0; index<pairs.length; index++) {
                final StationDescriptorPair    pair       = pairs[index]; pairs[index] = null;
                final Station                  station    = pair.station;
                final Descriptor               descriptor = pair.descriptor;
                final SpatioTemporalCoverage3D coverage   = coverages.get(descriptor);
                final Point2D                  coord      = station.getCoordinate();
                final Date                     time       = station.getTime();
                try {
                    values = coverage.evaluate(coord, time, values);
                } catch (PointOutsideCoverageException exception) {
                    warning(coverage, exception);
                    continue;
                }
                final float value = values[0];
                measures.setObservable(descriptor);
                measures.setStation   (station);
                try {
                    if (TEST_ONLY) {
                        final Measurement current = measures.getEntry();
                        System.out.print("INSERT station=");
                        System.out.print(station.getName());
                        System.out.print(" value=");
                        System.out.print(value);
                        if (current != null) {
                            System.out.print("  (old=");
                            System.out.print(current.getValue());
                            System.out.print(')');
                        }
                        System.out.println();
                    } else {
                        measures.setValue(value, Float.NaN);
                    }
                } catch (SQLException exception) {
                    throw new ServerException(exception);
                }
                if (cancel) {
                    Descriptor.LOGGER.info("Remplissage interrompu.");
                    return;
                }
            }
        }
        Descriptor.LOGGER.info("Remplissage de la table des mesures terminé.");
    }

    /**
     * Lance le remplissage de la table {@code "Measurements"}. Cette méthode est identique
     * à {@link #execute}, excepté qu'elle attrape les éventuelles exceptions et les fait
     * apparaître dans une interface utilisateur.
     */
    public void run() {
        try {
            execute();
        } catch (Exception exception) {
            ExceptionMonitor.show(owner, exception);
        }
    }

    /**
     * Appelle {@link #run} dans un thread en arrière-plan.
     */
    public void start() {
        final Thread thread = new Thread(this, "Remplissage de la table des mesures");
        thread.setPriority(Thread.MIN_PRIORITY + 2);
        thread.start();
    }

    /**
     * Écrit un message dans le journal avec le niveau "info".
     */
    private static void info(final int key, final Object arg) {
        LogRecord record = Resources.getResources(null).getLogRecord(Level.INFO, key, arg);
        record.setSourceClassName("MeasurementTableFiller");
        record.setSourceMethodName("execute");
        Descriptor.LOGGER.log(record);
    }

    /**
     * Indique qu'un point est en dehors de la région des données couvertes.
     * Cette méthode écrit un avertissement dans le journal, à la condition
     * qu'il n'y en avait pas déjà un.
     */
    private static void warning(final SpatioTemporalCoverage3D      source, 
                                final PointOutsideCoverageException exception) 
    {
        final LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
        record.setSourceClassName ("MeasurementTableFiller");
        record.setSourceMethodName("execute");
        record.setThrown(exception);
    }

    /**
     * Interrompt l'exécution de {@link #execute}. Cette méthode peut être appelée à partir de
     * n'importe quel thread.
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * Composante graphique pour démarrer le remplissage de la table des mesures. Cette composante
     * graphique demandera à l'utilisateur de sélectionner un sous-ensemble de descripteurs parmis
     * les descripteurs qui ont été spécifiés à {@link MeasurementTableFiller}. Si l'utilisateur
     * appuie sur le bouton "Exécuter" après cette sélection, alors cette objet appelera
     * {@link MeasurementTableFiller#execute} pour les descripteurs sélectionnés.
     * <p>
     * Pour faire apparaître cette composante graphique et permettre ainsi le lancement du
     * remplissage de la table des mesures, appelez {@link #show}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public class Starter extends DescriptorChooser {
        /**
         * Construit une composante graphique pour les descripteurs actuellement déclarés dans
         * l'objet {@link MeasurementTableFiller}.
         */
        public Starter() {
            super(descriptors);
        }

        /**
         * Appelée automatiquement lorsque l'utilisateur a appuyé sur le bouton "Exécuter".
         * L'implémentation par défaut réduit les descripteur de {@link MeasurementTableFiller}
         * à l'ensemble sélectionné par l'utilisateur, et appelle
         * {@link MeasurementTableFiller#start start()}.
         */
        @Override
        protected void execute() {
            synchronized (MeasurementTableFiller.this) {
                descriptors.clear();
                addDescriptors(getDescriptors(true));
                start();
            }
        }

        /**
         * Appelée automatiquement lorsque l'utilisateur a appuyé sur le bouton "Annuler".
         * L'implémentation par défaut interrompt l'exécution lancée par
         * {@link MeasurementTableFiller#start start()}.
         */
        @Override
        protected void cancel() {
            cancel = true;
            super.cancel();
        }
    }
}
