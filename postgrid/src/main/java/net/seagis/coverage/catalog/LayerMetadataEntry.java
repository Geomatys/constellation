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
package net.seagis.coverage.catalog;


import net.seagis.catalog.Entry;


/**
 * Implementation of a {@linkplain LayerMetadata layer metadata entry}.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadataEntry.java  $
 */
final class LayerMetadataEntry extends Entry implements LayerMetadata {

    private String layer_meta_name;
    private String layer_name;
    private String abbr_title;
    private String short_title;
    private String long_title;
    private String parameter_name;
    private String parameter_type;
    private String description;
    private String long_description;
    private String data_source;
    private String purpose;
    private String supplemental_info;
    private String update_frequency;
    private String use_constraint;
    
    /**
     * Creates a new layer.
     *
     * @param name         The layer name.
     * @param thematic     Thematic for this layer (e.g. Temperature, Salinity, etc.).
     * @param procedure    Procedure applied for this layer (e.g. Gradients, etc.).
     * @param timeInterval Typical time interval (in days) between images, or {@link Double#NaN} if unknown.
     * @param remarks      Optional remarks, or {@code null}.
     */
    protected LayerMetadataEntry(
        final String layer_meta_name,
        final String layer_name,
        final String abbr_title,
        final String short_title,
        final String long_title,
        final String parameter_name,
        final String parameter_type,
        final String description,
        final String long_description,
        final String data_source,
        final String purpose,
        final String supplemental_info,
        final String update_frequency,
        final String use_constraint)
    {
        super(layer_meta_name);
        this.layer_meta_name = layer_meta_name;
        this.layer_name = layer_name;
        this.abbr_title = abbr_title;
        this.short_title = short_title;
        this.long_title = long_title;
        this.parameter_name = parameter_name;
        this.parameter_type = parameter_type;
        this.description = description;
        this.long_description = long_description;
        this.data_source = data_source;
        this.purpose = purpose;
        this.supplemental_info = supplemental_info;
        this.update_frequency = update_frequency;
        this.use_constraint = use_constraint;
    }    

    public String getMetadata() {
        String res = 
                "layer meta name:   " + layer_meta_name + "\n" +
                "layer name:        " + layer_name + "\n" +
                "abbr title:        " + abbr_title + "\n" +
                "short title:       " + short_title + "\n" +
                "long title:        " + long_title + "\n" +
                "parameter name:    " + parameter_name + "\n" +
                "parameter type:    "+ parameter_type + "\n" +
                "description:       " + description + "\n" +
                "long description:  " + long_description + "\n" +
                "data source:       " + data_source + "\n" +
                "purpose            " + purpose + "\n" +
                "supplemental info: " + supplemental_info + "\n" +
                "update frequency:  " + update_frequency + "\n" +
                "use constraint:    " + use_constraint +"\n" ;
        return res;
    }
}
