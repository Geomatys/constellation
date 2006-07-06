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

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Sicade dependencies
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.coverage.LinearModel;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.resources.seagis.ResourceKeys;
import net.sicade.resources.seagis.Resources;


/**
 * Connexion vers une table pouvant substituer un {@linkplain Descriptor descripteur} par une
 * somme de {@linkplain LinearModel.Term termes}. Cette table est utilis�e notamment pour substituer
 * les gradients temporels. Ces derniers sont des descripteurs virtuels, calcul�s � la vol� par
 * PostgreSQL � partir de la diff�rence entre deux descripteurs "bruts". Cette table permet de
 * {@linkplain LinearModelEntry#substitute substituer} un descripteur virtuel par les descripteurs
 * r�els qui le compose.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(DescriptorTable.class)
@UsedBy(LinearModelTable.class)
public class DescriptorSubstitutionTable extends Table implements Shareable {
    /**
     * Requ�te SQL pour obtenir un descripteur.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("DescriptorSubstitutions:SELECT",
            "SELECT symbol1, symbol2\n"                    +
            "  FROM \"TemporalGradientDescriptors\"\n" +
            " WHERE symbol=?");

    /**
     * La table des descripteurs du paysage oc�anique. Ne sera construit que la premi�re fois
     * o� elle sera n�cessaire.
     */
    private DescriptorTable descriptors;

    /**
     * Construit une nouvelle instance de cette table pour la base de donn�es sp�cifi�e.
     */
    public DescriptorSubstitutionTable(final Database database) {
        super(database);
    }

    /**
     * D�finie la table des descripteurs � utiliser. Cette m�thode peut �tre appel�e par
     * {@link LinearModelTable} imm�diatement apr�s la construction de cette table et avant
     * toute premi�re utilisation. Notez que les instances ainsi cr��es ne devraient pas �tre
     * partag�es par {@link Database#getTable}.
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
            this.descriptors = descriptors;
        }
    }

    /**
     * Retourne les termes de mod�les lin�aire pour le descripteur sp�cifi�, ou {@code null}
     * s'il n'y en a pas. Si cette m�thode retourne un tableau non-nul, alors le descripteur
     * sp�cifi� sera remplac� par la somme de tous les termes retourn�s lors de la construction
     * d'un mod�le lin�aire.
     */
    public synchronized LinearModel.Term[] expand(final Descriptor descriptor)
            throws CatalogException, SQLException
    {
        final PreparedStatement statement = getStatement(SELECT);
        final String key = descriptor.getName();
        statement.setString(1, key);
        final ResultSet results = statement.executeQuery();
        if (!results.next()) {
            results.close();
            return null;
        }
        final String symbol1 = results.getString(1);
        final String symbol2 = results.getString(2);
        if (results.next()) {
            final String table = results.getMetaData().getTableName(1);
            results.close();
            throw new IllegalRecordException(table, Resources.format(
                      ResourceKeys.ERROR_DUPLICATED_RECORD_$1, key));
        }
        results.close();
        if (key.equals(symbol1) || key.equals(symbol2)) {
            throw new IllegalRecordException(null, "D�finition r�cursive d'un gradient temporel.");
        }
        if (descriptors == null) {
            descriptors = database.getTable(DescriptorTable.class);
        }
        final Descriptor d1 = descriptors.getEntry(symbol1);
        final Descriptor d2 = descriptors.getEntry(symbol2);
        final double scale = 1.0 / (d2.getLocationOffset().getDayOffset() -
                                    d1.getLocationOffset().getDayOffset());
        return new LinearModelTerm[] {
            new LinearModelTerm( scale, d2),
            new LinearModelTerm(-scale, d1)
        };
    }
}
