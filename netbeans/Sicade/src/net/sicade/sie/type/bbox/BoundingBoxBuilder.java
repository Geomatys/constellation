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
package net.sicade.sie.type.bbox;

// J2SE dependencies
import java.util.Set;
import java.util.LinkedList;

// OpenIDE dependencies
import org.openide.ErrorManager;

// Sicade dependencies
import net.sicade.observation.Observations;
import net.sicade.observation.coverage.Series;


/**
 * Complète la construction des objets {@link BoundingBox} en obtenant leurs séries de données.
 * Cette étape est exécutée en arrière plan, de sorte que les délais de connexion à la base de
 * données ne bloquent pas l'usage de l'application.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class BoundingBoxBuilder implements Runnable {
    /**
     * L'instance unique de {@code BoundingBoxBuilder}.
     */
    public static final BoundingBoxBuilder DEFAULT = new BoundingBoxBuilder();

    /**
     * Liste des objets {@link BoundingBox} en attente d'avoir leurs séries de données.
     */
    private final LinkedList<BoundingBox> queue = new LinkedList<BoundingBox>();

    /**
     * L'objet en cours de traitement. Cet objet a été retiré de la liste {@link #queue},
     * mais sa référence est conservée jusqu'à la fin du traitement afin d'éviter que
     * {@link #add} ne l'ajoute encore inutilement.
     */
    private transient BoundingBox processing;

    /**
     * Construit une instance de {@code BoundingBoxBuilder} qui sera immédiatement démarré
     * dans un thread en arrière-plan.
     */
    private BoundingBoxBuilder() {
        final Thread thread = new Thread(this, "BoundingBoxBuilder");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Ajoute l'élément spécifié à la liste des objets à construire. Si l'objet spécifié était déjà
     * dans la liste, alors il sera déplacé au début de la liste de façon à le rendre prioritaire.
     * Autrement dit, si cette méthode est appelée plus d'une fois pour le même objet {@code bbox},
     * alors tous les appels redondants sont considérés comme des "piqûres de rappel" indiquant que
     * cet objet {@code bbox} a été demandé plusieurs fois (probablement par l'utilisateur) et
     * devrait donc être traité en priorité.
     */
    public void add(final BoundingBox bbox) {
        synchronized (queue) {
            if (bbox != processing) {
                if (queue.remove(bbox)) {
                    assert !queue.contains(bbox); // Vérifie qu'il n'y a pas de doublons.
                    queue.addFirst(bbox);
                } else {
                    queue.addLast(bbox);
                    queue.notifyAll();
                }
            }
        }
    }

    /**
     * Obtient les séries de données pour tous les objets {@link BoundingBox} qui ont été
     * {@linkplain #add ajoutés à la liste}.
     */
    public void run() {
        while (true) {
            /*
             * Obtient le prochain élément à traiter. Le code suivant bloquera (le thread
             * dormira) jusqu'à ce qu'au moins un élément soit disponible dans la liste.
             */
            synchronized (queue) {
                processing = queue.poll();
                if (processing == null) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        /*
                         * L'attente a été interrompue. Il n'y a pas de raison pour que cela se
                         * produise. Mais si c'est tout de même le cas, ce n'est pas un soucis.
                         * Il suffit de retourner au boulot (vérifier si une nouvelle entrée est
                         * disponible, et continuer comme d'habitude).
                         */
                    }
                    continue;
                }
            }
            /*
             * Obtient maintenant toutes les séries de données qui interceptent la région
             * géographique et la plage de temps spécifiées. En cas d'exception autre que
             * java.lang.Error, on ne propage pas l'exception car il n'y aura personne pour
             * la gérer au dessus de cette méthode, et l'on ne veut pas tuer ce thread.
             */
            Series[] series = null;
            try {
                try {
                    final Set<Series> s;
                    s = Observations.DEFAULT.getSeries(processing.getGeographicBoundingBox(),
                                                       processing.getTimeRange());
                    series = s.toArray(new Series[s.size()]);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            } finally {
                processing.setSeries(series);
            }
        }
    }
}
