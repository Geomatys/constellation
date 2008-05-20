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
package net.seagis.coverage.metadata;


import net.seagis.catalog.Entry;


/**
 * Implementation of a {@linkplain CoverageMetadata layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id: CoverageMetadataEntry.java  $
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
        String res = 
                "id:                " + id + "\n" +
                "coverage id:       " + coverageId + "\n" +
                "uri:               " + uri + "\n" +
                "creation date:     " + creationDate + "\n" +
                "series name:       " + seriesName + "\n" ;
                
        return res;
    }
}
