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
 * limitations under the License..
 */

package org.constellation.admin.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.collection.TreeTable;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.engine.template.TemplateEngine;
import org.constellation.engine.template.TemplateEngineException;
import org.constellation.engine.template.TemplateEngineFactory;
import org.constellation.provider.DataProvider;
import org.constellation.util.MetadataMapBuilder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.constellation.util.Util;
import org.constellation.utils.CstlMetadatas;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.util.FileUtilities;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.util.GenericName;
import org.opengis.util.NoSuchIdentifierException;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.constellation.database.api.jooq.tables.pojos.MetadataBbox;
import org.geotoolkit.processing.metadata.MetadataProcessingRegistry;
import org.geotoolkit.processing.metadata.merge.MergeDescriptor;
import org.geotoolkit.storage.coverage.CoverageReference;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.Identification;
import org.apache.sis.util.logging.Logging;


/**
 * Utility class to do some operation on metadata file (generate, revover, ...)
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public final class MetadataUtilities {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.admin.util");

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");



    public static ArrayList<SimplyMetadataTreeNode> getSensorInformations(final String fileName, final ObservationStore store) {
        final ArrayList<SimplyMetadataTreeNode> results = new ArrayList<>();
        final SimplyMetadataTreeNode root = new SimplyMetadataTreeNode(fileName, true, "root", 11, null);
        results.add(root);

        final SimplyMetadataTreeNode procedures = new SimplyMetadataTreeNode("Procedures:", true, "procedures", 10, "root");
        results.add(procedures);
        int i = 0;
        for (GenericName procedure : store.getProcedureNames()) {
            final SimplyMetadataTreeNode procNode = new SimplyMetadataTreeNode(procedure.tip().toString(), false, "proc" + i, 9, "procedures");
            procNode.setValue(procedure.tip().toString());
            results.add(procNode);
        }

        final SimplyMetadataTreeNode variables = new SimplyMetadataTreeNode("Variables:", true, "variables", 10, "root");
        results.add(variables);
        i = 0;
        for (String phenomenon : store.getPhenomenonNames()) {
            final SimplyMetadataTreeNode phenNode = new SimplyMetadataTreeNode(phenomenon, false, "phen" + i, 9, "variables");
            phenNode.setValue(phenomenon);
            results.add(phenNode);
            i++;
        }

        final SimplyMetadataTreeNode times = new SimplyMetadataTreeNode("Temporal bounds:", true, "times", 10, "root");
        results.add(times);
        try {
            final TemporalGeometricPrimitive time = store.getTemporalBounds();

            if (time instanceof Period) {
                final Period period = (Period) time;
                final SimplyMetadataTreeNode beginNode = new SimplyMetadataTreeNode("Begin position", false, "time-begin", 9, "times");
                synchronized(FORMAT) {
                    beginNode.setValue(FORMAT.format(period.getBeginning().getDate()));
                }
                results.add(beginNode);
                final SimplyMetadataTreeNode endNode = new SimplyMetadataTreeNode("End position", false, "time-end", 9, "times");
                synchronized(FORMAT) {
                    endNode.setValue(FORMAT.format(period.getEnding().getDate()));
                }
                results.add(endNode);
            } else if (time instanceof Instant) {
                final Instant instant = (Instant) time;
                final SimplyMetadataTreeNode beginNode = new SimplyMetadataTreeNode("Position", false, "time-position", 9, "times");
                synchronized(FORMAT) {
                    beginNode.setValue(FORMAT.format(instant.getDate()));
                }
                results.add(beginNode);

            } else {
                final SimplyMetadataTreeNode timeNode = new SimplyMetadataTreeNode("Undefined", false, "time-undef", 9, "times");
                timeNode.setValue("Undefined");
                results.add(timeNode);
            }
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, "Error while retrieving temporal data in dataStore", ex);
            final SimplyMetadataTreeNode timeNode = new SimplyMetadataTreeNode("Error", false, "time-error", 9, "times");
            timeNode.setValue("Error");
            results.add(timeNode);
        }
        return results;
    }

    /**
     * @param coverageReader
     * @param metadata
     * @param dataType (raster, vector, ...)
     * @return a {@link org.constellation.dto.DataInformation} from data file
     * @throws CoverageStoreException
     * @throws org.opengis.util.NoSuchIdentifierException
     * @throws org.geotoolkit.process.ProcessException
     */
    public static DataInformation getRasterDataInformation(final GridCoverageReader coverageReader, final DefaultMetadata metadata, final String dataType) throws CoverageStoreException, NoSuchIdentifierException, ProcessException {

        final CoordinateReferenceSystem crs;
        try {
            crs = coverageReader.getGridGeometry(0).getCoordinateReferenceSystem();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Invalid coverage information", ex);
            return new DataInformation("error-data-import-no-geographic");
        }

        if (!(crs instanceof ImageCRS)) {

            // get Metadata as a List
            final DefaultMetadata fileMetadata = (DefaultMetadata) coverageReader.getMetadata();
            DefaultMetadata finalMetadata = null;

            if (metadata != null) {
                finalMetadata = (DefaultMetadata) mergeMetadata(fileMetadata, metadata);
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
            return new DataInformation("error-data-import-no-geographic");
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
                if (!(rootNodeName ==null)) {
                    final Node coverageRootNode = sm.getAsTree(rootNodeName);

                    MetadataMapBuilder.setCounter(0);
                    final List<SimplyMetadataTreeNode> coverageMetadataList = MetadataMapBuilder.createSpatialMetadataList(coverageRootNode, null, 11, i);

                    final CoverageMetadataBean coverageMetadataBean = new CoverageMetadataBean(coverageMetadataList);
                    nameSpatialMetadataMap.put(name.toString(), coverageMetadataBean);
                }
            }
        }

        //update DataInformation
        information.setCoveragesMetadata(nameSpatialMetadataMap);
    }

    /**
     * @param dataProvider
     * @param dataName
     * @return
     * @throws DataStoreException
     */
    public static DefaultMetadata getRasterMetadata(final DataProvider dataProvider, final GenericName dataName) throws DataStoreException {

    	final DataStore dataStore = dataProvider.getMainStore();
    	final CoverageStore coverageStore = (CoverageStore) dataStore;
    	final CoverageReference coverageReference = coverageStore.getCoverageReference(dataName);
        if (coverageReference != null) {
            final GridCoverageReader coverageReader = coverageReference.acquireReader();
            try {
                return (DefaultMetadata) coverageReader.getMetadata();
            } finally {
                coverageReference.recycle(coverageReader);
            }
        }
        return null;
    }

    /**
     * Returns the raster metadata for entire dataset referenced by given provider.
     * @param dataProvider the given data provider
     * @return {@code DefaultMetadata}
     * @throws DataStoreException
     */
    public static DefaultMetadata getRasterMetadata(final DataProvider dataProvider) throws DataStoreException {

        final DataStore dataStore = dataProvider.getMainStore();
        final CoverageStore coverageStore = (CoverageStore) dataStore;
        DefaultMetadata coverageMetadata =  (DefaultMetadata) coverageStore.getMetadata();
        if(coverageMetadata!=null)return coverageMetadata;

        //if the coverage metadata still null that means it is not implemented yet
        // so we return the metadata iso from the reader
        final Set<GenericName> names= coverageStore.getNames();
        DefaultMetadata metadata = new DefaultMetadata();
        if(names != null){
            for(final GenericName n : names){
                final CoverageReference cr = coverageStore.getCoverageReference(n);
                final GridCoverageReader reader = cr.acquireReader();
                try {
                    final Metadata meta = reader.getMetadata();
                    //@FIXME
                    // this merge is bad here to build a fully dataset
                    // metadata that should contains all data children information
                    //see issue JIRA CSTL-1151
                    metadata = mergeMetadata(metadata,(DefaultMetadata)meta);
                }catch(Exception ex){
                    LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                } finally{
                    cr.recycle(reader);
                }
            }
        }
        return metadata;
    }

    /**
     * Returns crs name if possible for raster data.
     * @param dataProvider
     * @return
     * @throws DataStoreException
     */
    public static String getRasterCRSName(final DataProvider dataProvider) throws DataStoreException {
        final DataStore dataStore = dataProvider.getMainStore();
        final CoverageStore coverageStore = (CoverageStore) dataStore;
        final Set<GenericName> names= coverageStore.getNames();
        CoordinateReferenceSystem candidat = null;
        if(names != null){
            for(final GenericName n : names){
                try{
                    final CoverageReference cr = coverageStore.getCoverageReference(n);
                    final GridCoverageReader reader = cr.acquireReader();
                    final GeneralGridGeometry gridGeom = reader.getGridGeometry(cr.getImageIndex());
                    final CoordinateReferenceSystem crs = gridGeom.getCoordinateReferenceSystem();
                    cr.recycle(reader);
                    if(candidat == null && crs != null){
                        candidat = crs;
                    }
                    final String crsIdentifier = IdentifiedObjects.lookupIdentifier(crs,true);
                    if(crsIdentifier != null){
                        return crsIdentifier;
                    }
                }catch(Exception ex){
                    LOGGER.finer(ex.getMessage());
                }
            }
        }
        if(candidat != null && candidat.getName() != null){
            return candidat.getName().toString();
        }
        return null;
    }

    /**
     * Returns crs name for vector data if possible.
     * @param dataProvider
     * @return
     * @throws DataStoreException
     */
    public static String getVectorCRSName(final DataProvider dataProvider) throws DataStoreException {
        final DataStore dataStore = dataProvider.getMainStore();
        final FeatureStore featureStore = (FeatureStore) dataStore;
        CoordinateReferenceSystem candidat = null;
        for (GenericName dataName : featureStore.getNames()) {
            Envelope env = featureStore.getEnvelope(QueryBuilder.all(dataName));
            if (env == null) {
                continue;
            }
            final CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
            if(candidat == null && crs != null){
                candidat = crs;
            }
            try{
                final String crsIdentifier = IdentifiedObjects.lookupIdentifier(crs,true);
                if(crsIdentifier != null){
                    return crsIdentifier;
                }
            }catch(Exception ex){
                LOGGER.finer(ex.getMessage());
            }
        }
        if(candidat != null && candidat.getName() != null){
            return candidat.getName().toString();
        }
        return null;
    }

    /**
     * @param dataProvider
     * @param dataName
     * @return
     * @throws DataStoreException
     */
    public static DefaultMetadata getVectorMetadata(final DataProvider dataProvider, final GenericName dataName) throws DataStoreException, TransformException {

    	final DataStore dataStore = dataProvider.getMainStore();
    	final FeatureStore featureStore = (FeatureStore) dataStore;


        final DefaultMetadata md = new DefaultMetadata();
        final DefaultDataIdentification ident = new DefaultDataIdentification();
        md.getIdentificationInfo().add(ident);

        Envelope env = featureStore.getEnvelope(QueryBuilder.all(dataName));
        if (env == null) {
            return md;
        }

        env = CRS.transform(env, CommonCRS.WGS84.normalizedGeographic());
        final DefaultGeographicBoundingBox bbox = new DefaultGeographicBoundingBox(
                env.getMinimum(0), env.getMaximum(0), env.getMinimum(1), env.getMaximum(1)
        );
        final DefaultExtent extent = new DefaultExtent("", bbox, null, null);
        ident.getExtents().add(extent);
        return md;
    }

    public static DefaultMetadata getVectorMetadata(final DataProvider dataProvider) throws DataStoreException, TransformException {

    	final DataStore dataStore = dataProvider.getMainStore();
    	final FeatureStore featureStore = (FeatureStore) dataStore;


        final DefaultMetadata md = new DefaultMetadata();
        final DefaultDataIdentification ident = new DefaultDataIdentification();
        md.getIdentificationInfo().add(ident);
        DefaultGeographicBoundingBox bbox = null;
        for (GenericName dataName : featureStore.getNames()) {
            Envelope env = featureStore.getEnvelope(QueryBuilder.all(dataName));
            if (env == null) {
                continue;
            }
            final DefaultGeographicBoundingBox databbox = new DefaultGeographicBoundingBox();
            databbox.setBounds(env);
            if (bbox == null) {
                bbox = databbox;
            } else {
                bbox.add(databbox);
            }
        }
        final DefaultExtent extent = new DefaultExtent("", bbox, null, null);
        ident.getExtents().add(extent);
        return md;
    }

    /**
     * @param fileMetadata
     * @param metadataToMerge
     *
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    public static DefaultMetadata mergeMetadata(final DefaultMetadata fileMetadata, final DefaultMetadata metadataToMerge) throws NoSuchIdentifierException, ProcessException {
        // call Merge Process
        DefaultMetadata resultMetadata;
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(MetadataProcessingRegistry.NAME, MergeDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(MergeDescriptor.FIRST_IN_NAME).setValue(fileMetadata);
        inputs.parameter(MergeDescriptor.SECOND_IN_NAME).setValue(metadataToMerge);
        final org.geotoolkit.process.Process mergeProcess = desc.createProcess(inputs);
        final ParameterValueGroup resultParameters = mergeProcess.call();

        resultMetadata = (DefaultMetadata) resultParameters.parameter(MergeDescriptor.RESULT_OUT_NAME).getValue();
        return resultMetadata;
    }

    public static void merge(final MetadataStandard standard, final Object sourceMetadata, final Object targetMetadata) {
        //transfomr metadatas to maps
        final Map<String, Object> source = standard.asValueMap(sourceMetadata, KeyNamePolicy.JAVABEANS_PROPERTY, ValueExistencePolicy.NON_EMPTY);
        final Map<String, Object> target = standard.asValueMap(targetMetadata, KeyNamePolicy.JAVABEANS_PROPERTY, ValueExistencePolicy.ALL);

        //Iterate on sources to found object which need to be merged
        for (final Map.Entry<String, Object> entry : source.entrySet()) {
            //
            final String propertyName = entry.getKey();
            final Object sourceValue = entry.getValue();
            final Object targetValue = target.get(propertyName);

            //directly put if value is null on targer (they don't need merge)
            if (targetValue == null) {
                target.put(propertyName, sourceValue);
            } else {
                //if it's metadata object (DefaultMetadata, Extent, ...)
                if (standard.isMetadata(targetValue.getClass())) {
                    merge(standard, sourceValue, targetValue);
                } else {
                    //targetValue is a Collection
                    if(targetValue instanceof Collection){
                        Collection targetList = ((Collection) targetValue);
                        Collection sourceList = ((Collection) sourceValue);
                        //recursively merge
                        if (targetList.size() > 0) {
                            for (Object mergeElement : targetList) {
                                for (Object sourceElement : sourceList) {
                                    if (mergeElement.getClass().equals(sourceElement.getClass()) && standard.isMetadata(mergeElement.getClass())) {
                                        merge(standard, sourceElement, mergeElement);
                                    }
                                }
                            }
                        } else {
                            //list is empty on target : we add all other collection without merge
                            ((Collection) targetValue).addAll((Collection) sourceValue);
                        }
                    }
                }
            }
        }
    }

    public static String getTemplateMetadata(final Properties prop, final String templatePath) {
        try {
            final TemplateEngine templateEngine = TemplateEngineFactory.getInstance(TemplateEngineFactory.GROOVY_TEMPLATE_ENGINE);
            final InputStream stream = Util.getResourceAsStream(templatePath);
            final File templateFile = File.createTempFile("mdTemplDataset", ".xml");
            FileUtilities.buildFileFromStream(stream, templateFile);
            return templateEngine.apply(templateFile, prop);
        } catch (TemplateEngineException | IOException ex) {
           LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    public static void overrideProperties(final Properties prop, final DataMetadata overridenValue, final GenericName dataName, final String formatName) {
        final String metadataId = CstlMetadatas.getMetadataIdForData(overridenValue.getDataName(), dataName);
        prop.put("fileId", metadataId);
        if (overridenValue.getAnAbstract() != null) {
            prop.put("dataAbstract", overridenValue.getAnAbstract());
        } else if (prop.get("dataAbstract") == null) {
            prop.put("dataAbstract", metadataId);
        }
        final String currentDate;
        synchronized (FORMAT){
            currentDate = FORMAT.format(new Date(System.currentTimeMillis()));
        }

        final Date overridingDate = overridenValue.getDate();
        if (overridingDate != null) {
            final String date;
            synchronized (FORMAT){
                date = FORMAT.format(overridingDate);
            }
            final String dateType = overridenValue.getDateType();
            if (dateType != null) {
                switch (dateType) {
                    case "Creation"    : prop.put("creationDate", date);break;
                    case "Revision"    : prop.put("revisionDate", date);break;
                    case "Publication" : prop.put("publicationDate", date);break;
                }
            } else {
                prop.put("creationDate", date);
            }
        } else {
            prop.put("publicationDate", currentDate);
        }

        prop.put("isoCreationDate", currentDate);
        prop.put("distributionFormat", formatName);

        if (overridenValue.getKeywords() != null) {
            prop.put("keywords", overridenValue.getKeywords());
        }

        final String localeData = overridenValue.getLocaleData();
        if (localeData != null) {
            try {
                final String[] localeAndCountry = localeData.split("_");
                Locale dataLocale;
                if (localeAndCountry.length == 2) {
                    dataLocale = new Locale(localeAndCountry[0], localeAndCountry[1]);
                } else {
                    dataLocale = new Locale(localeAndCountry[0]);
                }
                prop.put("dataLocale", dataLocale.getISO3Language());
            } catch (MissingResourceException ex) {
                LOGGER.warning(ex.getMessage());
                prop.put("dataLocale", "eng");
            }
        } else {
            prop.put("dataLocale", "eng");
        }
        if (overridenValue.getOrganisationName() != null) {
            prop.put("organisationName", overridenValue.getOrganisationName());
        }
        if (overridenValue.getRole() != null) {
            prop.put("role", buildProperCode(overridenValue.getRole()));
        }
        if (overridenValue.getTitle() != null) {
            prop.put("dataTitle", overridenValue.getTitle());
        } else if (prop.get("dataTitle") == null) {
            prop.put("dataTitle", metadataId);
        }
        final String topic = overridenValue.getTopicCategory();
        if (topic != null) {
            prop.put("topicCategory", buildProperCode(topic));
        }
        if (overridenValue.getUsername() != null) {
            prop.put("contactName", overridenValue.getUsername());
        }
    }

    private static String buildProperCode(final String codeValue) {
        final String[] parts = codeValue.split(" ");
        final StringBuilder sb = new StringBuilder();
        for (String s : parts) {
            sb.append(StringUtils.capitalize(s));
        }
        return sb.toString();
    }

    public static Long extractDatestamp(final DefaultMetadata metadata){
        if (metadata.getDateStamp() != null) {
            return metadata.getDateStamp().getTime();
        }
        return null;
    }

    public static String extractTitle(final DefaultMetadata metadata){
        if (metadata.getIdentificationInfo() != null && !metadata.getIdentificationInfo().isEmpty()) {
            final Identification id = metadata.getIdentificationInfo().iterator().next();
            if (id.getCitation() != null && id.getCitation().getTitle() != null) {
                return id.getCitation().getTitle().toString();
            }
        }
        return null;
    }

    public static String extractResume(final DefaultMetadata metadata){
        if (metadata.getIdentificationInfo() != null && !metadata.getIdentificationInfo().isEmpty()) {
            final Identification id = metadata.getIdentificationInfo().iterator().next();
            if (id.getAbstract() != null) {
                return id.getAbstract().toString();
            }
        }
        return null;
    }

    public static List<MetadataBbox> extractBbox(final DefaultMetadata metadata){
        final List<MetadataBbox> results = new ArrayList<>();
        if (metadata.getIdentificationInfo() != null && !metadata.getIdentificationInfo().isEmpty()) {
            final Identification id = metadata.getIdentificationInfo().iterator().next();
            for (Extent ex : id.getExtents()) {
                for (GeographicExtent geoEx : ex.getGeographicElements()) {
                    if (geoEx instanceof GeographicBoundingBox) {
                        GeographicBoundingBox geobox = (GeographicBoundingBox) geoEx;
                        final MetadataBbox bbox = new MetadataBbox(null, geobox.getEastBoundLongitude(),
                                                                         geobox.getWestBoundLongitude(),
                                                                         geobox.getNorthBoundLatitude(),
                                                                         geobox.getSouthBoundLatitude());
                        results.add(bbox);
                    }
                }
            }
        }
        return results;
    }

    public static String extractParent(final DefaultMetadata metadata){
        return metadata.getParentIdentifier();
    }
}
