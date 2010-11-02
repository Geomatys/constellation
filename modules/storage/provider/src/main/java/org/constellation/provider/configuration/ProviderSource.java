/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for source informations.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderSource {
    /**
     * Defines whether all data found in the directory specified in {@link #parameters}
     * are taken in account, or just the one listed in {@link #layers}.
     * By default, {@code false}, meaning only layers defined in {@link #layers} are handled.
     */
    public boolean loadAll = false;

    /**
     * The identifier of the source.
     */
    public String id;

    public final Map<String,String> parameters = new HashMap<String, String>();
    
    public final List<ProviderLayer> layers = new ArrayList<ProviderLayer>();

    public ProviderLayer getLayer(final String key){
        for (final ProviderLayer layer : layers) {
            if (layer.name.equals(key)) {
                return layer;
            }
        }
        return null;
    }
    
    public List<String> getStyles(final String key){
        for(final ProviderLayer layer : layers){
            if(layer.name.equals(key)){
                return layer.styles;
            }
        }
        return Collections.emptyList();
    }
    
    public boolean containsLayer(String key){
        for(final ProviderLayer layer : layers){
            if(layer.name.equals(key)){
                return true;
            }
        }
        return false;
    }
    
    
}
