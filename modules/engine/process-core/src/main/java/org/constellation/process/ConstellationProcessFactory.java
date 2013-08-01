/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;

import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.geotoolkit.process.AbstractProcessingRegistry;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConstellationProcessFactory extends AbstractProcessingRegistry {

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
        final List<ProcessDescriptor> descriptors = new ArrayList<ProcessDescriptor>();
        while (ite.hasNext()) {
            descriptors.add(ite.next());
        }
        
        return descriptors.toArray(new ProcessDescriptor[descriptors.size()]);
    }
}
