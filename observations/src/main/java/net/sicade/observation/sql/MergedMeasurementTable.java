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
package net.sicade.observation.sql;

// J2SE dependencies
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.logging.LogRecord;
import java.io.Writer;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import net.sicade.catalog.Database;
import net.sicade.catalog.LoggingLevel;
import net.sicade.catalog.Query;
import net.sicade.catalog.Table;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.Observable;
import net.sicade.coverage.model.Descriptor;
import net.sicade.coverage.model.RegionOfInterest;

// GeoAPI dependencies
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Process;


/**
 * Juxtapose des observations de différents types sur une même ligne. La première colonne pourrait
 * contenir par exemple des données de {@linkplain net.sicade.observation.fishery pêches}, et les
 * colonnes suivantes les valeurs de différents {@linkplain net.sicade.observation.coverage.Descriptor
 * descripteur du paysage océanique} aux positions de ces données de pêche. Cette classe interroge la
 * table {@code "Environments"} (ou une table équivalente) en la réarangeant d'une façon plus
 * appropriée pour l'analyse avec des logiciels statistiques. Les valeurs du paysage océanique
 * correspondant à un même échantillons (SST 5 jours avant, 10 jours avant, etc.) sont juxtaposées
 * sur une même ligne.
 * <p>
 * Cette interrogation pourrait être faites dans un logiciel de base de données avec une requête
 * SQL classique. Mais cette requête est assez longue et très laborieuse à construire à la main.
 * Cette classe découpera cette requête monstre en une série de requêtes plus petites.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated L'implémentation de cette classe n'est pas terminée. Il manque l'implémentation de
 *             {@link #getResultSet}.
 */
public class MergedMeasurementTable extends Table {
    /**
     * Les descripteurs que l'on voudra inclure dans cette table.
     */
    private final Map<Observable,Boolean> observables = new LinkedHashMap<Observable,Boolean>();

    /**
     * Construit une nouvelle table.
     *
     * @param database Connexion vers la base de données d'observations.
     */
    public MergedMeasurementTable(final Database database) {
        super(new Query(database)); // TODO
    }

    /**
     * Ajoute un observable à faire apparaître comme une colonne. Chaque objet {@code MergedMeasurementTable}
     * nouvellement créé ne contient initialement qu'une seule colonne: le numéro ID des stations. Chaque appel
     * à {@code add} ajoute une colonne. Cette colonne sera prise en compte lors du prochain appel de
     * la méthode {@link #getResultSet}.
     *
     * @param  observable pour la colonne à ajouter.
     */
    public final void add(final Observable observable) {
        add(observable, false);
    }

    /**
     * Ajoute un observable à faire apparaître comme une colonne, en spécifiant si les valeurs
     * nulles sont autorisées.
     *
     * @param  observable pour la colonne à ajouter.
     * @param  nullIncluded Indique si {@link #getResultSet} est autorisé à retourner des valeurs
     *         nulles. La valeur par défaut est {@code false}, ce qui indique que tous les
     *         enregistrements pour lesquels au moins un paramètre environnemental est manquant
     *         seront omis.
     */
    public synchronized void add(final Observable observable, final boolean nullIncluded) {
        observables.put(observable, Boolean.valueOf(nullIncluded));
    }

    /**
     * Retire un observable qui apparaissait comme une des colonnes. Cette méthode
     * permet de retirer une colonne qui aurait été ajoutée précédement par un appel
     * à <code>{@linkplain #add(Observable) add}(observable)</code>. Cette méthode
     * ne fait rien si aucune colonne ne correspond à l'observable spécifié.
     *
     * @param  observable pour la colonne à retirer.
     */
    public synchronized void remove(final Observable observable) {
        observables.remove(observable);
    }

    /**
     * Oublie tous les {@linkplain Observable observables} qui ont été déclarés avec
     * la méthode {@link #add(Observable) add}.
     */
    public synchronized void clear() {
        observables.clear();
    }

