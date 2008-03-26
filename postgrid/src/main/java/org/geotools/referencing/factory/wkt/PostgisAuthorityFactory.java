/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.referencing.factory.wkt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import org.geotools.factory.Hints;
import org.geotools.util.GenericName;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.logging.Logging;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.wkt.Parser;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * An authority factory creating CRS from the {@code "spatial_ref_sys"} table in a spatial
 * SQL database. This class is called <code><u>Postgis</u>AuthorityFactory</code> because
 * of some assumptions more suitable to PostGIS, like the default {@linkplain #getAuthority
 * authority} if none were explicitly defined. But this class should be usable to other OGC
 * compliant spatial database as well.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PostgisAuthorityFactory extends DirectSqlAuthorityFactory implements CRSAuthorityFactory {

    // NOTE: the following table and column names are defined as static private constants
    //       because the code assumes that they do not require quotes (i.e. all lowercase
    //       names on PostGIS). We avoid quotes on purpose in order to let other database
    //       engines to convert the names to their native case (uppercase on HSQL for example).
    //       If we allow users to supply those names, we would need to revisit this assumption.

    /**
     * The standard name of the table for CRS definitions.
     */
    private static final String CRS_TABLE = "spatial_ref_sys";

    /**
     * The primary key column.
     */
    private static final String PRIMARY_KEY = "srid";

    /**
     * The standard name of the column for the authority name.
     */
    public static final String AUTHORITY_COLUMN = "auth_name";

    /**
     * The standard name of the column for the authority code.
     */
    public static final String CODE_COLUMN = "auth_srid";

    /**
     * The standard name of the column for the WKT.
     */
    public static final String WKT_COLUMN = "srtext";

    /**
     * The schema of the CRS table, or {@code null} if none.
     */
    private final String schema;

    /**
     * Authorities found in the database. Will be computed only when first needed.
     * Keys are authority names, and values are whatever the authority codes match
     * primary keys or not.
     */
    private transient Map<String,Boolean> authorities;

    /**
     * The authority. Will be computed only when first needed.
     */
    private transient Citation authority;

    /**
     * The prepared statement for fetching the primary key, or {@code null} if not yet created.
     */
    private transient PreparedStatement selectPK;

    /**
     * The prepared statement for selecting an object, or {@code null} if not yet created.
     */
    private transient PreparedStatement select;

    /**
     * Creates a factory.
     *
     * @param hints The hints, or {@code null} if none.
     * @param connection The connection to the database.
     */
    public PostgisAuthorityFactory(final Hints hints, final Connection connection) {
        super(hints, connection);
        this.schema = null;  // TODO: fetch from the hints.
    }

    /**
     * Appends the {@code "FROM"} clause to the specified SQL statement.
     */
    private StringBuilder appendFrom(final StringBuilder sql) {
        sql.append(" FROM ");
        if (schema != null) {
            sql.append(schema).append('.');
        }
        return sql.append(CRS_TABLE);
    }

    /**
     * Returns the default authority to declare as a fallback if no explicit authority
     * has been found in the database.
     */
    private static Citation getDefaultAuthority() {
        return Citations.POSTGIS;
    }

    /**
     * Returns the authority name. The default implementation returns the first authority
     * returned by {@link #getAuthorities}. This is typically {@link Citations#EPSG}, but
     * may be something different depending on the table content.
     */
    public synchronized Citation getAuthority() {
        if (authority == null) try {
            final Citation[] authorities = getAuthorities();
            if (authorities != null && authorities.length != 0) {
                authority = authorities[0];
            } else {
                authority = getDefaultAuthority();
            }
        } catch (FactoryException exception) {
            Logging.unexpectedException(LOGGER, PostgisAuthorityFactory.class, "getAuthority", exception);
            authority = getDefaultAuthority();
        }
        return authority;
    }

    /**
     * Returns all authority names declared in the CRS table. If some authorities use the same
     * codes than the primary key, then those authorities are returned first, ordered by the
     * most common ones.
     *
     * @return All authority found in the database.
     * @throws FactoryException if an access to the database failed.
     */
    public synchronized Citation[] getAuthorities() throws FactoryException {
        final Set<String> names = getAuthorityNames().keySet();
        final Citation[] authorities = new Citation[names.size()];
        int i = 0;
        for (final String name : names) {
            authorities[i++] = (name != null) ? Citations.fromName(name) : getDefaultAuthority();
        }
        return authorities;
    }

    /**
     * Returns the authority names found in the database. Keys are authority names,
     * and values are whatever the authority codes match primary keys or not.
     *
     * @return All authority names found in the database.
     * @throws FactoryException if an access to the database failed.
     */
    private Map<String,Boolean> getAuthorityNames() throws FactoryException {
        assert Thread.holdsLock(this);
        if (authorities == null) {
            final StringBuilder sql = new StringBuilder("SELECT ").append(AUTHORITY_COLUMN)
                    .append(", SUM(CASE WHEN ").append(CODE_COLUMN).append('=').append(PRIMARY_KEY)
                    .append(" THEN 1 ELSE 0 END) AS np, COUNT(").append(AUTHORITY_COLUMN).append(") AS n");
            appendFrom(sql)
                    .append(" GROUP BY ").append(AUTHORITY_COLUMN)
                    .append(" ORDER BY np DESC, n DESC");
            final Map<String,Boolean> names = new LinkedHashMap<String,Boolean>();
            try {
                final Statement stmt = getConnection().createStatement();
                final ResultSet results = stmt.executeQuery(sql.toString());
                while (results.next()) {
                    final String name = results.getString(1); // May be null.
                    final int    np   = results.getInt   (2);
                    final int    n    = results.getInt   (3);
                    names.put(name, np == n);
                }
                results.close();
                stmt.close();
            } catch (SQLException exception) {
                throw databaseFailure(null, null, exception);
            }
            authorities = names;
        }
        return authorities;
    }

    /**
     * Returns the authority codes defined in the database for the given type.
     *
     * @param category The type of objects to search for (typically <code>{@linkplain
     *                 org.opengis.referencing.crs.CoordinateReferenceSystem}.class</code>).
     * @return The set of available codes.
     * @throws FactoryException if an error occured while querying the database.
     */
    public synchronized Set<String> getAuthorityCodes(final Class<? extends IdentifiedObject> category)
            throws FactoryException
    {
        final StringBuilder sql = new StringBuilder("SELECT CASE WHEN ")
                .append(CODE_COLUMN).append('=').append(PRIMARY_KEY).append(" THEN ")
                .append(PRIMARY_KEY).append("::text ELSE ").append(AUTHORITY_COLUMN)
                .append(" || '").append(GenericName.DEFAULT_SEPARATOR).append("' || ")
                .append(CODE_COLUMN).append(" END AS code");
        appendFrom(sql);
        final String type = Parser.getNameOf(category);
        if (type != null) {
            sql.append(" WHERE srtext ILIKE '").append(type).append("%'");
        }
        sql.append(" ORDER BY ").append(PRIMARY_KEY);
        final Set<String> codes = new LinkedHashSet<String>();
        try {
            final Statement stmt = getConnection().createStatement();
            final ResultSet results = stmt.executeQuery(sql.toString());
            while (results.next()) {
                codes.add(results.getString(1));
            }
            results.close();
            stmt.close();
        } catch (SQLException exception) {
            throw databaseFailure(category, null, exception);
        }
        return codes;
    }

    /**
     * Returns the primary key for the specified authority code. If the supplied code contains an
     * <cite>authority</cite> part as in {@code "EPSG:4326"}, then (using the above code as an
     * example) this method searchs for a row with {@code "EPSG"} string in the <cite>authority
     * name</cite> column and {@code 4326} integer in the <cite>authority SRID</cite> column, and
     * returns the <cite>SRID</cite> for that row.
     * <p>
     * If the supplied code do not contains an <cite>authority</cite> part (e.g. {@code "4326"}),
     * then this method parses the code as an integer. This is consistent with common practice
     * where the spatial CRS table contains entries from a single authority with primary keys
     * identical to the authority codes. This is also consistent with the codes returned by
     * {@link #getAuthorityCodes}.
     *
     * @param  code The authority code to convert to primary key value.
     * @return The primary key for the supplied code (never {@code null}). There is no
     *         garantee that this key exists (this method may or may not query the database).
     * @throws NoSuchAuthorityCodeException if a code can't be parsed as an integer or can't
     *         be found in the database.
     * @throws FactoryException if an error occured while querying the database.
     */
    public synchronized Integer getPrimaryKey(String code) throws FactoryException {
        code = code.trim();
        final int separator = code.lastIndexOf(GenericName.DEFAULT_SEPARATOR);
        final String authority  = (separator >= 0) ? code.substring(0, separator).trim() : "";
        final String identifier = code.substring(separator+1).trim();
        Integer srid;
        try {
            srid = Integer.parseInt(identifier);
        } catch (NumberFormatException cause) {
            NoSuchAuthorityCodeException e = noSuchAuthorityCode(IdentifiedObject.class, code);
            e.initCause(cause);
            throw e;
        }
        if (authority.length()!=0 && !Boolean.TRUE.equals(getAuthorityNames().get(authority))) try {
            if (selectPK == null) {
                final StringBuilder sql = new StringBuilder("SELECT ").append(PRIMARY_KEY);
                appendFrom(sql).append(" WHERE ").append(AUTHORITY_COLUMN).append("=?")
                        .append(" AND ").append(CODE_COLUMN).append("=?");
                selectPK = getConnection().prepareStatement(sql.toString());
            }
            selectPK.setString(1, authority);
            selectPK.setInt   (2, srid);
            srid = singleton(selectPK, Integer.class, code, IdentifiedObject.class);
        } catch (SQLException exception) {
            throw databaseFailure(null, code, exception);
        }
        return srid;
    }

    /**
     * Returns a description of the CRS object for the given code.
     *
     * @param  code The code of the CRS object to query.
     * @return A description of the specified CRS object.
     * @throws FactoryException if an error occured while querying the database.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        return new SimpleInternationalString(createObject(code).getName().getCode());
    }

    /**
     * Returns an object for the given code.
     *
     * @param  code The code of the CRS object to query.
     * @return The CRS object for the given code.
     * @throws FactoryException if an error occured while querying the database.
     */
    @Override
    public synchronized IdentifiedObject createObject(final String code) throws FactoryException {
        return createCoordinateReferenceSystem(code);
    }

    /**
     * Returns a coordinate reference system for the given code.
     *
     * @param  code The code of the CRS object to query.
     * @return The CRS object for the given code.
     * @throws FactoryException if an error occured while querying the database.
     */
    @Override
    public synchronized CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        final Integer key = getPrimaryKey(code);
        final String wkt;
        try {
            if (select == null) {
                final StringBuilder sql = new StringBuilder("SELECT ").append(WKT_COLUMN);
                appendFrom(sql).append(" WHERE ").append(PRIMARY_KEY).append("=?");
                select = getConnection().prepareStatement(sql.toString());
            }
            select.setInt(1, key);
            wkt = singleton(select, String.class, code, CoordinateReferenceSystem.class);
        } catch (SQLException exception) {
            throw databaseFailure(CoordinateReferenceSystem.class, code, exception);
        }
        return factories.getCRSFactory().createFromWKT(wkt);
    }

    /**
     * Returns the value in the specified statement. This method ensure that the result set
     * contains only one value.
     *
     * @param  statement The statement to execute.
     * @param  type The type of the value to fetch.
     * @param  code The authority code, for formatting an error message if needed.
     * @param  product The type of the product to be created, for formatting an error message if needed.
     * @return The singleton value found.
     * @throws FactoryException if no value or more than one value were found.
     * @throws SQLException if the query failed for an other reason.
     */
    private <T> T singleton(final PreparedStatement statement, final Class<T> type,
                            final String code, final Class<?> product)
            throws FactoryException, SQLException
    {
        T value = null;
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final Object candidate;
            if (Integer.class.isAssignableFrom(type)) {
                candidate = results.getInt(1);
            } else {
                candidate = results.getString(1);
            }
            if (!results.wasNull()) {
                if (value != null && !candidate.equals(value)) {
                    results.close();
                    throw new FactoryException(Errors.format(ErrorKeys.DUPLICATED_VALUES_$1, code));
                }
                value = type.cast(candidate);
            }
        }
        results.close();
        if (value == null) {
            throw noSuchAuthorityCode(product, code);
        }
        return value;
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     *
     * @throws FactoryException if an error occured while disposing the factory.
     */
    @Override
    public synchronized void dispose() throws FactoryException {
        try {
            if (select != null) {
                select.close();
                select = null;
            }
            if (selectPK != null) {
                selectPK.close();
                selectPK = null;
            }
        } catch (SQLException exception) {
            throw databaseFailure(null, null, exception);
        }
        authority   = null;
        authorities = null;
        super.dispose();
    }
}
