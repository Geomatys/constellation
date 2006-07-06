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

import java.io.IOException;
import java.util.Properties;
import net.sicade.observation.ConfigurationKey;
import org.geotools.referencing.factory.epsg.SimpleDataSource;


/**
 * Connexion vers la base de donn�es EPSG.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EPSG extends SimpleDataSource {
    /**
     * Le pilote de la base de donn�es.
     * La valeur par d�faut est d�termin�e � partir de {@link Database#DRIVER}.
     */
    public static final ConfigurationKey DRIVER = new ConfigurationKey("EPSG:Driver", null);

    /**
     * L'URL vers la base de donn�es.
     * La valeur par d�faut est d�termin�e � partir de {@link Database#URL}.
     */
    public static final ConfigurationKey DATABASE = new ConfigurationKey("EPSG:Database", null);

    /**
     * Le sch�ma qui contient les tables de la base de donn�es EPSG.
     * La valeur par d�faut est {@code "epsg"}.
     */
    public static final ConfigurationKey SCHEMA = new ConfigurationKey("EPSG:Schema", "epsg");

    /**
     * L'utilisateur se connectant � la {@linkplain #DATABASE base de donn�es}.
     * La valeur par d�faut est d�termin�e � partir de {@link Database#USER}.
     */
    public static final ConfigurationKey USER = new ConfigurationKey("EPSG:User", null);

    /**
     * Le mot de passe de l'{@linkplain #USER utilisateur}.
     * La valeur par d�faut est d�termin�e � partir de {@link Database#PASSWORD}.
     */
    public static final ConfigurationKey PASSWORD = new ConfigurationKey("EPSG:Password", null);

    /**
     * Construit une source de donn�es par d�faut.
     */
    public EPSG() {
        this(getDatabase());
    }

    /**
     * Retourne une nouvelle source de donn�es pour la connexion sp�cifi�e.
     */
    public EPSG(final Database database) {
        super(getProperties(database));
    }

    /**
     * Retourne les param�tres de connexion. Ce code devrait appara�tre directement
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
     * Retourne les propri�t�s pour la base de donn�es sp�cifi�e. Ce code devrait appara�tre
     * directement dans le second constructeur si seulement Sun voulait bien faire le RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Properties getProperties(final Database database) {
        final Properties properties = new Properties();
        properties.setProperty("driver",   database.getProperty(DRIVER,   Database.DRIVER));
        properties.setProperty("url",      database.getProperty(DATABASE, Database.DATABASE));
        properties.setProperty("schema",   database.getProperty(SCHEMA));
        properties.setProperty("user",     database.getProperty(USER,     Database.USER));
        properties.setProperty("password", database.getProperty(PASSWORD, Database.PASSWORD));
        return properties;
    }
}
