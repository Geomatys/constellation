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
package org.constellation.register.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.constellation.ServiceDef;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
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
    public List<Data> getAllLayerReferences(ServiceDef serviceDef) throws RegisterException {

        if (isServiceAllowed("read all files", serviceDef)) {
            return getAllLayerRefs(serviceDef);
        }

        /* SHOULD NOT REACH HERE */
        throw new RegisterException("Unsupported service type: " + serviceDef);
    }

    @Override
    public List<Data> getLayerReferences(ServiceDef serviceDef,
            List<Name> layerNames) throws RegisterException {

        if (isServiceAllowed("read all files", serviceDef)) {
            return getLayerRefs(layerNames);
        }

        /* SHOULD NOT REACH HERE */
        throw new RegisterException("Unsupported service type: " + serviceDef);

    }

    @Override
    public Data getLayerReference(ServiceDef serviceDef, Name layerName) throws RegisterException {

        if (isServiceAllowed("read all files", serviceDef)) {
            return getLayerRef(layerName);
        }

        /* SHOULD NOT REACH HERE */
        throw new RegisterException("Unsupported service type: " + serviceDef);

    }

    private List<Data> getAllLayerRefs(ServiceDef serviceDef) throws RegisterException {

        final List<Data> layerRefs = new ArrayList<Data>();
        final Set<Name> layerNames = DataProviders.getInstance().getKeys(serviceDef.specification.name());
        for (Name layerName : layerNames) {
            final Data layerRef = DataProviders.getInstance().get(layerName);

            if (null == layerRef) {
                throw new RegisterException("Unknown layer " + layerName);
            }

            layerRefs.add(layerRef);
        }

        return layerRefs;

    }

    private List<Data> getLayerRefs(List<Name> layerNames) throws RegisterException {

        final List<Data> layerRefs = new ArrayList<Data>();
        for (Name layerName : layerNames) {
            final Data layerRef = DataProviders.getInstance().getByIdentifier(layerName);

            if (null == layerRef) {
                throw new RegisterException("Unknown layer " + layerName);
            }
            layerRefs.add(layerRef);
        }

        return layerRefs;
    }

    private Data getLayerRef(Name layerName) throws RegisterException {

        final Data layerRef = DataProviders.getInstance().getByIdentifier(layerName);

        if (null == layerRef) {
            throw new RegisterException("Unknown layer " + layerName);
        }
        return layerRef;
    }

    @Override
    public List<String> getRootDirectory() throws RegisterException {

        final List<String> rootDirectories = new ArrayList<String>();
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            final String s = p.getSource().parameter("rootDirectory").stringValue();
            if (s != null) {
               rootDirectories.add(s);
            }
        }
        return rootDirectories;
    }
}
