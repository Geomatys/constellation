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
package org.constellation.admin;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.constellation.business.IProcessBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.ChainProcess;
import org.constellation.engine.register.repository.ChainProcessRepository;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.chain.ChainProcessDescriptor;
import org.geotoolkit.process.chain.model.Chain;
import org.geotoolkit.process.chain.model.ChainMarshallerPool;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
        
/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
@Component
@Primary
public class ProcessBusiness implements IProcessBusiness {

    @Inject
    private ChainProcessRepository chainRepository;
     
    public List<ProcessDescriptor> getChainDescriptors() throws ConfigurationException {
        final List<ProcessDescriptor> result = new ArrayList<>();
        final List<ChainProcess> chains = chainRepository.findAll();
        for (ChainProcess cp : chains) {
            try {
                final Unmarshaller u = ChainMarshallerPool.getInstance().acquireUnmarshaller();
                final Chain chain = (Chain) u.unmarshal(new StringReader(cp.getConfig()));
                ChainMarshallerPool.getInstance().recycle(u);
                final ProcessDescriptor desc = new ChainProcessDescriptor(chain, buildIdentification(chain.getName()));
                result.add(desc);
            } catch (JAXBException ex) {
                throw new ConfigurationException("Unable to unmarshall chain configuration:" + cp.getId(), ex);
            }
        }
        return result;
    }
    
    public Identification buildIdentification(final String name) {
        final DefaultServiceIdentification ident = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(name);
        final DefaultCitation citation = new DefaultCitation(name);
        citation.setIdentifiers(Collections.singleton(id));
        ident.setCitation(citation);
        return ident;
    }
    
    public void createChainProcess(final Chain chain) throws ConfigurationException {
        final String code = chain.getName();
        String config = null;
        try {
            final Marshaller m = ChainMarshallerPool.getInstance().acquireMarshaller();
            final StringWriter sw = new StringWriter();
            m.marshal(chain, sw);
            ChainMarshallerPool.getInstance().recycle(m);
            config = sw.toString();
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to marshall chain configuration");
        }
        final ChainProcess process = new ChainProcess("constellation", code, config);
        chainRepository.create(process);
    }
    
    public boolean deleteChainProcess(final String auth, final String code) {
        final ChainProcess chain = chainRepository.findOne(auth, code);
        if (chain != null) {
            chainRepository.delete(chain.getId());
            return true;
        }
        return false;
    }
    
    public ChainProcess getChainProcess(final String auth, final String code) {
        return chainRepository.findOne(auth, code);
    }
}
