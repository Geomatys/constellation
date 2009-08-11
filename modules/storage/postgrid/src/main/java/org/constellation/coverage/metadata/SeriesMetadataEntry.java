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
 * Implementation of a {@linkplain SeriesMetadata layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class SeriesMetadataEntry extends Entry implements SeriesMetadata {

    private String id;
    private String seriesName;
    private String legendURI;
    private String pubDate;
    private String pocId;
    private String version;
    private String forecast;
    private String themekey1;
    private String themekey2;
    private String themekey3;
    private String themekey4;
    private String themekey5;
    private String themekey6;
    private String themekey7;
    private String themekey8;


    protected SeriesMetadataEntry(
        final String id,
        final String seriesName,
        final String legendURI,
        final String pubDate,
        final String pocId,
        final String version,
        final String forecast,
        final String themekey1,
        final String themekey2,
        final String themekey3,
        final String themekey4,
        final String themekey5,
        final String themekey6,
        final String themekey7,
        final String themekey8 )

    {
        super(seriesName);
        this.id = id;
        this.seriesName = seriesName;
        this.legendURI = legendURI;
        this.pubDate = pubDate;
        this.pocId = pocId;
        this.version = version;
        this.forecast = forecast;
        this.themekey1 = themekey1;
        this.themekey2 = themekey2;
        this.themekey3 = themekey3;
        this.themekey4 = themekey4;
        this.themekey5 = themekey5;
        this.themekey6 = themekey6;
        this.themekey7 = themekey7;
        this.themekey8 = themekey8;
    }    

    public String getMetadata() {
        final String closeRow = "</td></tr>";
        String res = "<table border=1>" +
                //"<tr><td>id:</td><td>                " + id + "</td></tr>" +
                //"<tr><td>Series Name:</td><td>       " + seriesName + "</td></tr>" +
                "<th colspan=2>" + seriesName + "</th>" +
                "<tr><td>Legend URI:</td><td>        " + legendURI + closeRow +
                "<tr><td>Publication Date:</td><td>          " + pubDate + closeRow +
                //"<tr><td>POC ID:</td><td>            " + pocId + "</td></tr>" +
                "<tr><td>Version:</td><td>           " + version   + closeRow +
                "<tr><td>Forecast:</td><td>          " + forecast  + closeRow +
                "<tr><td>Themekey1:</td><td>         " + themekey1 + closeRow +
                "<tr><td>Themekey2:</td><td>         " + themekey2 + closeRow +
                "<tr><td>Themekey3:</td><td>         " + themekey3 + closeRow +
                "<tr><td>Themekey4:</td><td>         " + themekey4 + closeRow +
                "<tr><td>Themekey5:</td><td>         " + themekey5 + closeRow +
                "<tr><td>Themekey6:</td><td>         " + themekey6 + closeRow +
                "<tr><td>Themekey7:</td><td>         " + themekey7 + closeRow +
                "<tr><td>Themekey8:</td><td>         " + themekey8 + closeRow +
                "</table>" ;
        return res;
    }
    public String getPointOfContactID() {
        return pocId;
    }
}
