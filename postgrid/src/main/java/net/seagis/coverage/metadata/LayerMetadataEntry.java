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
 * Implementation of a {@linkplain LayerMetadata layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadataEntry.java  $
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
        String res = 
                "layer meta name:   " + layerMetaName + "\n" +
                "layer name:        " + layerName + "\n" +
                "abbr title:        " + abbrTitle + "\n" +
                "short title:       " + shortTitle + "\n" +
                "long title:        " + longTitle + "\n" +
                "parameter name:    " + parameterName + "\n" +
                "parameter type:    "+ parameterType + "\n" +
                "description:       " + description + "\n" +
                "long description:  " + longDescription + "\n" +
                "data source:       " + dataSource + "\n" +
                "purpose            " + purpose + "\n" +
                "supplemental info: " + supplementalInfo + "\n" +
                "update frequency:  " + updateFrequency + "\n" +
                "use constraint:    " + useConstraint +"\n" ;
        return res;
    }
}
