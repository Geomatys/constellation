/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.map.setup;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

import com.sun.jersey.core.spi.component.ProviderServices;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.map.configuration.StyleProviderConfig;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.CreateProviderDescriptor;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.ProviderService;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.internal.SetupService;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.DefaultRasterSymbolizer;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.Symbolizer;
import org.opengis.util.NoSuchIdentifierException;

import static org.geotoolkit.style.StyleConstants.*;

/**
 * specific setup for map service 
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class MapSetup implements SetupService {

    private static final Logger LOGGER = Logging.getLogger(MapSetup.class);
    
    @Override
    public void initialize(Properties properties, boolean reinit) {
        
        LOGGER.log(Level.INFO, "=== Activating Native Codec ===");
        
        //reset values, only allow pure java readers
        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }

        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }
        
        //reset values, only allow pure java writers
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
        }
        
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
        }

        // Create default SLD provider containing default styles.
        StyleProvider provider = StyleProviderProxy.getInstance().getProvider("sld");
        if (provider == null) {
            // Acquire SLD provider service instance.
            ProviderService sldService = null;
            for (final ProviderService service : StyleProviderProxy.getInstance().getServices()) {
                if (service.getName().equals("sld")) {
                    sldService = service;
                    break;
                }
            }
            if (sldService == null) {
                LOGGER.log(Level.WARNING, "SLD provider service not found.");
                return;
            }

            // Prepare create provider process inputs.
            final ParameterDescriptorGroup serviceDesc = sldService.getServiceDescriptor();
            final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) serviceDesc.descriptor("source");
            final ParameterValueGroup source = sourceDesc.createValue();
            source.parameter("id").setValue("sld");
            source.parameter("providerType").setValue("sld");
            source.groups("sldFolder").get(0).parameter("path").setValue(ConfigDirectory.getStyleDirectory().getPath());

            // Create SLD provider.
            try {
                final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);
                final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue("sld");
                inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                desc.createProcess(inputs).call();
            } catch (NoSuchIdentifierException ignore) { // should never happen
            } catch (ProcessException ex) {
                LOGGER.log(Level.WARNING, "An error occurred when creating default SLD provider.", ex);
                return;
            }

            // Retrieve created provider instance.
            provider = StyleProviderProxy.getInstance().getProvider("sld");
        }

        // Fill default SLD provider.
        final DefaultStyleFactory sf = new DefaultStyleFactory();
        try {
            if (provider.get("default-point") == null) {
                final MutableStyle style = sf.style(DEFAULT_POINT_SYMBOLIZER);
                style.setName("default-point");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-line") == null) {
                final MutableStyle style = sf.style(DEFAULT_LINE_SYMBOLIZER);
                style.setName("default-line");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-polygon") == null) {
                final MutableStyle style = sf.style(DEFAULT_POLYGON_SYMBOLIZER);
                style.setName("default-polygon");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-raster") == null) {
                final MutableStyle style = sf.style(DEFAULT_RASTER_SYMBOLIZER);
                style.setName("default-raster");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-hybrid") == null) {
                final MutableStyle style = sf.style(new Symbolizer[]{DEFAULT_POINT_SYMBOLIZER, DEFAULT_RASTER_SYMBOLIZER});
                style.setName("default-hybrid");
                StyleProviderConfig.createStyle("sld", style);
            }
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "An error occurred when creating default styles for default SLD provider.", ex);
        }
    }

    /**
     * Invoked when the module needs to be shutdown.
     */
    @Override
    public void shutdown() {
        LayerProviderProxy.getInstance().dispose();
        StyleProviderProxy.getInstance().dispose();
    }
}
