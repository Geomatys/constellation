/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.ws.rs;

import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import javax.xml.bind.Marshaller;
import java.io.File;
import javax.xml.bind.JAXBException;

import org.constellation.ServiceDef;
import org.constellation.configuration.LayerContext;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * A Super class for WMS, WMTS, WFS and WCS web-service.
 * The point is to remove the hard-coded dependency to JAI.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @since 0.5
 */
public abstract class GridWebService<W extends Worker> extends OGCWebService<W> {

    public GridWebService(final ServiceDef... supportedVersions) {
        super(supportedVersions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void specificRestart(String identifier) {
        LOGGER.info("reloading provider");
        // clear style and layer caches.
        StyleProviderProxy.getInstance().dispose();
        LayerProviderProxy.getInstance().dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureInstance(final File instanceDirectory, final Object configuration) throws CstlServiceException {
        if (configuration instanceof LayerContext) {
            final File configurationFile = new File(instanceDirectory, "layerContext.xml");
            Marshaller marshaller = null;
            try {
                marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                
            } catch(JAXBException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } finally {
                if (marshaller != null) {
                    GenericDatabaseMarshallerPool.getInstance().release(marshaller);
                }
            }
        } else {
            throw new CstlServiceException("The configuration Object is not a layer context", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void basicConfigure(final File instanceDirectory) throws CstlServiceException {
        configureInstance(instanceDirectory, new LayerContext());
    }
}
