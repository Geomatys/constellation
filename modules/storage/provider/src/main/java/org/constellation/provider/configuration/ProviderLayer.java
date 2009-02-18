/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.provider.configuration;

import java.util.List;

/**
 * A container for layer source infos.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ProviderLayer {

    public final String name;
    public final List<String> styles;
    public final String dateStartField;
    public final String dateEndField;
    public final String elevationStartField;
    public final String elevationEndField;
    public final String elevationModel;
    public final boolean isElevationModel;
    
    ProviderLayer(final String name,final List<String> styles,
            final String startDate, final String endDate,
            final String startElevation, final String endElevation,
            final boolean isElevationModel, final String elevationModel){
        if(name == null || styles == null){
            throw new NullPointerException("Name and style list must not be null");
        }
        this.name = name;
        this.styles = styles;
        this.dateStartField = startDate;
        this.dateEndField = endDate;
        this.elevationStartField = startElevation;
        this.elevationEndField = endElevation;
        this.isElevationModel = isElevationModel;
        this.elevationModel = elevationModel;
    }
    
    
    
}
