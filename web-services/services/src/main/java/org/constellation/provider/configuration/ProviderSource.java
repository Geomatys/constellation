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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for source informations.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ProviderSource {

    public final Map<String,String> parameters = new HashMap<String, String>();
    
    public final Map<String,List<String>> layers = new HashMap<String, List<String>>();
        
    ProviderSource() {}
}
