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
package net.sicade.observation.coverage.sql;

// Utilitaires
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Base de donn�es
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
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LinearModel;


/**
 * Connexion vers la table des {@linkplain LinearModel mod�les lin�aires}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@Use({DescriptorTable.class, DescriptorSubstitutionTable.class})
@UsedBy(SeriesTable.class)
public class LinearModelTable extends Table implements Shareable {
    /**
     * La requ�te SQL servant � interroger la table.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("LinearModels:SELECT",
            "SELECT source1, source2, coefficient\n" +
            "  FROM \"LinearModelTerms\"\n"          +
            " WHERE target=?\n" +
            " ORDER BY source1, source2");

    /** Num�ro d'argument. */ private static final int ARGUMENT_TARGET = 1;
    /** Num�ro de colonne. */ private static final int SOURCE1         = 1;
    /** Num�ro de colonne. */ private static final int SOURCE2         = 2;
    /** Num�ro de colonne. */ private static final int COEFFICIENT     = 3;

    /**
     * La table des descripteurs du paysage oc�anique. Ne sera construit que la premi�re fois
     * o� elle sera n�cessaire.
     */
    private DescriptorTable descriptors;

    /**
     * La table de substitution des descripteurs. Ne sera construit que la premi�re fois
     * o� elle sera n�cessaire.
     */
    private DescriptorSubstitutionTable substitution;

    /**
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public LinearModelTable(final Database database) {
        super(database);
    }

    /**
     * D�finie la table des descripteurs � utiliser. Cette m�thode peut �tre appel�e par
     * {@link SeriesTable} imm�diatement apr�s la construction de {@code LinearModelTable}
     * et avant toute premi�re utilisation. Notez que les instances de {@code LinearModelTable}
     * ainsi cr��es ne devraient pas �tre partag�es par {@link Database#getTable}.
     *
     * @param  descriptors Table des descripteurs � utiliser.
     * @throws IllegalStateException si cette instance utilise d�j� une autre table des descripteurs.
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
     * Retourne le mod�le lin�aire pour la s�rie sp�cifi�e. Si cette s�rie n'est pas
     * le r�sultat d'un mod�le lin�aire, alors cette m�thode retourne {@code null}.
     *
     * @param  target La s�rie d'images pour laquelle on veut le mod�le lin�aire.
     * @return Le mod�le lin�aire, ou {@code null} s'il n'y en a pas.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou�.
     */
    public synchronized LinearModel getEntry(final Series target) throws CatalogException, SQLException {
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
     * Retourne les termes de mod�le lin�aire pour la s�rie d'images sp�cifi�e. 
     * Si cette s�rie n'est pas le r�sultat d'un mod�le lin�aire, alors cette
     * m�thode retourne {@code null}.
     *
     * @param  target La s�rie d'images pour laquelle on veut le mod�le lin�aire.
     * @return Les termes du mod�le lin�aire, ou {@code null} s'il n'y en a pas.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou�.
     */
    private List<LinearModel.Term> getTerms(final Series target) throws CatalogException, SQLException {
        ArrayList<LinearModelTerm> terms = null;
        final PreparedStatement statement = getStatement(SELECT);
        statement.setString(ARGUMENT_TARGET, target.getName());
        final ResultSet results;
        try {
            results = statement.executeQuery();
        } catch (SQLException e) {
            /*
             * Il est possible que nous l'utilisateur courant n'aie pas les droits d'acc�s � la
             * table des mod�les lin�aires. Les donn�es de cette table sont parfois consid�r�es
             * sensibles et l'acc�s restreint. Puisque les mod�les lin�aires ne sont pas une
             * information essentielle au fonctionnement des s�ries et que 'null' est une valeur
             * l�gale, on consid�rera que la s�rie demand�e n'a pas de mod�le lin�aire associ�e.
             */
            final LogRecord record = new LogRecord(Level.WARNING,
                    "Le mod�le lin�aire n'est pas accessible pour la s�rie \"" + target.getName() + "\".");
            record.setSourceClassName("LinearModelTable");
            record.setSourceMethodName("getEntry");
            record.setThrown(e);
            LinearModel.LOGGER.log(record);
            return null;
        }
        /*
         * Obtient l'ensemble des termes, mais en ne lisant que les noms des descripteurs. La
         * construction compl�te des descripteurs ne sera effectu�e qu'apr�s la fermeture du
         * 'ResultSet', afin d'�viter des probl�mes lors d'appels recursifs � cette m�thode.
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
         * Apr�s la construction de la liste des termes, compl�te la construction
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
