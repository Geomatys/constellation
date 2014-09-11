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
package org.constellation.business;

import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.ChainProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.chain.model.Chain;

import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IProcessBusiness {
    List<ProcessDescriptor> getChainDescriptors() throws ConfigurationException;

    void createChainProcess(final Chain chain) throws ConfigurationException;

    boolean deleteChainProcess(final String auth, final String code);

    ChainProcess getChainProcess(final String auth, final String code);
}
