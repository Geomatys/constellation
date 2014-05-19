/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
