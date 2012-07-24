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
package org.constellation.provider.coveragesgroup;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import static org.constellation.provider.coveragesgroup.CoveragesGroupProviderService.*;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Cédric Briançon
 */
public class CoveragesGroupProvider extends AbstractLayerProvider {

    public static final String KEY_FOLDER_PATH = "path";
    public static final String KEY_MAP_CONTEXT = "mapContext";

    private final Map<Name,File> index = new HashMap<Name,File>();

    private File folder;

    public CoveragesGroupProvider(final ProviderService service, final ParameterValueGroup param) {
        super(service,param);
        visit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Name> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayerDetails get(final Name key) {
        final File mapContextFile = index.get(key);
        if (mapContextFile != null) {
            return new CoveragesGroupLayerDetails(key, mapContextFile);
        }
        return null;
    }

    private static ParameterValueGroup getSourceConfiguration(final ParameterValueGroup params){
        final List<ParameterValueGroup> groups = params.groups(SOURCE_CONFIG_DESCRIPTOR.getName().getCode());
        if(!groups.isEmpty()){
            return groups.get(0);
        }
        return null;
    }

    /**
     * Write a Geotk {@link MapContext} in an xml file.
     *
     * @param key file key, where to store the xml.
     * @param mapContext A map context to parse.
     * @throws JAXBException
     */
    public void write(final Name key, final MapContext mapContext) throws JAXBException {
        final File mapContextFile = index.get(key);

        final org.geotoolkit.providers.xml.MapItem finalMapItem = new org.geotoolkit.providers.xml.MapItem(null);
        final org.geotoolkit.providers.xml.MapContext finalMapContext = new org.geotoolkit.providers.xml.MapContext(finalMapItem);
        if (mapContextFile != null) {
            for (final MapItem mapItem : mapContext.items()) {
                if (mapItem instanceof FeatureMapLayer) {
                    final FeatureMapLayer fml = (FeatureMapLayer) mapItem;
                    final String id = fml.getCollection().getID();
                    final MutableStyle ms = fml.getSelectionStyle();
                    final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                            new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                    final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                    finalMapItem.getMapItems().add(ml);
                } else if (mapItem instanceof CoverageMapLayer) {
                    final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                    final String id = cml.getCoverageReference().getName().getLocalPart();
                    final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), (org.geotoolkit.providers.xml.StyleReference)null);
                    finalMapItem.getMapItems().add(ml);
                } else {
                    visitMapItem(mapItem, finalMapItem);
                }
            }
        }

        // write finalMapContext
        if (mapContextFile != null) {
            final MarshallerPool pool = new MarshallerPool(org.geotoolkit.providers.xml.MapContext.class, org.geotoolkit.internal.jaxb.geometry.ObjectFactory.class);
            final Marshaller marshaller = pool.acquireMarshaller();
            marshaller.marshal(finalMapContext, mapContextFile);
            pool.release(marshaller);
        }
    }

    private void visitMapItem(final MapItem mapItemOrig, final org.geotoolkit.providers.xml.MapItem finalMapItem) {
        for (final MapItem mapItem : mapItemOrig.items()) {
            if (mapItem instanceof FeatureMapLayer) {
                final FeatureMapLayer fml = (FeatureMapLayer) mapItem;
                final String id = fml.getCollection().getID();
                final MutableStyle ms = fml.getSelectionStyle();
                    final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                            new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                    final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                    finalMapItem.getMapItems().add(ml);
            } else if (mapItem instanceof CoverageMapLayer) {
                final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                final String id = cml.getCoverageReference().getName().getLocalPart();
                final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), (org.geotoolkit.providers.xml.StyleReference)null);
                    finalMapItem.getMapItems().add(ml);
            } else {
                final org.geotoolkit.providers.xml.MapItem finalMapItemChild = new org.geotoolkit.providers.xml.MapItem(null);
                finalMapItem.getMapItems().add(finalMapItemChild);
                visitMapItem(mapItem, finalMapItemChild);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            index.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
        }
    }

    /**
     * Visit all files to keep only XML files.
     */
    @Override
    protected void visit() {
        final ParameterValue<URL> paramFolder = (ParameterValue<URL>) getSourceConfiguration(getSource()).parameter(FOLDER_DESCRIPTOR.getName().getCode());

        if(paramFolder == null){
            getLogger().log(Level.WARNING,"Provided File path is not defined.");
            return;
        }

        final URL path = paramFolder.getValue();

        if (path == null) {
            getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
            return;
        }

        try {
            folder = new File(path.toURI());
        } catch (URISyntaxException e) {
            getLogger().log(Level.INFO,"Fails to convert path url to file.");
            folder = new File(path.getPath());
        }

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
            return;
        }

        visit(folder);

        final ParameterValue<MapContext> paramMapContext = (ParameterValue<MapContext>) getSourceConfiguration(getSource()).parameter(MAP_CONTEXT_DESCRIPTOR.getName().getCode());
        if (paramMapContext != null) {
            final MapContext mapContext = paramMapContext.getValue();
            if (mapContext != null) {
                final DefaultName name = new DefaultName(mapContext.getName());
                File tempFile = index.get(name);
                if (tempFile == null) {
                    tempFile = new File(folder, mapContext.getName() + ".xml");
                    index.put(name, tempFile);
                }
                try {
                    write(name, mapContext);
                } catch (JAXBException e) {
                    getLogger().log(Level.INFO, "Unable to do the marshalling of the map context object");
                }
            }
        }

        super.visit();
    }

    /**
     * Visit all files and directories contained in the directory specified, and add
     * all XML files in {@link #index}.
     *
     * @param file The starting file or folder.
     */
    private void visit(final File file) {

        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        } else {
            test(file);
        }
    }

    /**
     * Keep only XML files.
     *
     * @param candidate Candidate to be a map context file.
     */
    private void test(final File candidate) {
        final String fullName = candidate.getName();
        final int idx = fullName.lastIndexOf('.');
        final String extension = fullName.substring(idx + 1);
        if (extension.equalsIgnoreCase("xml")) {
            final String name = fullName.substring(0, idx);
            index.put(new DefaultName(name), candidate);
        }
    }
}
