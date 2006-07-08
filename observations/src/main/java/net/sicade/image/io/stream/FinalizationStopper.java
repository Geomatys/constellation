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
package net.sicade.image.io.stream;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Stop l'ex�cution de {@link System#runFinalization} lorsqu'un d�lai a �t� d�pass�. Cette m�thode
 * est utilis�e pour �viter les bloquages que l'on observe parfois.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class FinalizationStopper extends TimerTask {
    /**
     * Le timer pour arr�ter les ex�cution de {@link System#runFinalization}.
     */
    private static final Timer TIMER = new Timer("Finalization stopper", true);

    /**
     * Le processus � arr�ter.
     */
    private final Thread toStop;

    /**
     * Construit une nouvelle t�che qui arr�tera le processus courant.
     */
    FinalizationStopper() {
        this.toStop = Thread.currentThread();
        TIMER.schedule(this, 4000);
    }

    /**
     * Interrompt 
     */
    public void run() {
        toStop.interrupt();
    }
}
