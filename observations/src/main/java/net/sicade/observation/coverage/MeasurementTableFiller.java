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
import net.sicade.observation.coverage.sql.DescriptorTable;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.XArray;


/**
 * Remplit une table des {@linkplain Observation observations} � partir des donn�es satellitaires.
 * La table {@code "Measurements"} contient les valeurs de descripteurs du paysage oc�anique tels
 * que la temp�rature, chlorophylle-<var>a</var>, hauteur de l'eau, <cite>etc.</cite> aux positions
 * des observations. Lorsque {@code MeasurementTableFiller} trouve une donn�es environnementale � une
 * position d'une observation, il ajoute un enregistrement � la table {@code "Measurements"}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class MeasurementTableFiller implements Runnable {
    /**
     * Mis � {@code true} quand l'utilisateur a demand� � annuler l'ex�cution de {@link #execute}.
     */
    private volatile boolean cancel;

    /**
     * Ensemble des stations concern�es.
     */
    private final Set<Station> stations = new LinkedHashSet<Station>();

    /**
     * Ensemble des descripteurs � utiliser pour remplir la table des mesures.
     */
    private final Set<Descriptor> descriptors = new LinkedHashSet<Descriptor>();

    /**
     * La table des mesures � modifier.
     */
    private final MeasurementTable measures;

    /**
     * Composante graphique qui a lanc� ce calcul, ou {@code null} si aucun.
     * Utilis� uniquement pour l'affichage �ventuel d'un message d'erreur.
     */
    private transient Component owner;

    /**
     * Construit un objet qui proc�dera au remplissage de la table des mesures sp�cifi�e.
     */
    public MeasurementTableFiller(final MeasurementTable measures) {
        this.measures = measures;
    }

    /**
     * Retourne l'ensemble des stations pour lesquelles on voudra calculer les descripteurs du
     * paysage oc�anique. L'ensemble retourn� est modifiable; il est possible d'ajouter ou de
     * retirer des stations � prendre en compte en appelant {@link Set#add} ou {@link Set#remove}.
     */
    public Set<Station> stations() {
        return stations;
    }

    /**
     * Retourne l'ensemble des descripteurs � �valuer pour chaque station. L'ensemble retourn� est
     * modifiable; il est possible d'ajouter ou de retirer des descripteurs � prendre en compte en
     * appelant {@link Set#add} ou {@link Set#remove}.
     */
    public Set<Descriptor> descriptors() {
        return descriptors;
    }

    /**
     * Utilise un ensemble de stations par d�faut pour lesquelles on voudra calculer les
     * descripteurs du paysage oc�anique. Cet ensemble est constitu� de stations pour lesquelles
     * {@link MeasurementTable} pourrait avoir des donn�es.
     *
     * @throws CatalogException si l'interrogation de la base de donn�es a �chou�e.
     */
    public synchronized void addDefaultStations() throws CatalogException {
        Descriptor.LOGGER.info("Obtient l'ensemble des stations.");
        try {
            stations.addAll(measures.getStations());
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Utilise un ensemble de descripteurs par d�faut � �valuer pour chaque station.
     *
     * @throws CatalogException si l'interrogation de la base de donn�es a �chou�e.
     */
    public synchronized void addDefaultDescriptors() throws CatalogException {
        Descriptor.LOGGER.info("Obtient l'ensemble des descripteurs.");
        try {
            descriptors.addAll(measures.getDatabase().getTable(DescriptorTable.class).getEntries());
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Classe les �l�ments du tableau sp�cifi� en ordre croissant.
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
     * @throws CatalogException si un probl�me est survenu lors des acc�s au catalogue.
     */
    public synchronized void execute() throws CatalogException {
        final Set<Station>    stations    = stations();
        final Set<Descriptor> descriptors = descriptors();
        if (descriptors.isEmpty()) {
            Descriptor.LOGGER.warning("L'ensemble des descripteurs est vide.");
            return;
        }
        if (stations.isEmpty()) {
            Descriptor.LOGGER.warning("L'ensemble des stations est vide.");
            return;
        }
        int withoutPosition = 0; // Compte le nombre de stations sans coordonn�es.
        final LinkedList<Descriptor> remaining = new LinkedList<Descriptor>(descriptors);
        final MeasurementInserts updater = new MeasurementInserts(measures);
        updater.start();
        /*
         * On traitera ensemble tous les descripteurs qui correspondent � la m�me s�rie d'images,
         * pour �viter de charger en m�moire les m�me images plusieurs fois.  On �vite de traiter
         * en m�me temps des s�ries d'images diff�rentes pour �viter de consommer trop de m�moire
         * lors de la cr�ation du tableau de paires stations-descripteurs plus bas.
         *
         * Note: Nous rempla�ont la variable de classe 'descriptors',  qui contenait l'ensemble
         *       des descripteurs, par une liste locale qui ne contient que les descripteurs de
         *       la m�me s�rie. Ca ne change rien pour l'algorithme qui suit,  except� que cela
         *       peut jouer sur les performances et la consommation de m�moire.
         */
        while (!remaining.isEmpty()) {
            final Series series = remaining.getFirst().getPhenomenon();
            final List<Descriptor> descriptorList = new ArrayList<Descriptor>(remaining.size());
            for (final Iterator<Descriptor> it=remaining.iterator(); it.hasNext();) {
                final Descriptor descriptor = it.next();
                if (Utilities.equals(descriptor.getPhenomenon(), series)) {
                    descriptorList.add(descriptor);
                    it.remove();
                }
            }
            if (descriptorList.isEmpty()) {
                throw new AssertionError(series);
            }
            assert Collections.disjoint(descriptorList, remaining) : series;
            assert descriptors.containsAll(descriptorList) : series;
            Descriptor.LOGGER.info("Traitement de la s�rie \"" + series.getName() +
                                   "\" (" + descriptorList.size() + " descripteurs)");
            /*
             * La variable locale 'descriptorList' ne contient maintenant qu'un sous-ensemble des
             * descripteurs pour une m�me s�rie. On voudra �valuer ces descripteurs aux positions
             * des stations, mais pas n�cessairement dans l'ordre chronologique des stations.
             * L'ordre d�pendra aussi des d�calages temporelles des descripteurs, car l'objectif
             * est de traiter toutes les stations qui correspondant � une m�me image (incluant les
             * stations 10 jours plus tard mais qui souhaite les valeurs environnementales 10 jours
             * auparavant) avant de passer � l'image suivante.
             */
            StationDescriptorPair[] pairs = new StationDescriptorPair[descriptorList.size() * stations.size()];
            final Map<Descriptor,SpatioTemporalCoverage3D> coverages = new IdentityHashMap<Descriptor,SpatioTemporalCoverage3D>();
            int index = 0;
            for (final Descriptor descriptor : descriptorList) {
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
            Descriptor.LOGGER.info("�valuation de " + index + " valeurs.");
            /*
             * Maintenant que l'on connait toutes les paires de descripteurs et de stations,
             * proc�de � l'extraction des valeurs dans leur ordre chronologique.
             */
            cancel = false;
            float[] values = null;
            for (index=0; index<pairs.length; index++) {
                final StationDescriptorPair    pair       = pairs[index];
                final Station                  station    = pair.station;
                final Descriptor               descriptor = pair.descriptor;
                final SpatioTemporalCoverage3D coverage   = coverages.get(descriptor);
                final Point2D                  coord      = station.getCoordinate();
                final Date                     time       = station.getTime();
                pairs[index] = null;
                if (coord==null || time==null) {
                    withoutPosition++;
                    continue;
                }
                try {
                    values = coverage.evaluate(coord, time, values);
                } catch (PointOutsideCoverageException exception) {
                    warning(coverage, exception);
                    continue;
                }
                pair.value = values[0];
                updater.add(pair);
                if (cancel) {
                    updater.finished();
                    Descriptor.LOGGER.info("Remplissage interrompu.");
                    return;
                }
            }
        }
        if (withoutPosition != 0) {
            Descriptor.LOGGER.warning("Les coordonn�es de " + withoutPosition +
                                      " couple(s) (station, descripteur) sont incompl�tes.");
        }
        updater.finished();
        Descriptor.LOGGER.info("Remplissage de la table des mesures termin�.");
    }

    /**
     * Lance le remplissage de la table {@code "Measurements"}. Cette m�thode est identique
     * � {@link #execute}, except� qu'elle attrape les �ventuelles exceptions et les fait
     * appara�tre dans une interface utilisateur.
     */
    public void run() {
        try {
            execute();
        } catch (Exception exception) {
            ExceptionMonitor.show(owner, exception);
        }
    }

    /**
     * Appelle {@link #run} dans un thread en arri�re-plan.
     */
    public void start() {
        final Thread thread = new Thread(this, "Remplissage de la table des mesures");
        thread.setPriority(Thread.MIN_PRIORITY + 2);
        thread.start();
    }

    /**
     * �crit un message dans le journal avec le niveau "info".
     */
    private static void info(final int key, final Object arg) {
        LogRecord record = Resources.getResources(null).getLogRecord(Level.INFO, key, arg);
        record.setSourceClassName("MeasurementTableFiller");
        record.setSourceMethodName("execute");
        Descriptor.LOGGER.log(record);
    }

    /**
     * Indique qu'un point est en dehors de la r�gion des donn�es couvertes.
     * Cette m�thode �crit un avertissement dans le journal, � la condition
     * qu'il n'y en avait pas d�j� un.
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
     * Interrompt l'ex�cution de {@link #execute}. Cette m�thode peut �tre appel�e � partir de
     * n'importe quel thread.
     */
    public void cancel() {
        cancel = true;
    }
}
