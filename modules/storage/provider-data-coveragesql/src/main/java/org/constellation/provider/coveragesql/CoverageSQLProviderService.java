/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2011, Geomatys
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
package org.constellation.provider.coveragesql;

import java.util.logging.Level;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.*;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid coverage-sql provider config";

    public static final ParameterDescriptor<String> SERVER_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("server","",String.class,null,true);
    public static final ParameterDescriptor<Integer> PORT_DESCRIPTOR =
             new DefaultParameterDescriptor<Integer>("port","",Integer.class,5432,false);
    public static final ParameterDescriptor<String> DATABASE_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("database","",String.class,null,true);
    public static final ParameterDescriptor<String> SCHEMA_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("schema","",String.class,null,false);
    public static final ParameterDescriptor<String> USER_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("user","",String.class,null,false);
    public static final ParameterDescriptor<String> PASSWORD_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("password","",String.class,null,false);
    public static final ParameterDescriptor<Boolean> READONLY_DESCRIPTOR =
             new DefaultParameterDescriptor<Boolean>("readOnly","",Boolean.class,null,false);
    public static final ParameterDescriptor<String> DRIVER_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("driver","",String.class,null,false);
    public static final ParameterDescriptor<String> ROOT_DIRECTORY_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("rootDirectory","",String.class,null,false);
    public static final ParameterDescriptor<String> NAMESPACE_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("namespace","",String.class,null,false);
    public static final ParameterDescriptorGroup COVERAGESQL_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("coveragesql",
            SERVER_DESCRIPTOR,PORT_DESCRIPTOR,DATABASE_DESCRIPTOR,SCHEMA_DESCRIPTOR,
            USER_DESCRIPTOR,PASSWORD_DESCRIPTOR,READONLY_DESCRIPTOR,DRIVER_DESCRIPTOR,
            ROOT_DIRECTORY_DESCRIPTOR,NAMESPACE_DESCRIPTOR);
    private static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            ProviderParameters.createDescriptor(COVERAGESQL_DESCRIPTOR);




    public CoverageSQLProviderService(){
        super("coverage-sql");
    }

    @Override
    public ParameterDescriptorGroup getDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public LayerProvider createProvider(ParameterValueGroup ps) {
        try {
            final CoverageSQLProvider provider = new CoverageSQLProvider(this,ps);
            ps = ProviderParameters.getOrCreate(COVERAGESQL_DESCRIPTOR, ps);
            getLogger().log(Level.INFO, "[PROVIDER]> coverage-sql provider created : {0} > {1}"
                    , new Object[]{
                        value(DATABASE_DESCRIPTOR, ps),
                        value(ROOT_DIRECTORY_DESCRIPTOR, ps)
                     });
            return provider;
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            getLogger().log(Level.SEVERE, ERROR_MSG, ex);
        }
        return null;
    }

}
