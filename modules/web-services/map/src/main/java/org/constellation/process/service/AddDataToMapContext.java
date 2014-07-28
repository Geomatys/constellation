/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.process.service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.process.AbstractCstlProcess;
import static org.constellation.process.service.AddDataToMapContextDescriptor.CONTEXT_NAME;
import static org.constellation.process.service.AddDataToMapContextDescriptor.CONTEXT_PROVIDER_ID;
import static org.constellation.process.service.AddDataToMapContextDescriptor.DATA_NAME;
import static org.constellation.process.service.AddDataToMapContextDescriptor.DATA_PROVIDER_ID;
import static org.constellation.process.service.AddDataToMapContextDescriptor.INSTANCE;
import static org.constellation.process.service.AddDataToMapContextDescriptor.LAYER_STYLE;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.coveragesgroup.xml.DataReference;
import org.constellation.provider.coveragesgroup.xml.MapContext;
import org.constellation.provider.coveragesgroup.xml.MapItem;
import org.constellation.provider.coveragesgroup.xml.MapLayer;
import org.constellation.provider.coveragesgroup.xml.StyleReference;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author guilhem
 */
public class AddDataToMapContext extends AbstractCstlProcess {
    
    public AddDataToMapContext(final ParameterValueGroup input) {
        super(INSTANCE,input);
    }

    @Override
    protected void execute() throws ProcessException {
        fireProcessStarted("Start Add to map Context");
        final String providerID         = getOrCreate(CONTEXT_PROVIDER_ID, inputParameters).stringValue();
        final String groupName          = getOrCreate(CONTEXT_NAME, inputParameters).stringValue();
        final String layerName          = getOrCreate(DATA_NAME, inputParameters).stringValue();
        final String layerProvider      = getOrCreate(DATA_PROVIDER_ID, inputParameters).stringValue();
        final StyleReference layerStyle = (StyleReference) getOrCreate(LAYER_STYLE, inputParameters).getValue();

        final DataProvider p = DataProviders.getInstance().getProvider(providerID);
        if (p != null) {
            final ParameterValueGroup source = p.getSource();
            final ParameterValueGroup params = ParametersExt.getGroup(source, "coveragesgroup");
            final File mapContextDirectory = new File(((URL)ParametersExt.getOrCreateValue(params, "path").getValue()).getFile());
            final DataReference dataRef = new DataReference("${providerLayerType|" + layerProvider + "|" + layerName + "}");
            final MapLayer layer = new MapLayer(dataRef, layerStyle);
            try {
                final JAXBContext ctx = JAXBContext.newInstance(MapContext.class, org.apache.sis.internal.jaxb.geometry.ObjectFactory.class);
                final File f = new File(mapContextDirectory, groupName + ".xml");
                final MapContext context;
                if (f.exists()) {
                    final Unmarshaller u = ctx.createUnmarshaller();
                    context = (MapContext) u.unmarshal(f);
                    context.getMapItem().getMapItems().add(layer);
                } else {
                    final List<MapItem> items = new ArrayList<>();
                    items.add(layer);
                    final MapItem item = new MapItem(items);
                    context = new MapContext(item, groupName);
                }

                final Marshaller m = ctx.createMarshaller();
                m.marshal(context, f);
                DataProviders.getInstance().getProvider(providerID).reload();
                
            } catch (JAXBException ex) {
                throw new ProcessException("JAXB error during add data to map context", this, ex);
            }
        } else {
            throw new ProcessException("Unable to find a provider:" + providerID, this, null);
        }
    }
}
