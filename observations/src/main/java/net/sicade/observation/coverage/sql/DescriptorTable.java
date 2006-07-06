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

// Geotools dependencies
import org.geotools.resources.CharUtilities;

// Sicade dependencies
import net.sicade.observation.Distribution;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.NoSuchRecordException;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.LocationOffset;
import net.sicade.observation.sql.DistributionTable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.NumericAccess;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Use;


/**
 * Connexion vers la table des {@linkplain Descriptor descripteurs}. Les informations n�cessaires �
 * la construction des descripteurs sont puis�es principalement dans trois tables: {@link SeriesTable},
 * {@link LocationOffsetTable} et {@link OperationTable}. De ces trois tables, la table des s�ries
 * est particuli�re du fait qu'elle n'est pas sens�e �tre {@linkplain Shareable partageable}. Cela
 * n'emp�che toutefois pas {@code DescriptorTable} de l'�tre, puisqu'il utilise par d�faut une table
 * des s�ries globales dont il ne modifiera pas la configuration.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@Use({SeriesTable.class, OperationTable.class, LocationOffsetTable.class, DistributionTable.class})
@UsedBy({LinearModelTable.class, DescriptorSubstitutionTable.class})
public class DescriptorTable extends SingletonTable<Descriptor> implements NumericAccess, Shareable {
    /**
     * Requ�te SQL pour obtenir un descripteur du paysage oc�anique.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Descriptors:SELECT",
            "SELECT symbol, identifier, phenomenon, procedure, \"offset\", band, distribution\n" +
            "  FROM \"Descriptors\"\n"                                                           +
            " WHERE symbol=?\n"                                                                  +
            " ORDER BY identifier");
    
    /** Num�ro de colonne. */ private static final int SYMBOL       = 1;
    /** Num�ro de colonne. */ private static final int IDENTIFIER   = 2;
    /** Num�ro de colonne. */ private static final int PHENOMENON   = 3;
    /** Num�ro de colonne. */ private static final int PROCEDURE    = 4;
    /** Num�ro de colonne. */ private static final int OFFSET       = 5;
    /** Num�ro de colonne. */ private static final int BAND         = 6;
    /** Num�ro de colonne. */ private static final int DISTRIBUTION = 7;

    /**
     * La table des s�ries. Elle sera construite la premi�re fois o� elle sera n�cessaire.
     */
    private SeriesTable series;
    
    /**
     * La table des op�rations. Ne sera construite que la premi�re fois o� elle sera n�cessaire.
     */
    private OperationTable operations;
    
    /**
     * La table des positions relatives.
     * Ne sera construite que la premi�re fois o� elle sera n�cessaire.
     */
    private LocationOffsetTable offsets;
    
    /**
     * La table des distributions.
     * Ne sera construite que la premi�re fois o� elle sera n�cessaire.
     */
    private DistributionTable distributions;
    
    /**
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public DescriptorTable(final Database database) {
        super(database);
    }

    /**
     * D�finie la table des s�ries � utiliser. Cette m�thode peut �tre appel�e par {@link SeriesTable}
     * imm�diatement apr�s la construction de {@code DescriptorTable} et avant toute premi�re utilisation.
     * Notez que les instances de {@code DescriptorTable} ainsi cr��es ne seront pas partag�es par
     * {@link Database#getTable}.
     *
     * @param  series Table des s�ries � utiliser.
     * @throws IllegalStateException si cette instance utilise d�j� une autre table des s�ries.
     */
    protected synchronized void setSeriesTable(final SeriesTable series)
            throws IllegalStateException
    {
        if (this.series != series) {
            if (this.series != null) {
                throw new IllegalStateException();
            }
            this.series = series;
        }
    }

    /**
     * Retourne une entr�e pour le nom sp�cifi�. Cette m�thode est tol�rante au nom: si ce dernier
     * est purement num�rique, alors {@link #getEntry(int)} est appel�e. Sinon, les chiffres qui
     * apparaissent � la fin du nom peuvent �tre remplac�s par les caract�res unicodes repr�sentant
     * ces m�mes chiffres sous forme d'indices.
     */
    public Descriptor getEntryLenient(final String name) throws CatalogException, SQLException {
        try {
            return getEntry(name);
        } catch (final NoSuchRecordException exception) {
            /*
             * Aucune entr�e n'a �t� trouv�e pour le nom. Essaie comme identifiant num�rique.
             * Si l'identifiant est purement num�rique mais la recherche �choue pour ce dernier
             * aussi, on ne fera pas d'autres tentatives.
             */
            int identifier = 0;
            try {
                identifier = Integer.parseInt(name);
            } catch (NumberFormatException dummy) {
                /*
                 * L'identifiant n'est pas num�rique. Essaie de remplacer les derniers chiffres
                 * par les caract�res unicodes correspondant � ces m�me chiffres en indices.
                 */
                final StringBuilder builder = new StringBuilder(name);
                for (int i=builder.length(); --i>=0;) {
                    final char c = builder.charAt(i);
                    final char n = CharUtilities.toSubScript(c);
                    if (c == n) {
                        break;
                    }
                    builder.setCharAt(i, n);
                }
                String modified = builder.toString();
                if (!modified.equals(name)) try {
                    return getEntry(modified);
                } catch (NoSuchRecordException ignore) {
                    throw exception;
                }
            }
            return getEntry(identifier);
        }
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les descripteurs.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un descripteur pour l'enregistrement courant.
     */
    protected Descriptor createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String    symbol      = results.getString (SYMBOL);
        final int       identifier  = results.getInt    (IDENTIFIER);
        final String    phenomenon  = results.getString (PHENOMENON);
        final String    procedure   = results.getString (PROCEDURE);
        final String    position    = results.getString (OFFSET);
        final short     band =(short)(results.getShort  (BAND) - 1);
        final String    distrib     = results.getString (DISTRIBUTION);
        if (offsets == null) {
            offsets = database.getTable(LocationOffsetTable.class);
        }
        final LocationOffset offset = offsets.getEntry(position);
        if (series == null) {
            setSeriesTable(database.getTable(SeriesTable.class));
        }
        final Series series = this.series.getEntry(phenomenon);
        if (operations == null) {
            operations = database.getTable(OperationTable.class);
        }
        final Operation operation = operations.getEntry(procedure);
        if (distributions == null) {
            distributions = database.getTable(DistributionTable.class);
        }
        final Distribution distribution = distributions.getEntry(distrib);
        return new DescriptorEntry(identifier, symbol, series, operation, band, offset, distribution, null);
    }
}
