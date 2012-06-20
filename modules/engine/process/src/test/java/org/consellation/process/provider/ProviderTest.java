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
package org.consellation.process.provider;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.consellation.process.AbstractProcessTest;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.ProviderService;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.feature.ComplexAttribute;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class ProviderTest extends AbstractProcessTest {
    
    private static File configDirectory;
    protected static URL EMPTY_CSV;
    // dataStore service
    protected static ProviderService DATASTORE_SERVICE;
    static {
        final Collection<LayerProviderService> availableLayerServices = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService tmpService: availableLayerServices) {
            if ("data-store".equals(tmpService.getName())) {
                DATASTORE_SERVICE = tmpService;
            }
        }
    }
    
    protected ProviderTest(final String processName) {
        super(processName);
    }
    
    @BeforeClass
    public static void initFolder() throws MalformedURLException {
        
        configDirectory = new File("ProcessProviderTest");

        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }
        
        configDirectory.mkdir();
        File providerDirectory = new File(configDirectory, "provider");
        providerDirectory.mkdir();
        File datastore = new File(providerDirectory, "data-store.xml");
        ConfigDirectory.setConfigDirectory(configDirectory);    
        
        File csv = new File(configDirectory, "file.csv");
        EMPTY_CSV = csv.toURI().toURL();
        
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider1", true, EMPTY_CSV));
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider2", true, EMPTY_CSV));
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider3", true, EMPTY_CSV));
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider4", true, EMPTY_CSV));
        
    }
    
    @AfterClass
    public static void destroyFolder() {
        FileUtilities.deleteDirectory(configDirectory);
    }
    
    /**
     * Create a CSV provider for test using.
     * @param sercice
     * @param providerID
     * @return
     * @throws MalformedURLException 
     */
    protected static ParameterValueGroup buildCSVProvider(final ProviderService sercice, final String providerID, final boolean loadAll, 
            final URL url) throws MalformedURLException {
        
        ParameterDescriptorGroup desc = sercice.getServiceDescriptor();
       
        if (desc != null) {
            ComplexAttribute root = FeatureUtilities.toFeature(desc.createValue());
            final ParameterDescriptorGroup sourceDescriptor = (ParameterDescriptorGroup)desc.descriptor("source");
            ComplexAttribute source = (ComplexAttribute) FeatureUtilities.defaultProperty(root.getType().getDescriptor("source"));
            source.getProperty("id").setValue(providerID);
            source.getProperty("load_all").setValue(loadAll);
            
            ComplexAttribute choice = (ComplexAttribute) source.getProperty("choice");
            ComplexAttribute csv = (ComplexAttribute) FeatureUtilities.defaultProperty(choice.getType().getDescriptor("CSVParameters"));
            csv.getProperty("identifier").setValue("csv");
            csv.getProperty("url").setValue(url);
            
            choice.getProperties().add(csv);
            return FeatureUtilities.toParameter(source, sourceDescriptor);
        } else {
            //error
            return null;
        }
    }
    
    private static void addProvider(ParameterValueGroup providerSource) {
        LayerProviderProxy.getInstance().createProvider((LayerProviderService) DATASTORE_SERVICE, providerSource);
    }
}
