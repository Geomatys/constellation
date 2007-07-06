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

import java.io.IOException;
import java.util.Properties;
import net.sicade.observation.ConfigurationKey;
import org.geotools.referencing.factory.epsg.SimpleDataSource;


/**
 * Connexion vers la base de données EPSG.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EPSG extends SimpleDataSource {
    /**
     * Le pilote de la base de données.
     * La valeur par défaut est déterminée à partir de {@link Database#DRIVER}.
     */
    public static final ConfigurationKey DRIVER = new ConfigurationKey("EPSG:Driver", null);

    /**
     * L'URL vers la base de données.
     * La valeur par défaut est déterminée à partir de {@link Database#URL}.
     */
    public static final ConfigurationKey DATABASE = new ConfigurationKey("EPSG:Database", null);

    /**
     * Le schéma qui contient les tables de la base de données EPSG.
     * La valeur par défaut est {@code "epsg"}.
     */
    public static final ConfigurationKey SCHEMA = new ConfigurationKey("EPSG:Schema", "epsg");

    /**
     * L'utilisateur se connectant à la {@linkplain #DATABASE base de données}.
     * La valeur par défaut est déterminée à partir de {@link Database#USER}.
     */
    public static final ConfigurationKey USER = new ConfigurationKey("EPSG:User", null);

    /**
     * Le mot de passe de l'{@linkplain #USER utilisateur}.
     * La valeur par défaut est déterminée à partir de {@link Database#PASSWORD}.
     */
    public static final ConfigurationKey PASSWORD = new ConfigurationKey("EPSG:Password", null);

    /**
     * Construit une source de données par défaut.
     */
    public EPSG() {
        this(getDatabase());
    }

    /**
     * Retourne une nouvelle source de données pour la connexion spécifiée.
     */
    public EPSG(final Database database) {
        super(getProperties(database));
    }

    /**
     * Retourne les paramètres de connexion. Ce code devrait apparaître directement
     * dans le premier constructeur si seulement Sun voulait bien faire le RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Database getDatabase() {
        try {
            return new Database();
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Retourne les propriétés pour la base de données spécifiée. Ce code devrait apparaître
     * directement dans le second constructeur si seulement Sun voulait bien faire le RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Properties getProperties(final Database database) {
        final Properties properties = new Properties();
        database.getProperty(DRIVER,   Database.DRIVER,   properties, "driver"  );
        database.getProperty(DATABASE, Database.DATABASE, properties, "url"     );
        database.getProperty(SCHEMA,   null,              properties, "schema"  );
        database.getProperty(USER,     Database.USER,     properties, "user"    );
        database.getProperty(PASSWORD, Database.PASSWORD, properties, "password");
        return properties;
    }
}
