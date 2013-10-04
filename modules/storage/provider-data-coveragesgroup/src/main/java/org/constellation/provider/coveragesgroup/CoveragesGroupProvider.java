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

import org.apache.sis.xml.MarshallerPool;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.admin.dao.DataRecord.DataType;

import static org.constellation.provider.coveragesgroup.CoveragesGroupProviderService.*;

/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class CoveragesGroupProvider extends AbstractLayerProvider {

    public static final String KEY_PATH = "path";
    public static final String KEY_MAP_CONTEXT = "mapContext";

    private final Map<Name,File> index = new HashMap<>();

    private File path;

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
       return get(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayerDetails get(final Name key, Date version) {
        return get(key, null, null);
    }

    /**
     * hacked method to pass the login/pass to WebMapServer
     */
    public LayerDetails get(final Name key, final String login, final String password) {
        final File mapContextFile = index.get(key);
        if (mapContextFile != null) {
            return new CoveragesGroupLayerDetails(key, mapContextFile, login, password);
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
    public void write(final Name key, final MapContext mapContext) throws JAXBException, IOException {
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
                    ml.setOpacity(fml.getOpacity());
                    finalMapItem.getMapItems().add(ml);
                } else if (mapItem instanceof CoverageMapLayer) {
                    final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                    final String id = cml.getCoverageReference().getName().getLocalPart();
                    final MutableStyle ms = cml.getSelectionStyle();
                    final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                            new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                    final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                    ml.setOpacity(cml.getOpacity());
                    finalMapItem.getMapItems().add(ml);
                } else {
                    visitMapItem(mapItem, finalMapItem);
                }
            }
        }

        // write finalMapContext
        if (mapContextFile != null) {
            final MarshallerPool pool = new MarshallerPool(JAXBContext.newInstance(org.geotoolkit.providers.xml.MapContext.class, org.apache.sis.internal.jaxb.geometry.ObjectFactory.class), null);
            final Marshaller marshaller = pool.acquireMarshaller();
            if (!mapContextFile.exists()) {
                mapContextFile.createNewFile();
            }
            marshaller.marshal(finalMapContext, mapContextFile);
            pool.recycle(marshaller);
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
                final MutableStyle ms = cml.getSelectionStyle();
                final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                        new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), styleRef);
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
        final ParameterValue<URL> paramUrl = (ParameterValue<URL>) getSourceConfiguration(getSource()).parameter(URL.getName().getCode());

        if(paramUrl == null || paramUrl.getValue() == null){
            getLogger().log(Level.WARNING,"Provided File path is not defined.");
            return;
        }

        final URL urlPath = paramUrl.getValue();

        try {
            path = new File(urlPath.toURI());
        } catch (URISyntaxException e) {
            getLogger().log(Level.INFO,"Fails to convert path url to file.");
            path = new File(urlPath.getPath());
        }

        visit(path);

        final ParameterValue<MapContext> paramMapContext = (ParameterValue<MapContext>) getSourceConfiguration(getSource()).parameter(MAP_CONTEXT.getName().getCode());
        if (paramMapContext != null) {
            final MapContext mapContext = paramMapContext.getValue();
            if (mapContext != null) {
                final DefaultName name = new DefaultName(mapContext.getName());
                File tempFile = index.get(name);
                if (tempFile == null) {
                    if (path.isDirectory()) {
                        tempFile = new File(path, mapContext.getName() + ".xml");
                    } else {
                        tempFile = path;
                    }
                    index.put(name, tempFile);
                }
                try {
                    write(name, mapContext);
                } catch (JAXBException e) {
                    getLogger().log(Level.WARNING, "Unable to do the marshalling of the map context object", e);
                } catch (IOException e) {
                    getLogger().log(Level.WARNING, "Unable to write the map context in this file!", e);
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

    @Override
    public DataType getDataType() {
        return DataType.COVERAGE;
    }

}
