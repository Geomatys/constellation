/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.admin.service;

import org.geotoolkit.client.AbstractClientFactory;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collections;
import org.apache.sis.storage.DataStoreException;

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
