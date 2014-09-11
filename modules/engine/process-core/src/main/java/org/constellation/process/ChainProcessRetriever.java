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


package org.constellation.process;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.constellation.admin.ProcessBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IProcessBusiness;
import org.constellation.configuration.ConfigurationException;
import org.geotoolkit.process.ProcessDescriptor;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ChainProcessRetriever {
    
    @Inject
    private IProcessBusiness processBusiness;

    public ChainProcessRetriever() {
        SpringHelper.injectDependencies(this);
    }
    
    public List<ProcessDescriptor> getChainDescriptors() throws ConfigurationException {
        final List<ProcessDescriptor> results = new ArrayList<>();
        if (processBusiness != null) {
            results.addAll(processBusiness.getChainDescriptors());
        }
        return results;
    }
}
