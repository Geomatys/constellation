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
 * Implementation of a {@linkplain LayerMetadata layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class LayerMetadataEntry extends Entry implements LayerMetadata {

    private String layerMetaName;
    private String layerName;
    private String abbrTitle;
    private String shortTitle;
    private String longTitle;
    private String parameterName;
    private String parameterType;
    private String description;
    private String longDescription;
    private String dataSource;
    private String purpose;
    private String supplementalInfo;
    private String updateFrequency;
    private String useConstraint;
    

    protected LayerMetadataEntry(
        final String layerMetaName,
        final String layerName,
        final String abbrTitle,
        final String shortTitle,
        final String longTitle,
        final String parameterName,
        final String parameterType,
        final String description,
        final String longDescription,
        final String dataSource,
        final String purpose,
        final String supplementalInfo,
        final String updateFrequency,
        final String useConstraint)
    {
        super(layerMetaName);
        this.layerMetaName = layerMetaName;
        this.layerName = layerName;
        this.abbrTitle = abbrTitle;
        this.shortTitle = shortTitle;
        this.longTitle = longTitle;
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        this.description = description;
        this.longDescription = longDescription;
        this.dataSource = dataSource;
        this.purpose = purpose;
        this.supplementalInfo = supplementalInfo;
        this.updateFrequency = updateFrequency;
        this.useConstraint = useConstraint;
    }    

    public String getMetadata() {
        String res = "<table border=1>" +
                //"<tr><td>layer meta name:   " + layerMetaName + "</td></tr>" +
                //"<tr><td>Layer Name:</td><td>        " + layerName + "</td></tr>" +
                "<th colspan=2>" + shortTitle + "</th>" +
                "<tr><td>Abbrev. Title:</td><td>        " + abbrTitle + "</td></tr>" +
                "<tr><td>Short Title:</td><td>       " + shortTitle + "</td></tr>" +
                "<tr><td>Long Title:</td><td>        " + longTitle + "</td></tr>" +
                "<tr><td>Parameter Pame:</td><td>    " + parameterName + "</td></tr>" +
                "<tr><td>Parameter Type:</td><td>    "+ parameterType + "</td></tr>" +
                "<tr><td>Description:</td><td>       " + description + "</td></tr>" +
                "<tr><td>Long Description:</td><td>  " + longDescription + "</td></tr>" +
                "<tr><td>Data Source:</td><td>       " + dataSource + "</td></tr>" +
                "<tr><td>Purpose:</td><td>            " + purpose + "</td></tr>" +
                "<tr><td>Supplemental Info:</td><td> " + supplementalInfo + "</td></tr>" +
                "<tr><td>Update Frequency:</td><td>  " + updateFrequency + "</td></tr>" +
                "<tr><td>Use Constraint:</td><td>    " + useConstraint +"</td></tr>" +
                "</table>" ;
        return res;
    }
    public String getLongTitle() {
        return longTitle;
    }
    public String getParameterName() {
        return parameterName;
    }
    public String getParameterType() {
        return parameterType;
    }
    public String getUpdateFrequency() {
        return updateFrequency;
    }
    public String getDescription() {
        return description;
    }
    public String getPurpose() {
        return purpose;
    }
    public String getUseConstraint() {
        return useConstraint;
    }    
}
