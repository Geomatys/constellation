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

import java.io.File;
import java.io.IOException;

// JAXB dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.lucene.LuceneObservationIndexer;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonEntry;
import org.geotoolkit.sos.xml.v100.OfferingProcedureEntry;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.observation.Measurement;
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

    private MarshallerPool marshallerPool;

    private LuceneObservationIndexer indexer;

    private String observationTemplateIdBase;

    private static final String FILE_EXTENSION = ".xml";

    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos.io.filesystem");

    public FileObservationWriter(Automatic configuration, String observationTemplateIdBase) throws CstlServiceException {
        super();
        this.observationTemplateIdBase = observationTemplateIdBase;
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
        try {
            indexer        = new LuceneObservationIndexer(configuration, "");
            marshallerPool = SOSMarshallerPool.getInstance();
            if (marshallerPool == null){
                throw new CstlServiceException("JAXB exception while initializing the file observation reader", NO_APPLICABLE_CODE);
            }
        } catch (IndexingException ex) {
            throw new CstlServiceException("Indexing exception while initializing the file observation reader", ex, NO_APPLICABLE_CODE);
        }


    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(Observation observation) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            final File observationFile;
            if (observation.getName().startsWith(observationTemplateIdBase)) {
                observationFile = new File(observationTemplateDirectory, observation.getName() + FILE_EXTENSION);
            } else {
                observationFile = new File(observationDirectory, observation.getName() + FILE_EXTENSION);
            }
            if (observationFile.exists()) {
                final boolean created      = observationFile.createNewFile();
                if (!created) {
                    throw new CstlServiceException("unable to create an observation file.", NO_APPLICABLE_CODE);
                }
            } else {
                LOGGER.log(Level.WARNING, "we overwrite the file:{0}", observationFile.getPath());
            }
            
            marshaller.marshal(observation, observationFile);
            writePhenomenon((PhenomenonEntry) observation.getObservedProperty());
            if (observation.getFeatureOfInterest() !=  null) {
                writeFeatureOfInterest((SamplingFeatureEntry) observation.getFeatureOfInterest());
            }
            indexer.indexDocument((ObservationEntry) observation);
            return observation.getName();
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeMeasurement(Measurement measurement) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            final File observationFile = new File(observationDirectory, measurement.getName() + FILE_EXTENSION);
            final boolean created = observationFile.createNewFile();
            if (!created) {
                throw new CstlServiceException("unable to create an observation file.", NO_APPLICABLE_CODE);
            }
            marshaller.marshal(measurement, observationFile);
            writePhenomenon((PhenomenonEntry) measurement.getObservedProperty());
            writeFeatureOfInterest((SamplingFeatureEntry) measurement.getFeatureOfInterest());
            indexer.indexDocument((ObservationEntry) measurement);
            return measurement.getName();
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the observation file.", ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    private void writePhenomenon(PhenomenonEntry phenomenon) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (!phenomenonDirectory.exists()) {
                phenomenonDirectory.mkdir();
            }
            final File phenomenonFile = new File(phenomenonDirectory, phenomenon.getId() + FILE_EXTENSION);
            if (!phenomenonFile.exists()) {
                final boolean created = phenomenonFile.createNewFile();
                if (!created) {
                    throw new CstlServiceException("unable to create a phenomenon file.", NO_APPLICABLE_CODE);
                }
                marshaller.marshal(phenomenon, phenomenonFile);
            }
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the phenomenon file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the phenomenon file.", ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    private void writeFeatureOfInterest(SamplingFeatureEntry foi) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (!foiDirectory.exists()) {
                foiDirectory.mkdir();
            }
            final File foiFile = new File(foiDirectory, foi.getId() + FILE_EXTENSION);
            if (!foiFile.exists()) {
                final boolean created = foiFile.createNewFile();
                if (!created) {
                    throw new CstlServiceException("unable to create a feature of interest file.", NO_APPLICABLE_CODE);
                }
                marshaller.marshal(foi, foiFile);
            }
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the feature of interest file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the feature of interest file.", ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(ObservationOfferingEntry offering) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (!offeringDirectory.exists()) {
                offeringDirectory.mkdir();
            }
            final File offeringFile = new File(offeringDirectory, offering.getName() + FILE_EXTENSION);
            final boolean created = offeringFile.createNewFile();
            if (!created) {
                throw new CstlServiceException("unable to create an offering file.", NO_APPLICABLE_CODE);
            }
            marshaller.marshal(offering, offeringFile);
            return offering.getName();
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB exception while marshalling the offering file.", ex, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while marshalling the offering file.", ex, NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(OfferingProcedureEntry offProc, OfferingPhenomenonEntry offPheno, OfferingSamplingFeatureEntry offSF) throws CstlServiceException {
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
    public void recordProcedureLocation(String physicalID, DirectPositionType position) throws CstlServiceException {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Filesystem O&M Writer 0.6";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // do nothing
    }

}
