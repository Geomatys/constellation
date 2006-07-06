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
package net.sicade.observation;

import java.util.logging.Level;


/**
 * Niveaux utilis�s dans le paquet {@code net.sicade.observation} pour la journalisation d'�v�n�ments.
 * Les �v�nements archiv�s peuvent �tre des {@linkplain #SELECT consultations} et surtout des
 * {@linkplain #UPDATE mises � jour}. Du c�t� du serveur, ils correspondent � des instructions
 * SQL {@code SELECT} et {@code UPDATE} (ou {@code INSERT}) respectivement. Du c�t� du client,
 * ils correspondent � des appels � des m�thodes distances via les RMI.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Element#LOGGER
 */
public final class LoggingLevel extends Level {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 7505485471495575298L;

    /**
     * Le niveau pour les instructions SQL {@code SELECT}.
     */
    public static final Level SELECT = new LoggingLevel("SQL SELECT", FINE.intValue()+50);

    /**
     * Le niveau pour les instructions SQL {@code DELETE}.
     */
    public static final Level DELETE = new LoggingLevel("SQL DELETE", INFO.intValue()-50);

    /**
     * Le niveau pour les instructions SQL {@code INSERT}.
     */
    public static final Level INSERT = new LoggingLevel("SQL INSERT", DELETE.intValue());

    /**
     * Le niveau pour les instructions SQL {@code UPDATE}.
     */
    public static final Level UPDATE = new LoggingLevel("SQL UPDATE", INSERT.intValue());

    /**
     * Le niveau pour les instructions SQL {@code CREATE}.
     */
    public static final Level CREATE = new LoggingLevel("SQL CREATE", INFO.intValue()-25);

    /**
     * Construit un nouveau niveau de journalisation.
     *
     * @param name  Le nom du niveau (par exemple {@code "SQL_UPDATE"}.
     * @param value La valeur du niveau.
     */
    private LoggingLevel(final String name, final int value) {
        super(name, value);
    }
}
