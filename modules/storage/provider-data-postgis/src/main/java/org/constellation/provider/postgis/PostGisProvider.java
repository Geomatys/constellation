/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2011, Geomatys
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
package org.constellation.provider.postgis;

import org.constellation.provider.AbstractDataStoreProvider;

import org.geotoolkit.data.postgis.PostgisNGDataStoreFactory;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * PostGIS Data provider.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class PostGisProvider extends AbstractDataStoreProvider{

    protected PostGisProvider(final PostGisProviderService service,
            final ParameterValueGroup source) throws DataStoreException {
        super(service,source);
    }

    @Override
    protected ParameterDescriptorGroup getDatastoreDescriptor() {
        return PostgisNGDataStoreFactory.PARAMETERS_DESCRIPTOR;
    }
    
}
