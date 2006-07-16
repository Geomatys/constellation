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
package net.sicade.observation.sql;

// J2SE dependencies
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.Station;
import net.sicade.observation.Observable;
import net.sicade.observation.Measurement;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;


/**
 * Connexion vers la table des {@linkplain Measurement mesures}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @see MergedMeasurementTable
 * @see net.sicade.observation.coverage.MeasurementTableFiller
 */
public class MeasurementTable extends ObservationTable<Measurement> {
    /**
     * Requ�te SQL pour obtenir les mesures pour une station et un observable donn�s.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Measurements:SELECT",
            "SELECT station, observable, value, error\n"  +
            "  FROM \"Measurements\"\n"                   +
            " WHERE (station = ?) AND (observable = ?)");

    /**
     * Requ�te SQL pour ins�rer les mesures pour une station et un observable donn�s.
     */
    private static final ConfigurationKey INSERT = new ConfigurationKey("Measurements:INSERT",
            "INSERT INTO \"Measurements\" (station, observable, value, error)\n"  +
            "VALUES (?, ?, ?, ?)");

    /** Num�ro de colonne. */ private static final int VALUE = 3;
    /** Num�ro de colonne. */ private static final int ERROR = 4;

    /**
     * La cl� d�signant la requ�te � utiliser pour ajouter des valeurs.
     */
    private final ConfigurationKey insert;

    /**
     * Construit une nouvelle connexion vers la table des mesures.
     */
    public MeasurementTable(final Database database) {
        this(database, SELECT, INSERT);
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures.
     * 
     * @param  database Connexion vers la base de donn�es des observations.
     * @param  select   Cl� de la requ�te SQL � utiliser pour obtenir des valeurs.
     * @param  insert   Cl� de la requ�te SQL � utiliser pour ajouter des valeurs,
     *                  ou {@code null} si les insertions ne sont pas support�es.
     */
    protected MeasurementTable(final Database       database,
                               final ConfigurationKey select,
                               final ConfigurationKey insert)
    {
        super(database, select);
        this.insert = insert;
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures pour les stations sp�cifi�es.
     * 
     * @param  stations La table des stations � utiliser.
     * @param  select   Cl� de la requ�te SQL � utiliser pour obtenir des valeurs.
     * @param  insert   Cl� de la requ�te SQL � utiliser pour ajouter des valeurs,
     *                  ou {@code null} si les insertions ne sont pas support�es.
     */
    protected MeasurementTable(final StationTable   stations,
                               final ConfigurationKey select,
                               final ConfigurationKey insert)
    {
        super(stations, select);
        this.insert = insert;
    }

    /**
     * Construit une mesure pour l'enregistrement couran
     */
    @Override
    protected Measurement createEntry(final Station    station,
                                      final Observable observable,
                                      final ResultSet  result) throws SQLException
    {
        float value = result.getFloat(VALUE); if (result.wasNull()) value=Float.NaN;
        float error = result.getFloat(ERROR); if (result.wasNull()) error=Float.NaN;
        return new MeasurementEntry(station, observable, value, error);
    }

    /**
     * D�finie une valeur r�elle pour la station et l'observable courant.
     *
     * @param  value Valeur � inscrire dans la base de donn�es.
     * @param  error Une estimation de l'erreur, ou {@link Float#NaN} s'il n'y en a pas.
     * @throws CatalogException si la station ou le descripteur sp�cifi� n'existe pas.
     * @throws SQLException si la mise � jour de la base de donn�es a �chou� pour une autre raison.
     */
    public synchronized void setValue(final float value, final float error) throws CatalogException, SQLException {
        if (insert == null) {
            throw new CatalogException("La table \"" + Utilities.getShortClassName(this) +
                                       "\" n'est pas modifiable.");
        }
        final Station station = getStation();
        if (station == null) {
            throw new CatalogException("La station doit �tre d�finie.");
        }
        final Observable observable = getObservable();
        if (observable == null) {
            throw new CatalogException("L'observable doit �tre d�fini.");
        }
        if (Float.isNaN(value)) {
            return;
        }
        final PreparedStatement statement = getStatement(insert);
        statement.setInt  (STATION,    station   .getNumericIdentifier());
        statement.setInt  (OBSERVABLE, observable.getNumericIdentifier());
        statement.setFloat(VALUE, value);
        if (!Float.isNaN(error)) {
            statement.setFloat(ERROR, error);
        } else {
            statement.setNull(ERROR, Types.FLOAT);
        }
        final int count = statement.executeUpdate();
        if (count != 1) {
            Measurement.LOGGER.warning(count + " valeurs ajout�es pour \"" + observable + "\" � la station " + station);
        }
    }
}
