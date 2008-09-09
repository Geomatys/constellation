/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
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
package org.constellation.coverage.metadata;


import org.constellation.catalog.Entry;


/**
 * Implementation of a {@linkplain CoverageMetadata layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class CoverageMetadataEntry extends Entry implements CoverageMetadata {

    private String id;
    private String coverageId;
    private String uri;
    private String creationDate;
    private String seriesName;

    protected CoverageMetadataEntry(
        final String id,
        final String coverageId,
        final String uri,
        final String creationDate,
        final String seriesName )

    {
        super(id);
        this.id = id;
        this.coverageId = coverageId;
        this.uri = uri;
        this.creationDate = creationDate;
        this.seriesName = seriesName;
    }    

    public String getMetadata() {
        String res = "<table border=1>" +
                //"<tr><td>ID:</td><td>                " + id             + "</td></tr>" +
                //"<tr><td>Coverage ID:</td><td>       " + coverageId     + "</td></tr>" +
                "<tr><td>URI:</td><td>               " + uri            + "</td></tr>" +
                "<tr><td>Creation Date:</td><td>     " + creationDate   + "</td></tr>" +
                //"<tr><td>Series Name:</td><td>       " + seriesName     + "</td></tr>" +
                "</table>";
                
        return res;
    }
    
    public String getCreationDate() {
        return this.creationDate;
    }
}
