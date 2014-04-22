/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.sos.io.filesystem;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.lucene.LuceneObservationIndexer;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.observation.xml.AbstractObservation;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sampling.xml.SamplingFeature;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.swe.xml.Phenomenon;
import org.geotoolkit.swes.xml.ObservationTemplate;
import org.opengis.observation.Observation;


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

    public FileObservationWriter(final Automatic configuration,  final Map<String, Object> properties) throws CstlServiceException {
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
            throw new CstlServiceException("JAXB exception while initializing the file observation reader", NO_APPLICABLE_CODE);
        }
        try {
            indexer        = new LuceneObservationIndexer(configuration, "", true);
        } catch (IndexingException ex) {
            throw new CstlServiceException("Indexing exception while initializing the file observation reader", ex, NO_APPLICABLE_CODE);
        }


    }

    @Override
    public String writeObservationTemplate(final ObservationTemplate template) throws CstlServiceException {
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
                    throw new CstlServiceException("unable to create an observation file.", NO_APPLICABLE_CODE);
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
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final AbstractObservation observation) throws CstlServiceException {
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
                    throw new CstlServiceException("unable to create an observation file:" + observationFile.getName(), NO_APPLICABLE_CODE);
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
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        }
    }

    @Override
    public void removeObservation(final String observationID) throws CstlServiceException {
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
    public void removeObservationForProcedure(final String procedureID) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    private void writePhenomenon(final Phenomenon phenomenon) throws CstlServiceException {
        try {
            if (!phenomenonDirectory.exists()) {
                phenomenonDirectory.mkdir();
            }
            final File phenomenonFile = new File(phenomenonDirectory, phenomenon.getName() + FILE_EXTENSION);
            if (!phenomenonFile.exists()) {
                final boolean created = phenomenonFile.createNewFile();
                if (!created) {
                    throw new CstlServiceException("unable to create a phenomenon file.", NO_APPLICABLE_CODE);
                }
                final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
                marshaller.marshal(phenomenon, phenomenonFile);
                MARSHALLER_POOL.recycle(marshaller);
            }
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the phenomenon file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the phenomenon file.", ex, NO_APPLICABLE_CODE);
        }
    }

    private void writeFeatureOfInterest(final SamplingFeature foi) throws CstlServiceException {
        try {
            
            if (!foiDirectory.exists()) {
                foiDirectory.mkdir();
            }
            final File foiFile = new File(foiDirectory, foi.getId() + FILE_EXTENSION);
            if (!foiFile.exists()) {
                final boolean created = foiFile.createNewFile();
                if (!created) {
                    throw new CstlServiceException("unable to create a feature of interest file.", NO_APPLICABLE_CODE);
                }
                final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
                marshaller.marshal(foi, foiFile);
                MARSHALLER_POOL.recycle(marshaller);
            }
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the feature of interest file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the feature of interest file.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(final ObservationOffering offering) throws CstlServiceException {
        try {
            
            if (!offeringDirectory.exists()) {
                offeringDirectory.mkdir();
            }
            final File offeringFile = new File(offeringDirectory, offering.getId() + FILE_EXTENSION);
            final boolean created = offeringFile.createNewFile();
            if (!created) {
                throw new CstlServiceException("unable to create an offering file.", NO_APPLICABLE_CODE);
            }
            final Marshaller marshaller = MARSHALLER_POOL.acquireMarshaller();
            marshaller.marshal(offering, offeringFile);
            MARSHALLER_POOL.recycle(marshaller);
            return offering.getId();
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the offering file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the offering file.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final String offeringID, final String offProc, final List<String> offPheno, final String offSF) throws CstlServiceException {
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
    public void recordProcedureLocation(final String physicalID, final AbstractGeometry position) throws CstlServiceException {
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
