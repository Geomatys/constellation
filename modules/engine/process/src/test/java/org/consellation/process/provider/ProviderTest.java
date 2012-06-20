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
import org.constellation.configuration.ConfigDirectory;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.ProviderService;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.ComplexAttribute;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class ProviderTest {
    
    private static File configDirectory;
    
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
    
    private static final String factory = "constellation";
    private String process;


    protected ProviderTest(final String process){
        this.process = process;
        
        
    }

    @Test
    public void findProcessTest() throws NoSuchIdentifierException{
        ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(factory, process);
        assertNotNull(desc);
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
        
        
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider1", true, new URL("http://dataurl.csv"), "csv1"));
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider2", true, new URL("http://dataurl.csv"), "csv2"));
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider3", true, new URL("http://dataurl.csv"), "csv3"));
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider4", true, new URL("http://dataurl.csv"), "csv4"));
        
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
            final URL url, final String identifier) throws MalformedURLException {
        
        ParameterDescriptorGroup desc = sercice.getServiceDescriptor();
       
        if (desc != null) {
            ComplexAttribute root = FeatureUtilities.toFeature(desc.createValue());
            final ParameterDescriptorGroup sourceDescriptor = (ParameterDescriptorGroup)desc.descriptor("source");
            ComplexAttribute source = (ComplexAttribute) FeatureUtilities.defaultProperty(root.getType().getDescriptor("source"));
            source.getProperty("id").setValue(providerID);
            source.getProperty("load_all").setValue(loadAll);
            
            ComplexAttribute choice = (ComplexAttribute) source.getProperty("choice");
            ComplexAttribute csv = (ComplexAttribute) FeatureUtilities.defaultProperty(choice.getType().getDescriptor("CSVParameters"));
            csv.getProperty("identifier").setValue(identifier);
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
