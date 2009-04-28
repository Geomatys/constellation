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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311modified.DirectPositionType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonEntry;
import org.geotoolkit.sos.xml.v100.OfferingProcedureEntry;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureEntry;
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

    private File sensorDirectory;

    private File foiDirectory;

    private File resultDirectory;

    private MarshallerPool marshallerPool;

    public FileObservationWriter(Automatic configuration) throws CstlServiceException {
        super();
        File dataDirectory = configuration.getdataDirectory();
        if (dataDirectory.exists()) {
            offeringDirectory    = new File(dataDirectory, "offerings");
            phenomenonDirectory  = new File(dataDirectory, "phenomenons");
            observationDirectory = new File(dataDirectory, "observations");
            sensorDirectory      = new File(dataDirectory, "sensors");
            foiDirectory         = new File(dataDirectory, "features");
            resultDirectory      = new File(dataDirectory, "results");
        }
        try {
            marshallerPool = new MarshallerPool("org.geotoolkit.sos.xml.v100:org.geotoolkit.observation.xml.v100");
        } catch(JAXBException ex) {
            throw new CstlServiceException("JAXB exception while initializing the file observation reader", ex, NO_APPLICABLE_CODE);
        }

    }


    @Override
    public String writeObservation(Observation observation) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            File observationFile = new File(observationDirectory, observation.getName() + ".xml");
            observationFile.createNewFile();
            marshaller.marshal(observation, observationFile);
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

    @Override
    public String writeMeasurement(Measurement measurement) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            File observationFile = new File(observationDirectory, measurement.getName() + ".xml");
            observationFile.createNewFile();
            marshaller.marshal(measurement, observationFile);
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

    @Override
    public String writeOffering(ObservationOfferingEntry offering) throws CstlServiceException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            File offeringFile = new File(offeringDirectory, offering.getName() + ".xml");
            offeringFile.createNewFile();
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

    @Override
    public void updateOffering(OfferingProcedureEntry offProc, OfferingPhenomenonEntry offPheno, OfferingSamplingFeatureEntry offSF) throws CstlServiceException {
        // TODO
    }

    @Override
    public void updateOfferings() {
        //do nothing
    }

    @Override
    public void recordProcedureLocation(String physicalID, DirectPositionType position) throws CstlServiceException {
        // TODO
    }

    @Override
    public String getInfos() {
        return "Constellation Filesystem O&M Writer 0.3";
    }

    @Override
    public void destroy() {
        // do nothing
    }

}