    /**
     * Retourne le nombre d'{@linkplain Observable observables} dans cette table correspondant aux
     * critères spécifiés. Si un ou plusieurs des arguments {@code phenomenon}, {@code procedure}
     * ou {@code offset} est non-nul, alors cette méthode filtre les observables en ne comptant que
     * ceux qui correspondent aux arguments non-nuls. Par exemple {@code count(null,null,offset)}
     * comptera tous les observables dont la position spatio-temporelle relative est égale à
     * {@code offset}.
     *
     * @param  phenomenon Phénomène à compter, ou {@code null} pour les compter tous.
     * @param  procedure Si non-nul, alors seul les observables sur lesquelles on applique
     *         cette procédure seront pris en compte.
     * @param  offset Si non-nul, alors seul les observables à cette position relative seront
     *         pris en compte.
     */
    public synchronized int count(final Phenomenon phenomenon,
                                  final Process   procedure,
                                  final RegionOfInterest offset)
    {
        if (phenomenon == null && procedure == null && offset == null) {
            return observables.size();
        }
        int count = 0;
        for (final Observable observable : observables.keySet()) {
            if (phenomenon != null && !phenomenon.equals(observable.getPhenomenon())) {
                continue;
            }
            if (procedure != null && !procedure.equals(observable.getProcess())) {
                continue;
            }
            if (offset != null && !(observable instanceof Descriptor &&
                offset.equals(((Descriptor) observable).getRegionOfInterest())))
            {
                continue;
            }
            count++;
        }
        return count;
    }

    /**
     * Retourne un itérateur qui parcourera l'ensemble des données sélectionnées. La première
     * colonne de l'itérateur {@link ResultSet} contiendra le numéro identifiant les stations (ID).
     * Toutes les colonnes suivantes contiendront les valeurs des {@linkplain Observable observables}
     * qui auront été demandées par des appels de {@link #add(Observable) add(...)}.
     * <p>
     * Note: <strong>Chaque objet {@code MergedMeasurementTable} ne maintient qu'un seul objet
     *       {@code ResultSet} à la fois.</strong>  Si cette méthode est appelée plusieurs
     *       fois, alors chaque nouvel appel fermera le {@link ResultSet} de l'appel précédent.
     *
     * @return Les données environnementales pour les captures.
     *
     * @todo Cette méthode n'est pas encore implémentée. Il faudrait construire ici une requête
     *       qui enchaîne de nombreuses instruction {@code JOIN ON}, une pour chaque observable.
     */
    protected ResultSet getResultSet() throws SQLException {
        throw new UnsupportedOperationException("Pas encore implémenté.");
    }

    /**
     * Affiche les enregistrements vers le flot spécifié.
     * Cette méthode est surtout utile à des fins de vérification.
     *
     * @param  out Flot de sortie.
     * @param  max Nombre maximal d'enregistrements à écrire.
     * @return Nombre d'enregistrement écrits.
     * @throws SQLException si une erreur est survenue lors de l'accès à la base de données.
     * @throws IOException si une erreur est survenue lors de l'écriture.
     */
    public synchronized int print(final Writer out, int max) throws SQLException, IOException {
        final ResultSet       result = getResultSet();
        final ResultSetMetaData meta = result.getMetaData();
        final String   lineSeparator = System.getProperty("line.separator", "\n");
        final int        columnCount = meta.getColumnCount();
        final int[]            width = new int    [columnCount];
        final boolean[]       isDate = new boolean[columnCount];
        for (int i=0; i<columnCount; i++) {
            final String title = meta.getColumnLabel(i+1);
            out.write(title);
            int length = title.length();
            final int type = meta.getColumnType(i+1);
            switch (type) {
                case Types.DATE: // Fall through
                case Types.TIME: // Fall through
                case Types.TIMESTAMP: {
                    isDate[i] = true;
                    width [i] = 8;
                    break;
                }
                default: {
                    width[i] = Math.max(i==0 ? 11 : 7, length);
                    break;
                }
            }
            if (false) {
                // Ajoute le code du type entre parenthèses.
                final String code = String.valueOf(type);
                out.write('(');
                out.write(code);
                out.write(')');
                length += (code.length() + 2);
            }
            out.write(Utilities.spaces(width[i] - length + 1));
        }
        int count = 0;
        out.write(lineSeparator);
        DateFormat dateFormat = null;
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        while (--max>=0 && result.next()) {
            for (int i=0; i<width.length; i++) {
                final String value;
                if (i == 0) {
                    final int x = result.getInt(i+1);
                    value = result.wasNull() ? "" :  String.valueOf(x);
                } else if (!isDate[i]) {
                    final double x = result.getDouble(i+1);
                    value = result.wasNull() ? "" : format.format(x);
                } else {
                    final Date x=result.getDate(i+1);
                    if (!result.wasNull()) {
                        if (dateFormat == null) {
                            dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                        }
                        value = dateFormat.format(x);
                    } else {
                        value = "";
                    }
                }
                out.write(Utilities.spaces(width[i]-value.length()));
                out.write(value);
                out.write(' ');
            }
            out.write(lineSeparator);
            count++;
        }
        result.close();
        out.flush();
        return count;
    }

