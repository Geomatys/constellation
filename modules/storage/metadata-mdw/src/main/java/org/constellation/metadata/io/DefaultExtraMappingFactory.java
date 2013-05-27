/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.metadata.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultExtraMappingFactory implements ExtraMappingFactory {

    @Override
    public Map<String, List<String>> getExtraPackage() {
        final Map<String, List<String>> extraPackage = new HashMap<String, List<String>>();
        extraPackage.put("NATSDI",    Arrays.asList("org.geotoolkit.naturesdi")); // TODO move to naturesdi code
        extraPackage.put("GEONETCAB", Arrays.asList("org.geotoolkit.geotnetcab")); // TODO move to geonetcab code
        extraPackage.put("CNES",      Arrays.asList("org.geotoolkit.cnes"));// TODO move to cnes code
        return extraPackage;
    }

    @Override
    public Map<String, List<String>> getExtraStandard() {
        final Map<String, List<String>> extraStandard = new HashMap<String, List<String>>();
        extraStandard.put("ISO 19115", Arrays.asList("NATSDI","GEONETCAB","CNES"));// TODO move to subProject code
        return extraStandard;
    }

}
