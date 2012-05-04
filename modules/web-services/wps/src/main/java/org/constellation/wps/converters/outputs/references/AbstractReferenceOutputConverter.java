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
package org.constellation.wps.converters.outputs.references;

import java.util.Map;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.geotoolkit.wps.xml.v100.OutputReferenceType;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractReferenceOutputConverter extends SimpleConverter<Map<String, Object>, OutputReferenceType> {

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
    public Class<? extends OutputReferenceType> getTargetClass() {
        return OutputReferenceType.class;
    }


    /**
     * Convert the data from source Map into {@link ComplexDataType}. 
     * The {@code source} Map contain : 
     * <ul>
     *      <li>outData : the object to convert into {@link ComplexDataType}.</li>
     *      <li>outMime : the requested mime type for the output.</li>
     *      <li>outEncoding : the requested encoding for the output</li>
     *      <li>outSchema : the schema of the complex output</li>
     *      <li>outTempDirectoryPath : the absolute path to the output storage like schemas.</li>
     *      <li>outTempDirectoryUrl : the URL path to the web accessible storage folder.</li>
     * </ul>
     * @param source
     * @return the converted outData into {@link ComplexDataType}.
     * @throws NonconvertibleObjectException if an error occurs durring the convertion processing.
     */
    @Override
    public abstract OutputReferenceType convert(final Map<String, Object> source) throws NonconvertibleObjectException;
}
