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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.map.configuration.StyleProviderConfig;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.CreateProviderDescriptor;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderService;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import static org.constellation.provider.configuration.ProviderParameters.SOURCE_DESCRIPTOR_NAME;
import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import static org.geotoolkit.style.StyleConstants.*;

/**
 * specific setup for map service
 *
 * @author Guilhem Legal (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class MapSetup implements ServletContextListener {

    private static final Logger LOGGER = Logging.getLogger(MapSetup.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.log(Level.INFO, "=== Activating Native Codec ===");

        try {
            // Try to load postgresql driver for further use
            Class.forName("org.postgresql.ds.PGSimpleDataSource");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        }

        initializeDefaultStyles();

        initializeDefaultVectorData();
        initializeDefaultRasterData();
    }

    /**
     * Invoked when the module needs to be shutdown.
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        DataProviders.getInstance().dispose();
        StyleProviders.getInstance().dispose();
    }

    /**
     * Initialize default styles for generic data.
     */
    private void initializeDefaultStyles() {
        // Create default SLD provider containing default styles.
        StyleProvider provider = StyleProviders.getInstance().getProvider("sld");
        final String sldPath = ConfigDirectory.getStyleDirectory().getPath();
        if (provider == null) {
            // Acquire SLD provider service instance.
            ProviderService sldService = null;
            for (final ProviderService service : StyleProviders.getInstance().getServices()) {
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
            source.groups("sldFolder").get(0).parameter("path").setValue(sldPath);

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
            provider = StyleProviders.getInstance().getProvider("sld");
        }

        // Fill default SLD provider.
        final DefaultStyleFactory sf = new DefaultStyleFactory();
        try {
            if (provider.get("default-point") == null) {
                final MutableStyle style = sf.style(DEFAULT_POINT_SYMBOLIZER);
                style.setName("default-point");
                style.featureTypeStyles().get(0).rules().get(0).setName("default-point");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-line") == null) {
                final MutableStyle style = sf.style(DEFAULT_LINE_SYMBOLIZER);
                style.setName("default-line");
                style.featureTypeStyles().get(0).rules().get(0).setName("default-line");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-polygon") == null) {
                final MutableStyle style = sf.style(DEFAULT_POLYGON_SYMBOLIZER);
                style.setName("default-polygon");
                style.featureTypeStyles().get(0).rules().get(0).setName("default-polygon");
                StyleProviderConfig.createStyle("sld", style);
            }
            if (provider.get("default-raster") == null) {
                final MutableStyle style = sf.style(DEFAULT_RASTER_SYMBOLIZER);
                style.setName("default-raster");
                style.featureTypeStyles().get(0).rules().get(0).setName("default-raster");
                StyleProviderConfig.createStyle("sld", style);
            }
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "An error occurred when creating default styles for default SLD provider.", ex);
        }
    }

    /**
     * Initialize default vector data for displaying generic features in data editors.
     */
    private void initializeDefaultVectorData() {
        final File dst = new File(ConfigDirectory.getDataDirectory(), "shapes");
        try {
            if (dst.exists()) {
                if (!dst.isDirectory() || (dst.isDirectory() && dst.listFiles().length == 0)) {
                    final File src = FileUtilities.getDirectoryFromResource("org/constellation/map/setup/shapes");
                    FileUtilities.copy(src, dst);
                }
            } else {
                dst.mkdir();
                final File src = FileUtilities.getDirectoryFromResource("org/constellation/map/setup/shapes");
                FileUtilities.copy(src, dst);
            }

            final String featureStoreStr = "feature-store";
            final String shpProvName = "generic_shp";
            LayerProvider shpProvider = DataProviders.getInstance().getProvider(shpProvName);
            if (shpProvider == null) {
                // Acquire SHP provider service instance.
                ProviderService shpService = null;
                for (final ProviderService service : DataProviders.getInstance().getServices()) {
                    if (service.getName().equals(featureStoreStr)) {
                        shpService = service;
                        break;
                    }
                }
                if (shpService == null) {
                    LOGGER.log(Level.WARNING, "SHP provider service not found.");
                    return;
                }

                final ParameterValueGroup config = shpService.getServiceDescriptor().createValue();
                final ParameterValueGroup source = createGroup(config,SOURCE_DESCRIPTOR_NAME);
                getOrCreateValue(source, "id").setValue(shpProvName);
                getOrCreateValue(source, "load_all").setValue(true);
                getOrCreateValue(source, "providerType").setValue("vector");

                final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                final ParameterValueGroup shpConfig = createGroup(choice, "ShapefileParametersFolder");
                getOrCreateValue(shpConfig, "url").setValue(new URL("file:"+ dst.getAbsolutePath()));
                getOrCreateValue(shpConfig, "namespace").setValue("no namespace");

                // Create SHP Folder provider.
                try {
                    final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);
                    final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue(featureStoreStr);
                    inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                    desc.createProcess(inputs).call();
                } catch (NoSuchIdentifierException ignore) { // should never happen
                } catch (ProcessException ex) {
                    LOGGER.log(Level.WARNING, "An error occurred when creating default SHP provider.", ex);
                    return;
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Initialize default raster data for displaying generic features in data editors.
     */
    private void initializeDefaultRasterData() {
        final File dst = new File(ConfigDirectory.getDataDirectory(), "raster");
        try {
            if (dst.exists()) {
                if (!dst.isDirectory() || (dst.isDirectory() && dst.listFiles().length == 0)) {
                    final File src = FileUtilities.getDirectoryFromResource("org/constellation/map/setup/raster");
                    FileUtilities.copy(src, dst);
                }
            } else {
                dst.mkdir();
                final File src = FileUtilities.getDirectoryFromResource("org/constellation/map/setup/raster");
                FileUtilities.copy(src, dst);
            }

            final String coverageFileStr = "coverage-store";
            final String tifProvName = "generic_world_tif";
            LayerProvider tifProvider = DataProviders.getInstance().getProvider(tifProvName);
            if (tifProvider == null) {
                // Acquire TIFF provider service instance.
                ProviderService tifService = null;
                for (final ProviderService service : DataProviders.getInstance().getServices()) {
                    if (service.getName().equals(coverageFileStr)) {
                        tifService = service;
                        break;
                    }
                }
                if (tifService == null) {
                    LOGGER.log(Level.WARNING, "TIFF provider service not found.");
                    return;
                }

                final ParameterValueGroup config = tifService.getServiceDescriptor().createValue();
                final ParameterValueGroup source = createGroup(config,SOURCE_DESCRIPTOR_NAME);
                getOrCreateValue(source, "id").setValue(tifProvName);
                getOrCreateValue(source, "load_all").setValue(true);
                getOrCreateValue(source, "providerType").setValue("raster");

                final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                final ParameterValueGroup tifConfig = createGroup(choice, "FileCoverageStoreParameters");
                final File dstTif = new File(dst, "cloudsgrey.tiff");
                getOrCreateValue(tifConfig, "path").setValue(new URL("file:"+ dstTif.getAbsolutePath()));
                getOrCreateValue(tifConfig, "namespace").setValue("no namespace");

                // Create SHP Folder provider.
                try {
                    final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);
                    final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue(coverageFileStr);
                    inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                    desc.createProcess(inputs).call();
                } catch (NoSuchIdentifierException ignore) { // should never happen
                } catch (ProcessException ex) {
                    LOGGER.log(Level.WARNING, "An error occurred when creating default TIFF provider.", ex);
                    return;
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

}
