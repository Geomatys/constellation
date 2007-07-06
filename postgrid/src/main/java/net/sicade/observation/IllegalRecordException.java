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
package net.sicade.observation;

import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Indique qu'une incohérence a été détectée dans un enregistrement d'une table de la base
 * de données. Cette exception peut être levée par exemple si une valeur négative a été trouvée
 * dans un champ qui ne devrait contenir que des valeurs positives, ou si une clé étrangère n'a
 * pas été trouvée. Dans plusieurs cas, cette exception ne devrait pas être soulévée si la base
 * de données à bien vérifié toutes les contraintes (par exemple les clés étrangères).
 * <p>
 * Cette exception contient le nom de la table contenant un enregistrement invalide.
 * Ce nom apparaît dans le message formaté par {@link #getLocalizedMessage}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IllegalRecordException extends CatalogException {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -8491590864510381052L;
    
    /**
     * Nom de la table qui contient l'enregistrement invalide, ou {@code null} si inconnu.
     */
    private final String table;

    /**
     * Construit une exception signalant qu'un enregistrement n'est pas valide.
     *
     * @param table Nom de la table qui contient l'enregistrement invalide, ou {@code null} si inconnu.
     * @param message Message décrivant l'erreur, ou {@code null} si aucun.
     */
    public IllegalRecordException(final String table, final String message) {
        super(message);
        this.table = table;
    }

    /**
     * Construit une exception signalant qu'un enregistrement n'est pas valide.
     *
     * @param table Nom de la table qui contient l'enregistrement invalide ou {@code null} si inconnu.
     * @param exception Exception rencontrée lors de l'analyse de l'enregistrement.
     */
    public IllegalRecordException(final String table, final Exception exception) {
        this(table, exception.getLocalizedMessage());
        initCause(exception);
    }

    /**
     * Retourne le nom de la table qui contient un enregistrement invalide.
     * Peut retourner {@code null} si le nom de la table n'est pas connu.
     */
    public String getTable() {
        return table;
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
