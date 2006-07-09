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
 * Compl�te la construction des objets {@link BoundingBox} en obtenant leurs s�ries de donn�es.
 * Cette �tape est ex�cut�e en arri�re plan, de sorte que les d�lais de connexion � la base de
 * donn�es ne bloquent pas l'usage de l'application.
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
     * Liste des objets {@link BoundingBox} en attente d'avoir leurs s�ries de donn�es.
     */
    private final LinkedList<BoundingBox> queue = new LinkedList<BoundingBox>();

    /**
     * L'objet en cours de traitement. Cet objet a �t� retir� de la liste {@link #queue},
     * mais sa r�f�rence est conserv�e jusqu'� la fin du traitement afin d'�viter que
     * {@link #add} ne l'ajoute encore inutilement.
     */
    private transient BoundingBox processing;

    /**
     * Construit une instance de {@code BoundingBoxBuilder} qui sera imm�diatement d�marr�
     * dans un thread en arri�re-plan.
     */
    private BoundingBoxBuilder() {
        final Thread thread = new Thread(this, "BoundingBoxBuilder");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Ajoute l'�l�ment sp�cifi� � la liste des objets � construire. Si l'objet sp�cifi� �tait d�j�
     * dans la liste, alors il sera d�plac� au d�but de la liste de fa�on � le rendre prioritaire.
     * Autrement dit, si cette m�thode est appel�e plus d'une fois pour le m�me objet {@code bbox},
     * alors tous les appels redondants sont consid�r�s comme des "piq�res de rappel" indiquant que
     * cet objet {@code bbox} a �t� demand� plusieurs fois (probablement par l'utilisateur) et
     * devrait donc �tre trait� en priorit�.
     */
    public void add(final BoundingBox bbox) {
        synchronized (queue) {
            if (bbox != processing) {
                if (queue.remove(bbox)) {
                    assert !queue.contains(bbox); // V�rifie qu'il n'y a pas de doublons.
                    queue.addFirst(bbox);
                } else {
                    queue.addLast(bbox);
                    queue.notifyAll();
                }
            }
        }
    }

    /**
     * Obtient les s�ries de donn�es pour tous les objets {@link BoundingBox} qui ont �t�
     * {@linkplain #add ajout�s � la liste}.
     */
    public void run() {
        while (true) {
            /*
             * Obtient le prochain �l�ment � traiter. Le code suivant bloquera (le thread
             * dormira) jusqu'� ce qu'au moins un �l�ment soit disponible dans la liste.
             */
            synchronized (queue) {
                processing = queue.poll();
                if (processing == null) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        /*
                         * L'attente a �t� interrompue. Il n'y a pas de raison pour que cela se
                         * produise. Mais si c'est tout de m�me le cas, ce n'est pas un soucis.
                         * Il suffit de retourner au boulot (v�rifier si une nouvelle entr�e est
                         * disponible, et continuer comme d'habitude).
                         */
                    }
                    continue;
                }
            }
            /*
             * Obtient maintenant toutes les s�ries de donn�es qui interceptent la r�gion
             * g�ographique et la plage de temps sp�cifi�es. En cas d'exception autre que
             * java.lang.Error, on ne propage pas l'exception car il n'y aura personne pour
             * la g�rer au dessus de cette m�thode, et l'on ne veut pas tuer ce thread.
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
