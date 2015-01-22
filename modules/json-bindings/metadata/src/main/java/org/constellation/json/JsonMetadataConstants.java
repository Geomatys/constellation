/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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
package org.constellation.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author guilhem
 */
public class JsonMetadataConstants {
    
    public static final String DATE_READ_ONLY = "DATE.readonly";
    
    /**
     * The object to use for parsing dates of the form "2014-09-11".
     * Usage of this format shall be synchronized on {@code DATE_FORMAT}.
     */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static final DateFormat DATE_HOUR_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static String cleanNumeratedPath(final String numeratedPath) {
        return numeratedPath.replaceAll("\\[[0-9]*\\]", "");
    }
}
