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
package org.constellation.admin.service;

import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.client.AbstractClientFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collections;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ConstellationServerFactory extends AbstractClientFactory{

    /** factory identification **/
    public static final String NAME = "cstl-admin";
    public static final DefaultServiceIdentification IDENTIFICATION;
    static {
        IDENTIFICATION = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME);
        final DefaultCitation citation = new DefaultCitation(NAME);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION.setCitation(citation);
    }

    public static final ParameterDescriptor<String> IDENTIFIER = new DefaultParameterDescriptor<String>(
            AbstractClientFactory.IDENTIFIER.getName().getCode(),
            AbstractClientFactory.IDENTIFIER.getRemarks(), String.class,NAME,true);

    public static final ParameterDescriptor<String> USER = new DefaultParameterDescriptor(
            "User","User login",String.class,null,true);
    public static final ParameterDescriptor<String> PASSWORD = new DefaultParameterDescriptor(
            "Password","User password",String.class,null,true);
    public static final ParameterDescriptor<String> SECURITY_TYPE = new DefaultParameterDescriptor(
            "SecurityType","Security type",String.class,"Basic",true);

    public static final ParameterDescriptorGroup PARAMETERS =
            new DefaultParameterDescriptorGroup("CstlParameters",
                    IDENTIFIER,URL,USER,PASSWORD,SECURITY_TYPE,SECURITY);

    @Override
    public Identification getIdentification() {
        return IDENTIFICATION;
    }

    @Override
    public ParameterDescriptorGroup getParametersDescriptor() {
        return PARAMETERS;
    }

    @Override
    public ConstellationServer open(ParameterValueGroup params) throws DataStoreException {
        checkCanProcessWithError(params);
        final ConstellationServer server = new ConstellationServer(params);
        return server;
    }

}
