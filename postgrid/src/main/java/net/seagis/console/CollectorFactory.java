/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008 Geomatys
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
package net.seagis.console;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import org.geotools.factory.AbstractFactory;
import org.geotools.factory.FactoryRegistry;


/**
 * Service interface provider for {@link Collector}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CollectorFactory extends AbstractFactory {
    /**
     * The service registry for collectors.
     */
    private static FactoryRegistry registry;

    /**
     * Creates a provider with the default priority level.
     */
    public CollectorFactory() {
        this(NORMAL_PRIORITY);
    }

    /**
     * Creates a provider with the specified priority.
     */
    protected CollectorFactory(final int priority) {
        super(priority);
    }

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param database The database connection, or {@code null} for the default.
     * @throws CatalogException if the connection failed.
     */
    protected Collector create(final Database database) throws CatalogException {
        return new Collector(database);
    }

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param database The database connection, or {@code null} for the default.
     * @throws CatalogException if the connection failed.
     */
    public static synchronized Collector getInstance(final Database database) throws CatalogException {
        if (registry == null) {
            registry = new FactoryRegistry(new Class<?>[] {
                CollectorFactory.class

            });
        }
        final CollectorFactory spi = registry.getServiceProvider(CollectorFactory.class, null, null, null);
        return spi.create(database);
    }
}
