/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.provider;

/**
 * Simple enum that contain all provider types.
 * @author Quentin Boileau (Geomatys).
 */
public enum ProviderType {

    SHAPEFILE("shapefile"),
    POSTGIS("postgis"),
    COVERAGE_SQL("coverage-sql"),
    DATA_STORE("data-store"),
    SERVER_STORE("server-store"),
    COVERAGE_STORE("coverage-store"),
    COVERAGE_GROUP("coverages-group"),
    SLD("sld");

    private String providerType;
    private ProviderType(final String providerType) {
        this.providerType = providerType;
    }

    public String getCode() {
        return this.providerType;
    }

}
