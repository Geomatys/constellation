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

package org.constellation.wfs.ws.rs;

import org.geotoolkit.data.FeatureCollection;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ValueCollectionWrapper {


    private final FeatureCollection featureCollection;

    private final String valueReference;

    private final String gmlVersion;

    public ValueCollectionWrapper(final FeatureCollection featureCollection, final String valueReference, final String gmlVersion) {
        this.featureCollection = featureCollection;
        this.gmlVersion        = gmlVersion;
        this.valueReference    = valueReference;

    }

    /**
     * @return the featureCollection
     */
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * @return the gmlVersion
     */
    public String getGmlVersion() {
        return gmlVersion;
    }

    /**
     * @return the valueReference
     */
    public String getValueReference() {
        return valueReference;
    }
}
