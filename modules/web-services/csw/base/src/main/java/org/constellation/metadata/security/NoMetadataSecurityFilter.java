/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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


package org.constellation.metadata.security;

/**
 * A basic security filter allowing all the metadata to everyone.
 * @author Guilhem Legal (Geomatys)
 */
public class NoMetadataSecurityFilter implements MetadataSecurityFilter {

    @Override
    public String[] filterResults(final String login, final String[] results) {
        return results;
    }

    @Override
    public boolean allowed(final String login, final String id) {
        return true;
    }
}
