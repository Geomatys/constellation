/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.catalog;


/**
 * A key for a configurable aspect of a {@linkplain Database database}. They are typically keys
 * in a {@linkplain java.util.Properties properties map}. A key may be used in order to specify
 * the JDBC driver to use.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 *
 * @see Database#getProperty
 * @see org.constellation.gui.swing.ConfigurationEditor
 */
public enum ConfigurationKey {
    /**
     * Key for the local data directory root. The value may be {@code null} if data are not accessible
     * locally. In such case, data may be accessible remotely from the {@link #ROOT_URL}.
     * <p>
     * The default value is {@code null}.
     */
    ROOT_DIRECTORY("RootDirectory", null),

    /**
     * Key for the URL to the server that host the data.
     * <p>
     * The default value is {@code "ftp://localhost/"}.
     */
    ROOT_URL("RootURL", "ftp://localhost/"),

    /**
     * Key for the database driver. A typical value is {@code "org.postgresql.Driver"}. This is
     * used only if no data source has been given explicitly to the {@linkplain Database database}
     * constructor.
     * <p>
     * The default value is {@code "org.apache.derby.jdbc.EmbeddedDriver"}.
     */
    DRIVER("Driver", "org.apache.derby.jdbc.EmbeddedDriver"),

    /**
     * Key for URL to the database. Example: {@code "jdbc:postgresql://myserver.com/mydatabase"}.
     * used only if no data source has been given explicitly to the {@linkplain Database database}
     * constructor.
     * <p>
     * The default value is {@code "jdbc:derby:postgrid"}.
     */
    DATABASE("Database", "jdbc:derby:postgrid"),

    /**
     * The database catalog to use, or {@code null} if none. This is not widely used except
     * by Oracle.
     */
    CATALOG("Catalog", null),

    /**
     * The database schema to use, or {@code null} if none. In the later case, the tables
     * will be located using the default mechanism on the underlying database. On PostgreSQL,
     * the search order is determined by the {@code "search_path"} database variable.
     */
    SCHEMA("Schema", null),

    /**
     * Key for the {@linkplain java.sql.Connection#setReadOnly read only} state
     * of the database connection. The default value is {@code true}.
     */
    READONLY("ReadOnly", "true"),

    /**
     * Key for user permissions.
     * <p>
     * The default value is {@code "Anonymous"}.
     */
    PERMISSION("Permission", "Anonymous"),

    /**
     * Key for user name connecting to the {@linkplain #DATABASE database}.
     */
    USER("User", null),

    /**
     * Key for {@linkplain #USER user} password.
     */
    PASSWORD("Password", null),

    /**
     * Key for the RMI (<cite>Remote Method Invocation</cite>) server. The default value
     * is {@code null}, which means that images will be downloaded by FTP processed locally
     * instead of delagating the work to some distant server.
     */
    REMOTE_SERVER("RemoteServer", null),

    /**
     * Key for the timezone. This apply to the dates that appear in the database.
     * The {@code "local"} value is a special string for the local timezone.
     * <p>
     * The default value is {@code "UTC"}.
     */
    TIMEZONE("TimeZone", "UTC"),

    /**
     * Key indicating if the database is a postgreSQL one.
     * The default value is {@code "true"}.
     */
    ISPOSTGRES("IsPostgres", "true");

    /**
     * Le nom de la clé courante. Chaque clé a un nom unique.
     */
    private final String name;

    /**
     * Valeur par défaut associée à la clé. Cette valeur est codée en dur dans les implémentations
     * qui définissent des clés comme variables statiques. Cette valeur par défaut ne sera utilisée
     * que si l'utilisateur n'a pas spécifié explicitement de valeur dans le fichier de configuration.
     */
    private final String defaultValue;

    /**
     * Construit une nouvelle clé.
     *
     * @param name          Le nom de la clé à créer. Ce nom doit être unique.
     * @param defaultValue  Valeur par défaut associée à la clé, utilisée que si l'utilisateur n'a
     *                      pas spécifié explicitement de valeur.
     */
    private ConfigurationKey(final String name, final String defaultValue) {
        this.name         = name.trim();
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the key to be used in {@link java.util.Properties} map.
     */
    public String getKey() {
        return name;
    }

    /**
     * Returns the default value for this key.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns this key name, for inclusion in a graphical user interface.
     */
    @Override
    public String toString() {
        return name;
    }
}
