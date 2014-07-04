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

package org.constellation.sos.io.filesystem;

// J2SE dependencies

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.lucene.LuceneObservationIndexer;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.observation.ObservationWriter;
import org.geotoolkit.sampling.xml.SamplingFeature;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.swe.xml.Phenomenon;
import org.geotoolkit.swes.xml.ObservationTemplate;
import org.opengis.observation.Observation;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileObservationWriter implements ObservationWriter {

    private File offeringDirectory;

    private File phenomenonDirectory;

    private File observationDirectory;

    private File observationTemplateDirectory;

    //private File sensorDirectory;

    private File foiDirectory;

    //private File resultDirectory;

    private static final MarshallerPool MARSHALLER_POOL;
    static {
        MARSHALLER_POOL = SOSMarshallerPool.getInstance();
    }

    private LuceneObservationIndexer indexer;

    private final String observationTemplateIdBase;

    private static final String FILE_EXTENSION = ".xml";

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos.io.filesystem");

    public FileObservationWriter(final Automatic configuration,  final Map<String, Object> properties) throws DataStoreException {
        super();
        this.observationTemplateIdBase = (String) properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
        final File dataDirectory = configuration.getDataDirectory();
        if (dataDirectory.exists()) {
            offeringDirectory    = new File(dataDirectory, "offerings");
            phenomenonDirectory  = new File(dataDirectory, "phenomenons");
            observationDirectory = new File(dataDirectory, "observations");
            //sensorDirectory      = new File(dataDirectory, "sensors");
            foiDirectory         = new File(dataDirectory, "features");
            //resultDirectory      = new File(dataDirectory, "results");
            observationTemplateDirectory = new File(dataDirectory, "observationTemplates");

        }
        if (MARSHALLER_POOL == null) {
            throw new DataStoreException("JAXB exception while initializing the file observation reader");
        }
        try {
            indexer        = new LuceneObservationIndexer(configuration, "", true);
        } catch (IndexingException ex) {
            throw new DataStoreException("Indexing exception while initializing the file observation reader", ex);
        }


    }

    @Override
    public String writeObservationTemplate(final ObservationTemplate template) throws DataStoreException {
        final Observation observation = template.getObservation();
        if (observation == null) {
            return null;
        }
        try {
            final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
            final File observationFile = new File(observationTemplateDirectory, observation.getName() + FILE_EXTENSION);
            
            if (observationFile.exists()) {
                final boolean created      = observationFile.createNewFile();
                if (!created) {
                    throw new DataStoreException("unable to create an observation file.");
                }
            } else {
                LOGGER.log(Level.WARNING, "we overwrite the file:{0}", observationFile.getPath());
            }
            
            marshaller.marshal(observation, observationFile);
            MARSHALLER_POOL.recycle(marshaller);
            writePhenomenon((Phenomenon) observation.getObservedProperty());
            if (observation.getFeatureOfInterest() !=  null) {
                writeFeatureOfInterest((SamplingFeature) observation.getFeatureOfInterest());
            }
            indexer.indexDocument(observation);
            return observation.getName();
        } catch (JAXBException | IOException ex) {
            throw new DataStoreException("Exception while marshalling the observation file.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final Observation observation) throws DataStoreException {
        try {
            final File observationFile;
            if (observation.getName().startsWith(observationTemplateIdBase)) {
                observationFile = new File(observationTemplateDirectory, observation.getName() + FILE_EXTENSION);
            } else {
                observationFile = new File(observationDirectory, observation.getName() + FILE_EXTENSION);
            }
            if (observationFile.exists()) {
                final boolean created      = observationFile.createNewFile();
                if (!created) {
                    throw new DataStoreException("unable to create an observation file:" + observationFile.getName());
                }
            } else {
                LOGGER.log(Level.WARNING, "we overwrite the file:{0}", observationFile.getPath());
            }
            
            final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
            marshaller.marshal(observation, observationFile);
            MARSHALLER_POOL.recycle(marshaller);
            
            writePhenomenon((Phenomenon) observation.getObservedProperty());
            if (observation.getFeatureOfInterest() !=  null) {
                writeFeatureOfInterest((SamplingFeature) observation.getFeatureOfInterest());
            }
            indexer.indexDocument(observation);
            return observation.getName();
        } catch (JAXBException | IOException ex) {
            throw new DataStoreException("Exception while marshalling the observation file.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> writeObservations(List<Observation> observations) throws DataStoreException {
        final List<String> results = new ArrayList<>();
        for (Observation observation : observations) {
            final String oid = writeObservation(observation);
            results.add(oid);
        }
        return results;
    }

    @Override
    public void removeObservation(final String observationID) throws DataStoreException {
        final File observationFile;
        if (observationID.startsWith(observationTemplateIdBase)) {
            observationFile = new File(observationTemplateDirectory, observationID + FILE_EXTENSION);
        } else {
            observationFile = new File(observationDirectory, observationID + FILE_EXTENSION);
        }
        if (observationFile.exists()) {
            observationFile.delete();
        } else {
            LOGGER.log(Level.WARNING, "unable to find t he fiel to delete:{0}", observationFile.getPath());
        }
    }
    
    @Override
    public void removeObservationForProcedure(final String procedureID) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    @Override
    public void removeProcedure(final String procedureID) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    private void writePhenomenon(final Phenomenon phenomenon) throws DataStoreException {
        try {
            if (!phenomenonDirectory.exists()) {
                phenomenonDirectory.mkdir();
            }
            final File phenomenonFile = new File(phenomenonDirectory, phenomenon.getName() + FILE_EXTENSION);
            if (!phenomenonFile.exists()) {
                final boolean created = phenomenonFile.createNewFile();
                if (!created) {
                    throw new DataStoreException("unable to create a phenomenon file.");
                }
                final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
                marshaller.marshal(phenomenon, phenomenonFile);
                MARSHALLER_POOL.recycle(marshaller);
            }
        } catch (JAXBException ex) {
            throw new DataStoreException("JAXB exception while marshalling the phenomenon file.", ex);
        } catch (IOException ex) {
            throw new DataStoreException("IO exception while marshalling the phenomenon file.", ex);
        }
    }

    private void writeFeatureOfInterest(final SamplingFeature foi) throws DataStoreException {
        try {
            
            if (!foiDirectory.exists()) {
                foiDirectory.mkdir();
            }
            final File foiFile = new File(foiDirectory, foi.getId() + FILE_EXTENSION);
            if (!foiFile.exists()) {
                final boolean created = foiFile.createNewFile();
                if (!created) {
                    throw new DataStoreException("unable to create a feature of interest file.");
                }
                final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
                marshaller.marshal(foi, foiFile);
                MARSHALLER_POOL.recycle(marshaller);
            }
        } catch (JAXBException | IOException ex) {
            throw new DataStoreException("Exception while marshalling the feature of interest file.", ex);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void writePhenomenons(final List<org.opengis.observation.Phenomenon> phenomenons) throws DataStoreException {
        for (org.opengis.observation.Phenomenon phenomenon : phenomenons)  {
            if (phenomenon instanceof Phenomenon) {
                writePhenomenon((Phenomenon)phenomenon);
            } else if (phenomenon != null) {
                LOGGER.log(Level.WARNING, "Bad implementation of phenomenon:{0}", phenomenon.getClass().getName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(final ObservationOffering offering) throws DataStoreException {
        try {
            
            if (!offeringDirectory.exists()) {
                offeringDirectory.mkdir();
            }
            final File offeringFile = new File(offeringDirectory, offering.getId() + FILE_EXTENSION);
            final boolean created = offeringFile.createNewFile();
            if (!created) {
                throw new DataStoreException("unable to create an offering file.");
            }
            final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
            marshaller.marshal(offering, offeringFile);
            MARSHALLER_POOL.recycle(marshaller);
            return offering.getId();
        } catch (JAXBException | IOException ex) {
            throw new DataStoreException("Exception while marshalling the offering file.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final String offeringID, final String offProc, final List<String> offPheno, final String offSF) throws DataStoreException {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOfferings() {
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordProcedureLocation(final String physicalID, final AbstractGeometry position) throws DataStoreException {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Filesystem O&M Writer 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        indexer.destroy();
    }

}
