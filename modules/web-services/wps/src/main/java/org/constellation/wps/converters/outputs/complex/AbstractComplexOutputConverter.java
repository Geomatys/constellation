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
package org.constellation.wps.converters.outputs.complex;

import java.util.Map;
import org.geotoolkit.util.converter.SimpleConverter;
import org.geotoolkit.wps.xml.v100.ComplexDataType;

/**
 *
 * @author Quentin Boileau (Geometry).
 */
public abstract class AbstractComplexOutputConverter extends SimpleConverter<Map<String, Object>, ComplexDataType> {

    public static final String OUT_DATA      = "outData";
    public static final String OUT_MIME      = "outMime";
    public static final String OUT_SCHEMA    = "outSchema";
    public static final String OUT_ENCODING  = "outEncoding";
    public static final String OUT_TMP_DIR_PATH = "outTempDirectoryPath";
    public static final String OUT_TMP_DIR_URL = "outTempDirectoryUrl";

    @Override
    public Class<? super Map<String, Object>> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends ComplexDataType> getTargetClass() {
        return ComplexDataType.class;
    }


}
