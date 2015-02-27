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
package org.constellation.provider;

import java.util.List;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.image.io.metadata.SpatialMetadata;

/**
 * Coverage extension of a {@link Data}, which add some methods specific
 * for coverage layers.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public interface CoverageData extends Data {
    /**
     * @see Layer#getSeries
     */
    String getImageFormat();

    /**
     * @see Layer#getRemarks
     */
    @Deprecated
    String getRemarks();

    /**
     * @see Layer#getThematic
     */
    @Deprecated
    String getThematic();

    SpatialMetadata getSpatialMetadata() throws DataStoreException;

    List<GridSampleDimension> getSampleDimensions() throws DataStoreException;
}
