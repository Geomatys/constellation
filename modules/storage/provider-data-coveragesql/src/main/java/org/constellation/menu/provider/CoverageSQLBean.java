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

package org.constellation.menu.provider;

import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.coveragesql.CoverageSQLProvider;
import org.constellation.provider.coveragesql.CoverageSQLProviderService;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * Coverage-SQL configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLBean extends AbstractDataStoreServiceBean{

    private static LayerProviderService getService(){
        for(LayerProviderService service : LayerProviderProxy.getInstance().getServices()){
            if(service.getName().equals("coverage-sql")){
                return service;
            }
        }
        return null;
    }

    public CoverageSQLBean(){
        super(getService(),"/provider/coveragesql.xhtml",
              "/provider/coveragesqlConfig.xhtml",
              "/provider/coveragesqlLayerConfig.xhtml");
        addBundle("provider.coveragesql");
    }

    @Override
    protected Class getProviderClass() {
        return CoverageSQLProvider.class;
    }

    @Override
    protected GeneralParameterDescriptor getSourceDescriptor() {
        return CoverageSQLProviderService.COVERAGESQL_DESCRIPTOR;
    }

}
