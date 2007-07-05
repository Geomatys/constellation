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
package net.sicade.observation;

import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Exception survenue du côté du serveur lors d'une requête sur une base de données.
 * La {@linkplain #getCause cause} dépend de l'implémentation de la classe qui a lancée l'exception,
 * mais sera typiquement de type {@link java.sql.SQLException} ou {@link java.rmi.RemoteException}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ServerException extends CatalogException {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -6413382346019648505L;

    /** 
     * Construit une exception avec la cause spécifiée.
     * Le message sera déterminée à partir de la cause.
     */
    public ServerException(final Exception exception) {
        super(exception, null);
    }

    /** 
     * Construit une exception avec la cause spécifiée.
     */
    public ServerException(final Exception exception, final String table) {
        super(exception, table);
    }

    /**
     * Retourne une chaîne de caractère qui contiendra le
     * nom de la table et un message décrivant l'erreur.
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
