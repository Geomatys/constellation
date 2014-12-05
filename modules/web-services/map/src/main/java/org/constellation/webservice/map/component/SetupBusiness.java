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
package org.constellation.webservice.map.component;

import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import static org.geotoolkit.style.StyleConstants.DEFAULT_LINE_SYMBOLIZER;
import static org.geotoolkit.style.StyleConstants.DEFAULT_POINT_SYMBOLIZER;
import static org.geotoolkit.style.StyleConstants.DEFAULT_POLYGON_SYMBOLIZER;
import static org.geotoolkit.style.StyleConstants.DEFAULT_RASTER_SYMBOLIZER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sis.util.logging.Logging;
import org.constellation.api.PropertyConstants;
import org.constellation.business.IConfigurationBusiness;
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.CreateProviderDescriptor;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.DefaultExternalGraphic;
import org.geotoolkit.style.DefaultGraphic;
import org.geotoolkit.style.DefaultOnlineResource;
import org.geotoolkit.style.DefaultPointSymbolizer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.GraphicalSymbol;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Specific setup for map service
 *
 * @author Guilhem Legal (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
@Named
@DependsOn("database-initer")
public class SetupBusiness {

    private static final Logger LOGGER = Logging.getLogger(SetupBusiness.class);

    private static final String DEFAULT_RESOURCES = "/org/constellation/map/setup.zip";

    @Inject
    private IStyleBusiness styleBusiness;

    @Inject
    private IConfigurationBusiness configurationBusiness;

    @Inject
    @Qualifier("txManager")
    protected PlatformTransactionManager txManager;
    
    @PostConstruct
    public void contextInitialized() {
        LOGGER.log(Level.INFO, "=== Initialize Application ===");

        try {
            // Try to load postgresql driver for further use
            Class.forName("org.postgresql.ds.PGSimpleDataSource");
            LOGGER.log(Level.INFO, "postgresql loading success!");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
        
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(txManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {

                WithDefaultResources defaultResourcesDeployed = deployDefaultResources(ConfigDirectory.getDataDirectory().toPath());
                
                LOGGER.log(Level.INFO, "initializing default styles ...");
                defaultResourcesDeployed.initializeDefaultStyles();
                LOGGER.log(Level.INFO, "initializing temporary styles ...");
                defaultResourcesDeployed.initializeDefaultTempStyles();
                LOGGER.log(Level.INFO, "initializing vector data ...");
                defaultResourcesDeployed.initializeDefaultVectorData();
                LOGGER.log(Level.INFO, "initializing raster data ...");
                defaultResourcesDeployed.initializeDefaultRasterData();
                LOGGER.log(Level.INFO, "initializing properties ...");
                defaultResourcesDeployed.initializeDefaultProperties();
                
            }
        });
        
        
    }

    /**
     * Invoked when the module needs to be shutdown.
     */
    @PreDestroy
    public void contextDestroyed() {
        DataProviders.getInstance().dispose();
        StyleProviders.getInstance().dispose();
    }
    
    
    public static Path pathTransform(final Path dst, final Path zipath) {
        Path ret = dst;
        for (final Path component : zipath)
            ret = ret.resolve(component.getFileName().toString());
        return ret;
    }

    public WithDefaultResources deployDefaultResources(final Path path) {

        Path setupZip = new File(ConfigDirectory.getDataDirectory(), "setup.zip").toPath();
        try (InputStream in = SetupBusiness.class.getResourceAsStream(DEFAULT_RESOURCES)) {
            LOGGER.info("Found " + DEFAULT_RESOURCES);
            Files.copy(in, setupZip, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CstlConfigurationRuntimeException(e);
        }

        try (FileSystem fs = FileSystems.newFileSystem(setupZip, null)) {

            final Path root = fs.getPath("/");

            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Path dst = pathTransform(path, file);
                    Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    FileVisitResult result = FileVisitResult.CONTINUE;
                    Path dst = pathTransform(path, dir);
                    Files.createDirectories(dst);
                    return result;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return super.postVisitDirectory(dir, exc);
                }
            });

        } catch (IOException e) {
            throw new CstlConfigurationRuntimeException(e);
        }

        return new WithDefaultResources();

    }

    private class WithDefaultResources {
        /**
         * Initialize default styles for generic data.
         */
        private void initializeDefaultStyles() {
            // Create default SLD provider containing default styles.
            StyleProvider provider = StyleProviders.getInstance().getProvider("sld");
            final String sldPath = ConfigDirectory.getStyleDirectory().getPath();
            if (provider == null) {
                // Acquire SLD provider service instance.
                ProviderFactory sldService = null;
                for (final ProviderFactory service : StyleProviders.getInstance().getFactories()) {
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
                final ParameterDescriptorGroup sourceDesc = sldService.getProviderDescriptor();
                final ParameterValueGroup source = sourceDesc.createValue();
                source.parameter("id").setValue("sld");
                source.parameter("providerType").setValue("sld");
                source.groups("sldFolder").get(0).parameter("path").setValue(sldPath);

                // Create SLD provider.
                try {
                    final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME,
                            CreateProviderDescriptor.NAME);
                    final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue("sld");
                    inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                    desc.createProcess(inputs).call();
                } catch (NoSuchIdentifierException ignore) { // should never
                                                         // happen
                    LOGGER.log(Level.SEVERE, "4th dimension", ignore);
                } catch (ProcessException ex) {
                    LOGGER.log(Level.WARNING, "An error occurred when creating default SLD provider.", ex);
                    return;
                }

                // Retrieve created provider instance.
                provider = StyleProviders.getInstance().getProvider("sld");
            }

            final File dstImages = new File(ConfigDirectory.getDataDirectory(), "images");
            try {
                if (dstImages.exists()) {
                    if (!dstImages.isDirectory() || (dstImages.isDirectory() && dstImages.listFiles().length == 0)) {
                        final File src = FileUtilities.getDirectoryFromResource("org/constellation/map/setup/images");
                        FileUtilities.copy(src, dstImages);
                    }
                } else {
                    dstImages.mkdir();
                    final File src = FileUtilities.getDirectoryFromResource("org/constellation/map/setup/images");
                    FileUtilities.copy(src, dstImages);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            // Fill default SLD provider.
            final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(null);
            try {
                if (provider.get("default-point") == null) {
                    final MutableStyle style = SF.style(DEFAULT_POINT_SYMBOLIZER);
                    style.setName("default-point");
                    style.featureTypeStyles().get(0).rules().get(0).setName("default-point");
                    styleBusiness.createStyle("sld", style);
                }
                if (provider.get("default-point-sensor") == null) {
                    final MutableStyle style = SF.style(DEFAULT_POINT_SYMBOLIZER);
                    style.setName("default-point-sensor");
                    style.featureTypeStyles().get(0).rules().get(0).setName("default-point-sensor");

                    // Marker
                    final File markerFile = new File(dstImages, "marker_normal.png");
                    final DefaultOnlineResource onlineResource = new DefaultOnlineResource(markerFile.toURI(), "", "", "marker_normal.png",
                            null, null);
                    final DefaultExternalGraphic graphSymb = (DefaultExternalGraphic) SF.externalGraphic(onlineResource, "png", null);
                    final List<GraphicalSymbol> symbs = new ArrayList<>();
                    symbs.add(graphSymb);
                    final DefaultGraphic graphic = (DefaultGraphic) SF.graphic(symbs, null, null, null, null, null);
                    final DefaultPointSymbolizer pointSymbolizer = (DefaultPointSymbolizer) SF.pointSymbolizer("default-point-sensor", "",
                            null, null, graphic);
                    style.featureTypeStyles().get(0).rules().get(0).symbolizers().clear();
                    style.featureTypeStyles().get(0).rules().get(0).symbolizers().add(pointSymbolizer);
                    styleBusiness.createStyle("sld", style);
                }
                if (provider.get("default-point-sensor-selected") == null) {
                    final MutableStyle style = SF.style(DEFAULT_POINT_SYMBOLIZER);
                    style.setName("default-point-sensor-selected");
                    style.featureTypeStyles().get(0).rules().get(0).setName("default-point-sensor-selected");

                    // Marker
                    final File markerFile = new File(dstImages, "marker_selected.png");
                    final DefaultOnlineResource onlineResource = new DefaultOnlineResource(markerFile.toURI(), "", "",
                            "marker_selected.png", null, null);
                    final DefaultExternalGraphic graphSymb = (DefaultExternalGraphic) SF.externalGraphic(onlineResource, "png", null);
                    final List<GraphicalSymbol> symbs = new ArrayList<>();
                    symbs.add(graphSymb);
                    final DefaultGraphic graphic = (DefaultGraphic) SF.graphic(symbs, null, null, null, null, null);
                    final DefaultPointSymbolizer pointSymbolizer = (DefaultPointSymbolizer) SF.pointSymbolizer(
                            "default-point-sensor-selected", "", null, null, graphic);
                    style.featureTypeStyles().get(0).rules().get(0).symbolizers().clear();
                    style.featureTypeStyles().get(0).rules().get(0).symbolizers().add(pointSymbolizer);
                    styleBusiness.createStyle("sld", style);
                }
                if (provider.get("default-line") == null) {
                    final MutableStyle style = SF.style(DEFAULT_LINE_SYMBOLIZER);
                    style.setName("default-line");
                    style.featureTypeStyles().get(0).rules().get(0).setName("default-line");
                    styleBusiness.createStyle("sld", style);
                }
                if (provider.get("default-polygon") == null) {
                    final MutableStyle style = SF.style(DEFAULT_POLYGON_SYMBOLIZER);
                    style.setName("default-polygon");
                    style.featureTypeStyles().get(0).rules().get(0).setName("default-polygon");
                    styleBusiness.createStyle("sld", style);
                }
                if (provider.get("default-raster") == null) {
                    final MutableStyle style = SF.style(DEFAULT_RASTER_SYMBOLIZER);
                    style.setName("default-raster");
                    style.featureTypeStyles().get(0).rules().get(0).setName("default-raster");
                    styleBusiness.createStyle("sld", style);
                }
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.WARNING, "An error occurred when creating default styles for default SLD provider.", ex);
            }
        }

        /**
         * Initialize default temporary styles for generic data.
         */
        private void initializeDefaultTempStyles() {
            // Create default SLD provider containing default styles.
            StyleProvider provider = StyleProviders.getInstance().getProvider("sld_temp");
            final String sldPath = ConfigDirectory.getStyleTempDirectory().getPath();
            if (provider == null) {
                // Acquire SLD provider service instance.
                ProviderFactory sldService = null;
                for (final ProviderFactory service : StyleProviders.getInstance().getFactories()) {
                    if (service.getName().equals("sld")) {
                        sldService = service;
                        break;
                    }
                }
                if (sldService == null) {
                    LOGGER.log(Level.WARNING, "SLD temp provider service not found.");
                    return;
                }

                // Prepare create provider process inputs.
                final ParameterDescriptorGroup sourceDesc = sldService.getProviderDescriptor();
                final ParameterValueGroup source = sourceDesc.createValue();
                source.parameter("id").setValue("sld_temp");
                source.parameter("providerType").setValue("sld");
                source.groups("sldFolder").get(0).parameter("path").setValue(sldPath);

                // Create SLD provider.
                try {
                    final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME,
                            CreateProviderDescriptor.NAME);
                    final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue("sld");
                    inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                    desc.createProcess(inputs).call();
                } catch (NoSuchIdentifierException ignore) { // should never
                                                             // happen
                } catch (ProcessException ex) {
                    LOGGER.log(Level.WARNING, "An error occurred when creating default SLD temp provider.", ex);
                }
            }

        }

        /**
         * Initialize default vector data for displaying generic features in
         * data editors.
         */
        private void initializeDefaultVectorData() {
            final File dst = new File(ConfigDirectory.getDataDirectory(), "shapes");
            try {

                final String featureStoreStr = "feature-store";
                final String shpProvName = "generic_shp";
                DataProvider shpProvider = DataProviders.getInstance().getProvider(shpProvName);
                if (shpProvider == null) {
                    // Acquire SHP provider service instance.
                    ProviderFactory shpService = null;
                    for (final ProviderFactory service : DataProviders.getInstance().getFactories()) {
                        if (service.getName().equals(featureStoreStr)) {
                            shpService = service;
                            break;
                        }
                    }
                    if (shpService == null) {
                        LOGGER.log(Level.WARNING, "SHP provider service not found.");
                        return;
                    }

                    final ParameterValueGroup source = shpService.getProviderDescriptor().createValue();
                    getOrCreateValue(source, "id").setValue(shpProvName);
                    getOrCreateValue(source, "load_all").setValue(true);
                    getOrCreateValue(source, "providerType").setValue("vector");

                    final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                    final ParameterValueGroup shpConfig = createGroup(choice, "ShapefileParametersFolder");
                    getOrCreateValue(shpConfig, "url").setValue(dst.toURI().toURL());
                    getOrCreateValue(shpConfig, "namespace").setValue("no namespace");

                    // Create SHP Folder provider.
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME,
                                CreateProviderDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue(featureStoreStr);
                        inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                        desc.createProcess(inputs).call();
                    } catch (NoSuchIdentifierException ignore) { // should never
                                                                 // happen
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
         * Initialize default raster data for displaying generic features in
         * data editors.
         */
        private void initializeDefaultRasterData() {
            final File dst = new File(ConfigDirectory.getDataDirectory(), "raster");
            try {

                final String coverageFileStr = "coverage-store";
                final String tifProvName = "generic_world_tif";
                DataProvider tifProvider = DataProviders.getInstance().getProvider(tifProvName);
                if (tifProvider == null) {
                    // Acquire TIFF provider service instance.
                    ProviderFactory tifService = null;
                    for (final ProviderFactory service : DataProviders.getInstance().getFactories()) {
                        if (service.getName().equals(coverageFileStr)) {
                            tifService = service;
                            break;
                        }
                    }
                    if (tifService == null) {
                        LOGGER.log(Level.WARNING, "TIFF provider service not found.");
                        return;
                    }

                    final ParameterValueGroup source = tifService.getProviderDescriptor().createValue();
                    getOrCreateValue(source, "id").setValue(tifProvName);
                    getOrCreateValue(source, "load_all").setValue(true);
                    getOrCreateValue(source, "providerType").setValue("raster");

                    final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                    final ParameterValueGroup tifConfig = createGroup(choice, "FileCoverageStoreParameters");
                    final File dstTif = new File(dst, "cloudsgrey.tiff");
                    getOrCreateValue(tifConfig, "path").setValue(dstTif.toURI().toURL());
                    getOrCreateValue(tifConfig, "namespace").setValue("no namespace");

                    // Create SHP Folder provider.
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME,
                                CreateProviderDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue(coverageFileStr);
                        inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(source);
                        desc.createProcess(inputs).call();
                    } catch (NoSuchIdentifierException ignore) { // should never
                                                                 // happen
                    } catch (ProcessException ex) {
                        LOGGER.log(Level.WARNING, "An error occurred when creating default TIFF provider.", ex);
                        return;
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }

        /**
         * Initialize default properties values if not exist.
         */
        private void initializeDefaultProperties() {
            String dataAnalyse = configurationBusiness.getProperty(PropertyConstants.DATA_ANALYSE_KEY);
            if (dataAnalyse == null) {
                LOGGER.log(Level.FINE, "Property " + PropertyConstants.DATA_ANALYSE_KEY + "set as : true");
                configurationBusiness.setProperty(PropertyConstants.DATA_ANALYSE_KEY, "true");
            }
        }

    }

    

}
