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

import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;

import javax.imageio.spi.ServiceRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigurationException;
import org.geotoolkit.processing.AbstractProcessingRegistry;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConstellationProcessFactory extends AbstractProcessingRegistry {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.process");

    /**Factory name*/
    public static final String NAME = "constellation";
    public static final DefaultServiceIdentification IDENTIFICATION;

    static {
        IDENTIFICATION = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME);
        final DefaultCitation citation = new DefaultCitation(NAME);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION.setCitation(citation);
    }

    /**
     * Default constructor
     */
    public ConstellationProcessFactory() {
        super( findDescriptors() );
    }

    @Override
    public Identification getIdentification() {
        return IDENTIFICATION;
    }

    /**
     * Find all processDescriptor defined in META-INF/service files.
     * @return
     */
    private static synchronized ProcessDescriptor[] findDescriptors() {
        final Iterator<ProcessDescriptor> ite = ServiceRegistry.lookupProviders(ProcessDescriptor.class);
        final List<ProcessDescriptor> descriptors = new ArrayList<>();
        while (ite.hasNext()) {
            descriptors.add(ite.next());
        }

        try {
            descriptors.addAll(new ChainProcessRetriever().getChainDescriptors());
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Exception while retrieving chain process", ex);
        }

        return descriptors.toArray(new ProcessDescriptor[descriptors.size()]);
    }
}
