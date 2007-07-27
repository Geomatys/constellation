/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.sicade.coverage.catalog.sql;

// Utilitaires
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Base de données
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Sicade
import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.Layer;
import net.sicade.coverage.catalog.Descriptor;
import net.sicade.coverage.catalog.LinearModel;
import net.sicade.sql.Table;
import net.sicade.sql.Database;
import net.sicade.sql.Column;
import net.sicade.sql.Parameter;
import net.sicade.sql.QueryType;
import static net.sicade.sql.QueryType.*;


/**
 * Connection to the table of {@linkplain LinearModel linear model}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class LinearModelTable extends Table {
    /**
     * La table des descripteurs du paysage océanique. Ne sera construit que la première fois
     * où elle sera nécessaire.
     */
    private DescriptorTable descriptors;

    /**
     * La table de substitution des descripteurs. Ne sera construit que la première fois
     * où elle sera nécessaire.
     */
    private DescriptorSubstitutionTable substitution;

    /**
     * Creates a linear model table.
     * 
     * @param database Connection to the database.
     */
    public LinearModelTable(final Database database) {
        super(new LinearModelQuery(database));
    }

    /**
     * Creates a new table using the same connection than the specified table.
     * This is useful when we want to change the configuration of the new table
     * while preserving the original table from changes.
     *
     * @param table The table to clone.
     *
     * @see #setDescriptorTable
     */
    protected LinearModelTable(final LinearModelTable table) {
        super(table);
    }

    /**
     * Sets the descriptor table to use. This method is invoked by {@link LayerTable}
     * immediately after the creation of this {@code LinearModelTable}. Note that
     * the instance given to this method should not be cached by {@link Database#getTable}.
     *
     * @param  descriptors The descriptor table to use.
     * @throws IllegalStateException if this table is already associated to an other descriptor table.
     */
    protected synchronized void setDescriptorTable(final DescriptorTable descriptors)
            throws IllegalStateException
    {
        if (this.descriptors != descriptors) {
            if (this.descriptors != null) {
                throw new IllegalStateException();
            }
            if (substitution != null) {
                substitution.setDescriptorTable(descriptors);
            }
            this.descriptors = descriptors;
        }
    }

    /**
     * Returns the linear model for the given layer, or {@code null} if none.
     *
     * @param  target The layer to query for a linear model.
     * @return The linear model, or {@code null} if none.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized LinearModel getEntry(final Layer target) throws CatalogException, SQLException {
        final List<LinearModel.Term> terms = getTerms(target);
        if (terms == null) {
            return null;
        }
        LinearModelEntry model = new LinearModelEntry(target, terms);
        if (substitution == null) {
            // Note: we wraps in new DescriptorSubstitutionTable(...)
            // in order to protect the shared instance from changes.
            substitution = new DescriptorSubstitutionTable(getDatabase().getTable(DescriptorSubstitutionTable.class));
            substitution.setDescriptorTable(descriptors);
        }
        for (final Descriptor descriptor : model.getDescriptors()) {
            final LinearModel.Term[] expansion = substitution.expand(descriptor);
            if (expansion != null) {
                model.substitute(descriptor, expansion);
            }
        }
        return model;
    }

    /**
     * Retourne les termes de modèle linéaire pour la couche d'images spécifiée. 
     * Si cette couche n'est pas le résultat d'un modèle linéaire, alors cette
     * méthode retourne {@code null}.
     *
     * @param  target La couche d'images pour laquelle on veut le modèle linéaire.
     * @return Les termes du modèle linéaire, ou {@code null} s'il n'y en a pas.
     * @throws SQLException si l'interrogation de la base de données a échoué.
     */
    private List<LinearModel.Term> getTerms(final Layer target) throws CatalogException, SQLException {
        final LinearModelQuery query = (LinearModelQuery) super.query;
        ArrayList<LinearModelTerm> terms = null;
        final PreparedStatement statement = getStatement(SELECT);
        statement.setString(indexOf(query.byTarget), target.getName());
        final ResultSet results;
        try {
            results = statement.executeQuery();
        } catch (SQLException e) {
            /*
             * Il est possible que nous l'utilisateur courant n'aie pas les droits d'accès à la
             * table des modèles linéaires. Les données de cette table sont parfois considérées
             * sensibles et l'accès restreint. Puisque les modèles linéaires ne sont pas une
             * information essentielle au fonctionnement des couches et que 'null' est une valeur
             * légale, on considèrera que la couche demandée n'a pas de modèle linéaire associée.
             */
            final LogRecord record = new LogRecord(Level.WARNING,
                    "Le modèle linéaire n'est pas accessible pour la couche \"" + target.getName() + "\".");
            record.setSourceClassName("LinearModelTable");
            record.setSourceMethodName("getEntry");
            record.setThrown(e);
            LinearModel.LOGGER.log(record);
            return null;
        }
        /*
         * Obtient l'ensemble des termes, mais en ne lisant que les noms des descripteurs. La
         * construction complète des descripteurs ne sera effectuée qu'après la fermeture du
         * 'ResultSet', afin d'éviter des problèmes lors d'appels recursifs à cette méthode.
         */
        final int s1 = indexOf(query.source1);
        final int s2 = indexOf(query.source2);
        final int cf = indexOf(query.coefficient);
        while (results.next()) {
            final String source1     = results.getString(s1);
            final String source2     = results.getString(s2);
            final double coefficient = results.getDouble(cf);
            if (terms == null) {
                terms = new ArrayList<LinearModelTerm>();
            }
            terms.add(new LinearModelTerm(coefficient, new String[] {source1, source2}));
        }
        results.close();
        /*
         * Après la construction de la liste des termes, complète la construction
         * de tous les descripteurs.
         */
        if (terms == null) {
            return null;
        }
        for (final LinearModelTerm t : terms) {
            final String[] names = t.getDescriptorNames();
            final String source1 = names[0];
            final String source2 = names[1];
            if (descriptors == null) {
                setDescriptorTable(getDatabase().getTable(DescriptorTable.class));
            }
            final Descriptor descriptor1 = descriptors.getEntry(source1);
            final Descriptor descriptor2 = descriptors.getEntry(source2);
            final Descriptor[] term;
            if (descriptor2.isIdentity()) {
                term = new Descriptor[] {descriptor1};
            } else if (descriptor1.isIdentity()) {
                term = new Descriptor[] {descriptor2};
            } else {
                term = new Descriptor[] {descriptor1, descriptor2};
            }
            t.setDescriptors(term);
        }
        return Arrays.asList(terms.toArray(new LinearModel.Term[terms.size()]));
    }
}