    /**
     * Copie toutes les données de {@link #getRowSet} vers une table du nom
     * spécifiée. Aucune table ne doit exister sous ce nom avant l'appel de
     * cette méthode. Cette méthode construira elle-même la table nécessaire.
     *
     * @param  connection La connection vers la base de données dans laquelle créer la table,
     *         or {@code null} pour créer une table dans la base de données courante.
     * @param  tableName Nom de la table à créer.
     * @return Le nombre d'enregistrement copiés dans la nouvelle table.
     * @throws SQLException si un accès à la base de données a échouée.
     */
    public synchronized int copyToTable(Connection connection, final String tableName)
            throws SQLException
    {
        if (connection == null) {
            connection = getDatabase().getConnection();
        }
        final Calendar      calendar = getCalendar();
        final ResultSet       source = getResultSet();
        final ResultSetMetaData meta = source.getMetaData();
        final int        columnCount = meta.getColumnCount();
        final boolean[]       isDate = new boolean[columnCount];
        final Statement      creator;
        final ResultSet         dest;
        /*
         * Creates the destination table. The table must not exist prior to this call.
         * All values (except the ID in column 0) are stored as 32 bits floating point.
         * The CREATE statement is logged for information.
         */
        if (true) {
            final StringBuilder buffer = new StringBuilder("CREATE TABLE \"");
            buffer.append(tableName);
            buffer.append("\"(\"");
            for (int i=0; i<columnCount; i++) {
                if (i!=0) {
                    buffer.append(", \"");
                }
                buffer.append(meta.getColumnName(i+1));
                buffer.append("\" ");
                if (i == 0) {
                    // TODO: Ce champ devrait probablement être une clé primaire...
                    buffer.append("INTEGER");
                } else {
                    switch (meta.getColumnType(i+1)) {
                        case Types.DATE: // Fall through
                        case Types.TIME: // Fall through
                        case Types.TIMESTAMP: {
                            // TODO: On aimerait déclarer que ce champ doit être indexé (avec doublons)...
                            isDate[i] = true;
                            buffer.append("TIMESTAMP");
                            break;
                        }
                        case Types.TINYINT:     // Fall through (not strictly true, but hey,
                        case Types.SMALLINT: {  // we are fighthing against MS-Access!!
                            // We should really uses a boolean type, but Access
                            // replace 'True' by '-1' while we really wanted '1'.
                            buffer.append("SMALLINT");
                            break;
                        }
                        default: {
                            buffer.append("REAL");
                            break;
                        }
                    }
                }
                if (meta.isNullable(i+1) == ResultSetMetaData.columnNoNulls) {
                    buffer.append(" NOT NULL");
                }
            }
            buffer.append(')');
            final String sqlCreate = buffer.toString();
            creator = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                 ResultSet.CONCUR_UPDATABLE);
            creator.execute(sqlCreate);
            buffer.setLength(0);
            buffer.append("SELECT * FROM \"");
            buffer.append(tableName);
            buffer.append('"');
            dest = creator.executeQuery(buffer.toString());
            if (true) {
                // Log the SQL statement.
                final LogRecord record = new LogRecord(LoggingLevel.CREATE, sqlCreate);
                record.setSourceClassName ("MergedMeasurementTable");
                record.setSourceMethodName("copyToTable");
                Observable.LOGGER.log(record);
            }
        }
        /*
         * Copies all values to the destination table.
         */
        int count = 0;
        while (source.next()) {
            final int ID = source.getInt(1);
            dest.moveToInsertRow();
            dest.updateInt(1, ID);
            for (int i=2; i<=columnCount; i++) {
                if (isDate[i-1]) {
                    dest.updateTimestamp(i, source.getTimestamp(i, calendar));
                } else {
                    final float x = source.getFloat(i);
                    if (!source.wasNull()) {
                        dest.updateFloat(i, x);
                    } else {
                        dest.updateNull(i);
                    }
                }
            }
            dest.insertRow();
            count++;
        }
        dest.close();
        source.close();
        creator.close();
        return count;
    }
}
