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
package net.sicade.observation.coverage.sql;

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
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Layer;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LinearModel;


/**
 * Connexion vers la table des {@linkplain LinearModel modèles linéaires}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@Use({DescriptorTable.class, DescriptorSubstitutionTable.class})
@UsedBy(LayerTable.class)
public class LinearModelTable extends Table implements Shareable {
    /**
     * La requête SQL servant à interroger la table.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("LinearModels:SELECT",
            "SELECT source1, source2, coefficient\n" +
            "  FROM \"LinearModelTerms\"\n"          +
            " WHERE target=?\n" +
            " ORDER BY source1, source2");

    /** Numéro d'argument. */ private static final int ARGUMENT_TARGET = 1;
    /** Numéro de colonne. */ private static final int SOURCE1         = 1;
    /** Numéro de colonne. */ private static final int SOURCE2         = 2;
    /** Numéro de colonne. */ private static final int COEFFICIENT     = 3;

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
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public LinearModelTable(final Database database) {
        super(database);
    }

    /**
     * Définie la table des descripteurs à utiliser. Cette méthode peut être appelée par
     * {@link LayerTable} immédiatement après la construction de {@code LinearModelTable}
     * et avant toute première utilisation. Notez que les instances de {@code LinearModelTable}
     * ainsi créées ne devraient pas être partagées par {@link Database#getTable}.
     *
     * @param  descriptors Table des descripteurs à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des descripteurs.
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
     * Retourne le modèle linéaire pour la couche spécifiée. Si cette couche n'est pas
     * le résultat d'un modèle linéaire, alors cette méthode retourne {@code null}.
     *
     * @param  target La couche d'images pour laquelle on veut le modèle linéaire.
     * @return Le modèle linéaire, ou {@code null} s'il n'y en a pas.
     * @throws SQLException si l'interrogation de la base de données a échoué.
     */
    public synchronized LinearModel getEntry(final Layer target) throws CatalogException, SQLException {
        final List<LinearModel.Term> terms = getTerms(target);
        if (terms == null) {
            return null;
        }
        LinearModelEntry model = new LinearModelEntry(target, terms);
        if (substitution == null) {
            substitution = database.createTable(DescriptorSubstitutionTable.class);
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
        ArrayList<LinearModelTerm> terms = null;
        final PreparedStatement statement = getStatement(SELECT);
        statement.setString(ARGUMENT_TARGET, target.getName());
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
        while (results.next()) {
            final String source1     = results.getString(SOURCE1);
            final String source2     = results.getString(SOURCE2);
            final double coefficient = results.getDouble(COEFFICIENT);
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
                setDescriptorTable(database.getTable(DescriptorTable.class));
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
