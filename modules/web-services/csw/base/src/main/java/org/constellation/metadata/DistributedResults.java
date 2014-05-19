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

package org.constellation.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DistributedResults {
        
    /**
     * The number of records matched on all distributed servers.
     */
    public int nbMatched;

    /**
     * The merged list of records.
     */
    public List<Object> additionalResults;

    /**
     * Build an empty distributed results with 0 records matching and 0 additional results.
     */
    public DistributedResults() {
        this.nbMatched         = 0;
        this.additionalResults = new ArrayList<Object>();
    }

    /**
     * Build a distributed results with the specified number of records matching and additional results.
     */
    public DistributedResults(int nbMatched, List<Object> additionalResults) {
        this.nbMatched         = nbMatched;
        this.additionalResults = additionalResults;
    }

}
