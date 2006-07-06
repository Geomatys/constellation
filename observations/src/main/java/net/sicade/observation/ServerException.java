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

import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Exception survenue du c�t� du serveur lors d'une requ�te sur une base de donn�es.
 * La {@linkplain #getCause cause} d�pend de l'impl�mentation de la classe qui a lanc�e l'exception,
 * mais sera typiquement de type {@link java.sql.SQLException} ou {@link java.rmi.RemoteException}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ServerException extends CatalogException {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -6413382346019648505L;

    /** 
     * Construit une exception avec la cause sp�cifi�e.
     * Le message sera d�termin�e � partir de la cause.
     */
    public ServerException(final Exception exception) {
        super(exception, null);
    }

    /** 
     * Construit une exception avec la cause sp�cifi�e.
     */
    public ServerException(final Exception exception, final String table) {
        super(exception, table);
    }

    /**
     * Retourne une cha�ne de caract�re qui contiendra le
     * nom de la table et un message d�crivant l'erreur.
     */
    @Override
    public String getLocalizedMessage() {
        final String message = super.getLocalizedMessage();
        final String table   = getTable();
        if (table == null) {
            return message;
        }
        return Resources.format(ResourceKeys.TABLE_ERROR_$2, table, message);
    }
}
