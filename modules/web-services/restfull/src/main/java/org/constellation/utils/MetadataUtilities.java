/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2009-2013, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */

package org.constellation.utils;

import com.google.common.io.Files;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.collection.TreeTable;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.util.MetadataMapBuilder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.metadata.MetadataProcessingRegistry;
import org.geotoolkit.process.metadata.merge.MergeDescriptor;
import org.geotoolkit.util.FileUtilities;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.util.GenericName;
import org.opengis.util.NoSuchIdentifierException;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Utility class to do some operation on metadata file (generate, revover, ...)
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public final class MetadataUtilities {

    private static final Logger LOGGER = Logger.getLogger(MetadataUtilities.class.getName());

    /**
     * Generate {@link org.constellation.dto.DataInformation} for require file data
     *
     * @param file         data {@link java.io.File}
     * @param metadataFile
     * @param dataType     @return a {@link org.constellation.dto.DataInformation}
     */
    public static DataInformation generateMetadatasInformation(final File file, final File metadataFile, final String dataType) {

        final Unmarshaller xmlReader;
        DefaultMetadata templateMetadata = null;

        try {
            xmlReader = CSWMarshallerPool.getInstance().acquireUnmarshaller();
            if (metadataFile != null) {
                templateMetadata = (DefaultMetadata) xmlReader.unmarshal(metadataFile);
            }
        } catch (JAXBException e) {
            LOGGER.log(Level.WARNING, "", e);
        }

        switch (dataType) {
            case "raster":
                try {
                    final GridCoverageReader coverageReader = CoverageIO.createSimpleReader(file);
                    final DataInformation di = getRasterDataInformation(coverageReader, templateMetadata, dataType);
                    di.setPath(file.getPath());
                    return di;
                } catch (CoverageStoreException | NoSuchIdentifierException | ProcessException | JAXBException e) {
                    LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                }
                break;
            case "vector":
                try {
                    //unzip file
                    String extension = Files.getFileExtension(file.getName());
                    String fileName = Files.getNameWithoutExtension(file.getName());
                    File parent = new File(file.getParent(), fileName);
                    ShapefileFeatureStore shapeStore = null;
                    if (extension.equalsIgnoreCase("zip")) {
                        parent.mkdirs();
                        FileUtilities.unzip(file, parent, null);
                        final FileFilter shapeFilter = new SuffixFileFilter(".shp");
                        final File[] files = parent.listFiles(shapeFilter);
                        if (files.length > 0) {
                            shapeStore = new ShapefileFeatureStore(files[0].toURI().toURL());
                        }
                    }

                    String crsName = "";
                    final CoordinateReferenceSystem crs = shapeStore.getFeatureType().getCoordinateReferenceSystem();
                    if (crs != null) {
                        crsName = crs.getName().toString();
                    }
                    final DataInformation information = new DataInformation(shapeStore.getName().getLocalPart(), parent.getAbsolutePath(), dataType, crsName);
                    final ArrayList<SimplyMetadataTreeNode> metadataList = getVectorDataInformation(templateMetadata);
                    information.setFileMetadata(metadataList);
                    return information;

                } catch (MalformedURLException e) {
                    LOGGER.log(Level.WARNING, "error on file URL", e);
                } catch (DataStoreException e) {
                    LOGGER.log(Level.WARNING, "error on data store creation", e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "error on un zip", e);
                }
                break;
        }
        return null;
    }


    /**
     * @param metadata
     * @param dataType (raster, vector, ...)  @return a {@link org.constellation.dto.DataInformation} from data file
     * @throws CoverageStoreException
     */
    public static DataInformation getRasterDataInformation(final GridCoverageReader coverageReader, final DefaultMetadata metadata, final String dataType) throws CoverageStoreException, NoSuchIdentifierException, ProcessException, JAXBException {

        CoordinateReferenceSystem cRs = coverageReader.getGridGeometry(0).getCoordinateReferenceSystem();

        if (!(cRs instanceof ImageCRS)) {

            // get Metadata as a List
            final DefaultMetadata fileMetadata = (DefaultMetadata) coverageReader.getMetadata();
            DefaultMetadata finalMetadata = null;
            //mergeTemplate(fileMetadata);
            if (metadata != null) {
                finalMetadata = (DefaultMetadata) mergeTemplate(fileMetadata, metadata);
            }

            final TreeTable.Node rootNode;
            if (finalMetadata != null) {
                rootNode = finalMetadata.asTreeTable().getRoot();
            } else {
                rootNode = fileMetadata.asTreeTable().getRoot();
            }

            MetadataMapBuilder.setCounter(0);
            final ArrayList<SimplyMetadataTreeNode> metadataList = MetadataMapBuilder.createMetadataList(rootNode, null, 11);

            final DataInformation information = new DataInformation(null, dataType, metadataList);
            addCoverageData(coverageReader, information);

            return information;
        } else {
            return new DataInformation();
        }
    }

    /**
     * @param metadata
     * @return
     */
    public static ArrayList<SimplyMetadataTreeNode> getVectorDataInformation(final DefaultMetadata metadata) {
        if (metadata != null) {
            // get Metadata as a List
            final TreeTable.Node rootNode;
            rootNode = metadata.asTreeTable().getRoot();

            MetadataMapBuilder.setCounter(0);
            return MetadataMapBuilder.createMetadataList(rootNode, null, 11);
        }
        return new ArrayList<>(0);
    }

    /**
     * Update information param with coverage metadata
     *
     * @param coverageReader Contains coverages which want to extract metadata
     * @param information    {@link org.constellation.dto.DataInformation} which be updated
     * @throws CoverageStoreException if we can't extract information from coverageReader
     */
    private static void addCoverageData(final GridCoverageReader coverageReader, final DataInformation information) throws CoverageStoreException {
        final Map<String, CoverageMetadataBean> nameSpatialMetadataMap = new HashMap<>(0);

        //iterate on picture coverages
        for (int i = 0; i < coverageReader.getCoverageNames().size(); i++) {

            //build metadata
            final GenericName name = coverageReader.getCoverageNames().get(i);
            final SpatialMetadata sm = coverageReader.getCoverageMetadata(i);
            if (sm != null) {

                final String rootNodeName = sm.getNativeMetadataFormatName();
                final Node coverateRootNode = sm.getAsTree(rootNodeName);

                MetadataMapBuilder.setCounter(0);
                final List<SimplyMetadataTreeNode> coverageMetadataList = MetadataMapBuilder.createSpatialMetadataList(coverateRootNode, null, 11, i);

                final CoverageMetadataBean coverageMetadataBean = new CoverageMetadataBean(coverageMetadataList);
                nameSpatialMetadataMap.put(name.toString(), coverageMetadataBean);
            }

        }

        //update DataInformation
        information.setCoveragesMetadata(nameSpatialMetadataMap);
    }

    /**
     * @param metadataToSave
     * @return
     * @throws CoverageStoreException
     */
    public static DefaultMetadata getRasterMetadata(final DataMetadata metadataToSave) throws CoverageStoreException {

        final File dataFile = new File(metadataToSave.getDataPath());
        GridCoverageReader coverageReader = CoverageIO.createSimpleReader(dataFile);
        if (!(coverageReader.getGridGeometry(0).getCoordinateReferenceSystem() instanceof ImageCRS)) {
            return (DefaultMetadata) coverageReader.getMetadata();
        }
        return null;
    }

    /**
     * @param fileMetadata
     * @return
     * @throws JAXBException
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    public static Metadata mergeTemplate(final DefaultMetadata fileMetadata, final DefaultMetadata metadataToMerge) throws JAXBException, NoSuchIdentifierException, ProcessException {
        // unmarshall metadataFile Template

        // call Merge Process
        DefaultMetadata resultMetadata;
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(MetadataProcessingRegistry.NAME, MergeDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(MergeDescriptor.FIRST_IN_NAME).setValue(fileMetadata);
        inputs.parameter(MergeDescriptor.SECOND_IN_NAME).setValue(metadataToMerge);
        final org.geotoolkit.process.Process mergeProcess = desc.createProcess(inputs);
        final ParameterValueGroup resultParameters = mergeProcess.call();

        resultMetadata = (DefaultMetadata) resultParameters.parameter(MergeDescriptor.RESULT_OUT_NAME).getValue();
        ;
        return resultMetadata;
    }
}
