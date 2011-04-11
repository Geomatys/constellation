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
package org.constellation.provider.om;

import org.constellation.provider.AbstractDataStoreProvider;

import org.geotoolkit.data.om.OMDataStoreFactory;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Observation and measurement Data provider. index and cache Datastores for the specified database.
 *
 * @version $Id:
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public class OMProvider extends AbstractDataStoreProvider {

    protected OMProvider(final OMProviderService service,
            final ParameterValueGroup source) throws DataStoreException {
        super(service,source);
    }

    @Override
    protected ParameterDescriptorGroup getDatastoreDescriptor() {
        return OMDataStoreFactory.PARAMETERS_DESCRIPTOR;
    }

}
