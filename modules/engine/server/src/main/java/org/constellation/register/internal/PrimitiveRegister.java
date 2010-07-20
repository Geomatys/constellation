/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.register.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.constellation.ServiceDef;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.register.PrimitiveRegisterIF;
import org.constellation.register.RegisterException;
import org.opengis.feature.type.Name;

/**
 * First attempt at a Register, we merely want something functional for now.
 * <p>
 * <b>Users should not user this class directly!</b><br/>
 * Instead, users should call {@link Cstl.Portrayal} and work through the 
 * interface.
 * </p>
 * 
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 * @see Cstl.Portrayal
 *
 */
public final class PrimitiveRegister implements PrimitiveRegisterIF {

    private static PrimitiveRegister instance = null;

    public static PrimitiveRegister internalGetInstance() {
        if (null == instance) {
            instance = new PrimitiveRegister();
        }
        return instance;
    }

    private boolean isServiceAllowed(String action, ServiceDef serviceDef) {
        //For now we allow all actions.
        //  this should be extended to handle user permissions
        if (serviceDef == ServiceDef.WMS_1_1_1     || serviceDef == ServiceDef.WMS_1_3_0     ||
            serviceDef == ServiceDef.WMS_1_1_1_SLD || serviceDef == ServiceDef.WMS_1_3_0_SLD ||
            serviceDef == ServiceDef.WCS_1_0_0     || serviceDef == ServiceDef.WCS_1_1_1     ||
            serviceDef == ServiceDef.WMTS_1_0_0) {

            return true;
        }
        return false;
    }

    //TODO: only handling providers for now.
    @Override
    public List<LayerDetails> getAllLayerReferences(ServiceDef serviceDef) throws RegisterException {

        if (isServiceAllowed("read all files", serviceDef)) {
            return getAllLayerRefs(serviceDef);
        }

        /* SHOULD NOT REACH HERE */
        throw new RegisterException("Unsupported service type: " + serviceDef);
    }

    @Override
    public List<LayerDetails> getLayerReferences(ServiceDef serviceDef,
            List<Name> layerNames) throws RegisterException {

        if (isServiceAllowed("read all files", serviceDef)) {
            return getLayerRefs(serviceDef, layerNames);
        }

        /* SHOULD NOT REACH HERE */
        throw new RegisterException("Unsupported service type: " + serviceDef);

    }

    @Override
    public LayerDetails getLayerReference(ServiceDef serviceDef, Name layerName) throws RegisterException {

        if (isServiceAllowed("read all files", serviceDef)) {
            return getLayerRef(serviceDef, layerName);
        }

        /* SHOULD NOT REACH HERE */
        throw new RegisterException("Unsupported service type: " + serviceDef);

    }

    private List<LayerDetails> getAllLayerRefs(ServiceDef serviceDef) throws RegisterException {

        final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
        final Set<Name> layerNames = LayerProviderProxy.getInstance().getKeys(serviceDef.specification.name());
        for (Name layerName : layerNames) {
            final LayerDetails layerRef = LayerProviderProxy.getInstance().get(layerName);

            if (null == layerRef) {
                throw new RegisterException("Unknown layer " + layerName);
            }

            layerRefs.add(layerRef);
        }

        return layerRefs;

    }

    private List<LayerDetails> getLayerRefs(ServiceDef serviceDef, List<Name> layerNames) throws RegisterException {

        final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
        for (Name layerName : layerNames) {
            final LayerDetails layerRef = LayerProviderProxy.getInstance().getByIdentifier(layerName,
                    serviceDef.specification.name());

            if (null == layerRef) {
                throw new RegisterException("Unknown layer " + layerName);
            }
            layerRefs.add(layerRef);
        }

        return layerRefs;
    }

    private LayerDetails getLayerRef(ServiceDef serviceDef, Name layerName) throws RegisterException {

        final LayerDetails layerRef = LayerProviderProxy.getInstance().getByIdentifier(layerName,
                serviceDef.specification.name());

        if (null == layerRef) {
            throw new RegisterException("Unknown layer " + layerName);
        }
        return layerRef;
    }

    @Override
    public List<String> getRootDirectory() throws RegisterException {

        final List<String> rootDirectories = new ArrayList<String>();
        final Collection<LayerProviderService> services = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService service : services) {
            for (LayerProvider p : service.getProviders()) {
                final String s = p.getSource().parameters.get("rootDirectory");
                if (s != null) {
                   rootDirectories.add(s);
                }
            }
        }
        return rootDirectories;
    }
}
