/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.process.service;

import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.coveragesgroup.CoveragesGroupProvider;
import org.constellation.provider.coveragesgroup.xml.*;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.constellation.process.service.RemoveDataFromMapContextDescriptor.*;
import org.geotoolkit.feature.type.NamesExt;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import org.opengis.util.GenericName;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RemoveDataFromMapContext extends AbstractCstlProcess {
    
    public RemoveDataFromMapContext(final ParameterValueGroup input) {
        super(INSTANCE,input);
    }

    /**
     * Quick constructor
     * @param providerID
     * @param groupName
     * @param layerName
     * @param layerProvider
     */
    public RemoveDataFromMapContext (final String providerID, final String groupName, final String layerName,
                                final String layerProvider) {
        this(toParameters(providerID, groupName, layerName, layerProvider));
    }

    private static ParameterValueGroup toParameters(final String providerID, final String groupName, final String layerName,
                                                    final String layerProvider){
        final ParameterValueGroup params = INSTANCE.getInputDescriptor().createValue();
        getOrCreate(CONTEXT_PROVIDER_ID, params).setValue(providerID);
        getOrCreate(CONTEXT_NAME, params).setValue(groupName);
        getOrCreate(DATA_NAME, params).setValue(layerName);
        getOrCreate(DATA_PROVIDER_ID, params).setValue(layerProvider);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {
        fireProcessStarted("Start remove to map Context");
        final String providerID         = getOrCreate(CONTEXT_PROVIDER_ID, inputParameters).stringValue();
        final String groupName          = getOrCreate(CONTEXT_NAME, inputParameters).stringValue();
        final String layerName          = getOrCreate(DATA_NAME, inputParameters).stringValue();
        final String layerProvider      = getOrCreate(DATA_PROVIDER_ID, inputParameters).stringValue();

        final DataProvider p = DataProviders.getInstance().getProvider(providerID);
        if (p != null && p instanceof CoveragesGroupProvider) {
            final CoveragesGroupProvider coveragesGroupProvider = (CoveragesGroupProvider) p;

            final DataReference dataRef = new DataReference("${providerLayerType|" + layerProvider + "|" + layerName + "}");
            try {
                final GenericName key = NamesExt.create(groupName);
                MapContext rawMapContext = coveragesGroupProvider.getRawMapContext(key);

                if (rawMapContext == null) {
                    throw new ProcessException("MapContext configuration not found for key : "+groupName,this, null);
                }
                List<MapItem> items = rawMapContext.getMapItem().getMapItems();
                List<MapItem> toRemove = new ArrayList<>();
                for (MapItem item : items) {
                    if (item instanceof MapLayer && ((MapLayer)item).getDataReference().equals(dataRef)) {
                        toRemove.add(item);
                    }
                }
                rawMapContext.getMapItem().getMapItems().removeAll(toRemove);

                coveragesGroupProvider.addRawMapContext(key, rawMapContext);
                DataProviders.getInstance().getProvider(providerID).reload();

            } catch (JAXBException | IOException ex) {
                throw new ProcessException("JAXB error during add data to map context", this, ex);
            }
        } else {
            throw new ProcessException("Unable to find a provider:" + providerID, this, null);
        }
    }
}
