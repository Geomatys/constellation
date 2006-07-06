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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.resources.Arguments;

// Sicade dependencies
import net.sicade.observation.Element;
import net.sicade.observation.LoggingLevel;
import net.sicade.observation.Observations;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;


/**
 * Copie le contenu de tables vers des tables �quivalentes d'une autre base de donn�es. Cette classe
 * est utilis�e lorsque l'on a deux copies d'une base de donn�es (typiquement une copie exp�rimentale
 * et une copie op�rationnelle), et que l'on souhaite copier de temps � autre le contenu de la table
 * exp�rimentale vers sa contrepartie op�rationnelle.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Synchronizer {
    /**
     * Connections vers les bases de donn�es source. Cette connexion ne doit pas �tre ferm�e,
     * car elle restera utilis�e par les m�thodes de {@link Observations}.
     */
    private final Connection source;
    
    /**
     * Connections vers les bases de donn�es destination.
     */
    private final Connection target;

    /**
     * Construit un objet qui synchronisera le contenu de la base de donn�es sp�cifi�e.
     * La base de donn�es source sera la base de donn�es par d�faut, telle que configur�e
     * sur le poste du client.
     *
     * @param  target L'URL vers la base de donn�es destination.
     * @throws CatalogException si une connexion n'a pas pu �tre �tablie.
     */
    public Synchronizer(final String target, final String user, final String password) throws CatalogException {
        this(Observations.getDefault().getDatabase(), target, user, password);
    }

    /**
     * Construit un objet qui synchronisera le contenu de la base de donn�es sp�cifi�e.
     *
     * @param  source La base de donn�es source.
     * @param  target L'URL vers la base de donn�es destination.
     * @throws CatalogException si une connexion n'a pas pu �tre �tablie.
     */
    public Synchronizer(final Database source, final String target, final String user, final String password)
            throws CatalogException
    {
        try {
            this.source = source.getConnection();
            this.target = DriverManager.getConnection(target, user, password);
            this.target.setAutoCommit(false);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Remplace le contenu de la table sp�cifi�e dans la base de donn�es destination.
     * Si des enregistrements existaient d�j� dans la table destination, ils seront
     * supprim�es avant la copie. Si {@code condition} est non-nul, alors seuls les
     * enregistrements r�pondant � cette condition seront affect�es.
     *
     * @param table Le nom de la table dont on veut remplacer les enregistrements.
     * @param condition Une condition SQL d�signant les enregistrements � remplacer,
     *        ou {@code null} pour remplacer tous les enregistrements.
     */
    public void replace(String table, String condition) throws SQLException {
        final Statement sourceStmt = source.createStatement();
        final Statement targetStmt = target.createStatement();
        boolean success = false;
        try {
            final StringBuilder  b = new StringBuilder();
            /*
             * Supprime les anciens enregistrements de la table destination.
             */
            b.append("DELETE FROM \"");
            b.append(table = table.trim());
            b.append('"');
            if (condition != null && (condition=condition.trim()).length() != 0) {
                b.append(" WHERE ");
                b.append(condition);
            }
            String sql = b.toString();
            int count = targetStmt.executeUpdate(sql);
            log(LoggingLevel.DELETE, "replace", sql + '\n' + count + " lignes supprim�es.");
            /*
             * Obtient les nouveaux enregistrements de la table source,
             * ainsi que les noms de toutes les colonnes impliqu�es.
             */
            b.setLength(0);
            b.append("SELECT * FROM \"");
            b.append(table = table.trim());
            b.append('"');
            if (condition != null && condition.length() != 0) {
                b.append(" WHERE ");
                b.append(condition);
            }
            sql = b.toString();
            final ResultSet          sources = sourceStmt.executeQuery(sql);
            final ResultSetMetaData metadata = sources.getMetaData();
            final String[]           columns = new String[metadata.getColumnCount()];
            for (int i=0; i<columns.length;) {
                columns[i] = metadata.getColumnName(++i);
            }
            log(LoggingLevel.SELECT, "replace", sql);
            /*
             * Copie les enregistrements dans la table destination.
             */
            b.setLength(0);
            b.append("INSERT INTO \"");
            b.append(table);
            b.append("\" (");
            for (int i=0; i<columns.length; i++) {
                if (i != 0) {
                    b.append(',');
                }
                b.append('"');
                b.append(columns[i]);
                b.append('"');
            }
            b.append(") VALUES (");
            final int valuesStart = b.length();
            while (sources.next()) {
                b.setLength(valuesStart);
                for (int i=0; i<columns.length;) {
                    if (i != 0) {
                        b.append(',');
                    }
                    b.append('\'');
                    b.append(sources.getString(++i));
                    b.append('\'');
                }
                b.append(')');
                sql = b.toString();
                count = targetStmt.executeUpdate(sql);
                if (count != 1) {
                    break;
                }
                log(LoggingLevel.INSERT, "replace", sql);
            }
            sources.close();
            success = true;
        } finally {
            if (success) {
                target.commit();
            } else {
                target.rollback();
            }
        }
        sourceStmt.close();
        targetStmt.close();
        if (!success) {
            throw new SQLException("Certains enregistrements n'ont pas pu �tre ajout�s.");
        }
    }

    /**
     * Lib�re les resources utilis�es par cet objet.
     */
    public void close() throws SQLException {
        // Ne PAS fermer 'source', car il reste utilis� par Observations.
        target.close();
    }

    /**
     * Enregistre un �v�nement du niveau sp�cifi� dans le journal.
     */
    private static void log(final Level level, final String method, final String message) {
        final LogRecord record = new LogRecord(level, message);
        record.setSourceClassName("Synchronize");
        record.setSourceMethodName(method);
        Element.LOGGER.log(record);
    }

    /**
     * Lance {@link #replace} � partir de la ligne de commande.
     */
    public static void main(String[] args) throws CatalogException, SQLException {
        if (false) {
            // A des fins de d�boguages seulement.
            args = new String[] {
                "-target",      "jdbc:postgresql://server/database",
                "-user",        "",
                "-password",    "",
                "-table",       "LinearModelTerms",
                "-condition",   "target = 'Potentiel de p�che ALB-optimal (Cal�donie)'"
            };
        }
        final Arguments arguments = new Arguments(args);
        final String target    = arguments.getRequiredString("-target");
        final String user      = arguments.getRequiredString("-user");
        final String password  = arguments.getRequiredString("-password");
        final String table     = arguments.getRequiredString("-table");
        final String condition = arguments.getOptionalString("-condition");
        args = arguments.getRemainingArguments(0);
        final Synchronizer synchronizer = new Synchronizer(target, user, password);
        synchronizer.replace(table, condition);
        synchronizer.close();
    }
}
