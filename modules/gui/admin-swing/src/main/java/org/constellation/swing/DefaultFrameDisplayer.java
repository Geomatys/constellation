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
package org.constellation.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ObjectFactory;
import org.constellation.configuration.ProviderReport;
import org.geotoolkit.client.Server;
import org.geotoolkit.client.ServerFactory;
import org.geotoolkit.client.ServerFinder;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.swing.go2.JMap2DFrame;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.DefaultDescription;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.style.StyleConstants;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultFrameDisplayer implements FrameDisplayer {

    private static final Logger LOGGER = Logging.getLogger(DefaultFrameDisplayer.class);

    @Override
    public void display(final JComponent edit) {
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setContentPane(edit);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setTitle(edit.getName());

        final PropertyChangeListener cl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("update".equals(evt.getPropertyName())) {
                    dialog.dispose();
                }
            }
        };
        edit.addPropertyChangeListener(cl);

        dialog.setVisible(true);
    }

    @Override
    public void display(final ConstellationServer cstl, final String serviceType, final Instance service) {
        try {
            final String url = cstl.services.getInstanceURL(serviceType, service.getName());
            final ServerFactory factory = ServerFinder.getFactoryById(serviceType);
            final ParameterValueGroup params = factory.getParametersDescriptor().createValue();
            params.parameter("url").setValue(new URL(url));
            params.parameter("security").setValue(cstl.getClientSecurity());
            try {
                params.parameter("post").setValue(true);
            } catch(ParameterNotFoundException ex) {
                // do nothing if the parameters does not exist
            }
            final Server server = factory.open(params);
            display(server);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
    }

    @Override
    public void display(final ConstellationServer server, final String providerType, final ProviderReport pr) {
        final GeneralParameterDescriptor desc = server.providers.getServiceDescriptor(providerType);
        if(!(desc instanceof ParameterDescriptorGroup)) {
            return;
        }

        // parameters needed to build the store.
        final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) ((ParameterDescriptorGroup) desc).descriptor(ObjectFactory.SOURCE_QNAME.getLocalPart());
        final ParameterValueGroup gpv = (ParameterValueGroup) server.providers.getProviderConfiguration(pr.getId(), sourceDesc);

        try {
            MapContext context = getProviderLayers(gpv);
            display(context);
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }

    }

    protected MapContext getProviderLayers(final ParameterValueGroup providerParameters) throws DataStoreException {

        MapContext result = MapBuilder.createContext();
        ParameterValueGroup choiceParam = providerParameters.groups("choice").get(0);

        if (!choiceParam.values().isEmpty()) {
            final String providerName = (String) providerParameters.parameter("id").getValue();
            final ParameterValueGroup storeconfig = (ParameterValueGroup) choiceParam.values().get(0);

            /**
             * As we don't know yet if we'll have a coverage or a datastore, we
             * try with the both of them, and check for the one which return
             * data
             */
            //Coverages
            final CoverageStore cStore = CoverageStoreFinder.open(storeconfig);
            if (cStore != null) {
                MutableStyle style = new DefaultStyleFactory().style(StyleConstants.DEFAULT_RASTER_SYMBOLIZER);
                for (Name name : cStore.getNames()) {
                    final MapLayer layer = MapBuilder.createCoverageLayer(cStore.getCoverageReference(name), style, name.getLocalPart());
                    result.layers().add(layer);
                }
                //datastore
            }

            final FeatureStore dStore = FeatureStoreFinder.open(storeconfig);

            if(dStore != null){
                Session storeSession = dStore.createSession(true);

                for (Name name : dStore.getNames()) {
                    final FeatureCollection collection = storeSession.getFeatureCollection(QueryBuilder.all(name));
                    final MutableStyle style = RandomStyleBuilder.createDefaultVectorStyle(collection.getFeatureType());
                    final MapLayer layer = MapBuilder.createFeatureLayer(collection, style);
                    layer.setName(name.toString());
                    layer.setDescription(new DefaultDescription(new SimpleInternationalString(name.toString()), null));
                    result.layers().add(layer);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param candidate , Server, DataStore or CoverageStore
     */
    protected void display(final Object candidate) throws DataStoreException{

        final MapContext context = MapBuilder.createContext();

        if (candidate instanceof CoverageStore) {
            final CoverageStore cs = (CoverageStore) candidate;

            for (Name n : cs.getNames()) {
                final CoverageReference ref = cs.getCoverageReference(n);
                final CoverageMapLayer layer = MapBuilder.createCoverageLayer(ref,
                        GO2Utilities.STYLE_FACTORY.style(StyleConstants.DEFAULT_RASTER_SYMBOLIZER), ref.getName().toString());
                layer.setVisible(false);
                context.layers().add(layer);
            }

        } else if (candidate instanceof FeatureStore) {
            final FeatureStore ds = (FeatureStore) candidate;
            final Session storeSession = ds.createSession(true);
            for (Name n : ds.getNames()) {
                final FeatureCollection collection = storeSession.getFeatureCollection(QueryBuilder.all(n));
                final MutableStyle style = RandomStyleBuilder.createDefaultVectorStyle(collection.getFeatureType());
                final FeatureMapLayer layer = MapBuilder.createFeatureLayer(collection, style);
                layer.setName(n.toString());
                layer.setDescription(new DefaultDescription(new SimpleInternationalString(n.toString()), null));
                layer.setVisible(false);
                context.layers().add(layer);
            }
        }
        display(context);
    }

    protected void display(final MapContext context){
        JMap2DFrame.show(context);
    }

}
