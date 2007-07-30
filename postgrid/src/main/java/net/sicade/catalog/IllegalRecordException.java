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
package net.sicade.catalog;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


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
     * Creates an exception with no cause and no details message.
     */
    public IllegalRecordException() {
        super();
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public IllegalRecordException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified cause and no details message.
     *
     * @param cause The cause for this exception.
     */
    public IllegalRecordException(final Exception cause) {
        super(cause);
    }

    /**
     * Creates an exception from the specified result set. The table and column names are
     * obtained from the {@code results} argument if non-null. <strong>Note that the result
     * set will be closed</strong>, because this exception is always thrown when an error
     * occured while reading this result set.
     *
     * @param message The details message.
     * @param results The result set in which a problem occured, or {@code null} if none.
     * @param column  The column index where a problem occured (number starts at 1), or {@code 0} if unknow.
     * @param key     The key value for the record where a problem occured, or {@code null} if none.
     * @throws SQLException if the metadata can't be read from the result set.
     */
    public IllegalRecordException(final String message, final ResultSet results, final int column, final String key)
            throws SQLException
    {
        super(message);
        setMetadata(results, column, key);
    }

    /**
     * Creates an exception from the specified result set. The table and column names are
     * obtained from the {@code results} argument if non-null. <strong>Note that the result
     * set will be closed</strong>, because this exception is always thrown when an error
     * occured while reading this result set.
     *
     * @param cause   The cause for this exception.
     * @param results The result set in which a problem occured, or {@code null} if none.
     * @param column  The column index where a problem occured (number starts at 1), or {@code 0} if unknow.
     * @param key     The key value for the record where a problem occured, or {@code null} if none.
     * @throws SQLException if the metadata can't be read from the result set.
     */
    public IllegalRecordException(final Exception cause, final ResultSet results, final int column, final String key)
            throws SQLException
    {
        super(cause);
        setMetadata(results, column, key);
    }
}
