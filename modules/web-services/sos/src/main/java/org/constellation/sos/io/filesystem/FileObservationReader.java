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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.swe.AnyResult;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.xml.MarshallerPool;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileObservationReader implements ObservationReader {

     /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;

    private File offeringDirectory;

    private File phenomenonDirectory;

    private File observationDirectory;

    private File sensorDirectory;

    private File foiDirectory;

    private File resultDirectory;

    private MarshallerPool marshallerPool;

    public FileObservationReader(String observationIdBase, Automatic configuration) throws CstlServiceException {
        this.observationIdBase = observationIdBase;
        File dataDirectory = configuration.getdataDirectory();
        if (dataDirectory != null && dataDirectory.exists()) {
            offeringDirectory    = new File(dataDirectory, "offerings");
            phenomenonDirectory  = new File(dataDirectory, "phenomenons");
            observationDirectory = new File(dataDirectory, "observations");
            sensorDirectory      = new File(dataDirectory, "sensors");
            foiDirectory         = new File(dataDirectory, "features");
            resultDirectory      = new File(dataDirectory, "results");
        }
        try {
            marshallerPool = new MarshallerPool("org.constellation.sos.v100:org.constellation.observation");
        } catch(JAXBException ex) {
            throw new CstlServiceException("JAXB exception while initializing the file observation reader",  NO_APPLICABLE_CODE);
        }

    }

    @Override
    public Collection<String> getOfferingNames() throws CstlServiceException {
        List<String> offeringNames = new ArrayList<String>();
        for (File offeringFile: offeringDirectory.listFiles()) {
            String offeringName = offeringFile.getName();
            offeringName.substring(0, offeringName.indexOf(".xml"));
            offeringNames.add(offeringName);
        }
        return offeringNames;
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException {
        File offeringFile = new File(observationDirectory, offeringName + ".xml");
        if (offeringFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(offeringFile);
                if (obj instanceof ObservationOfferingEntry) {
                    return (ObservationOfferingEntry) obj;
                }
                throw new CstlServiceException("The file " + offeringFile + " does not contains an offering Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + offeringFile, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + offeringFile + " does not exist", NO_APPLICABLE_CODE);
    }

    @Override
    public List<ObservationOfferingEntry> getObservationOfferings() throws CstlServiceException {
        List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        for (File offeringFile: offeringDirectory.listFiles()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(offeringFile);
                if (obj instanceof ObservationOfferingEntry) {
                    offerings.add((ObservationOfferingEntry) obj);
                }
                throw new CstlServiceException("The file " + offeringFile + " does not contains an offering Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                logger.severe("Unable to unmarshall The file " + offeringFile);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        }
        return offerings;
    }

    @Override
    public Collection<String> getProcedureNames() throws CstlServiceException {
        List<String> sensorNames = new ArrayList<String>();
        for (File sensorFile: sensorDirectory.listFiles()) {
            String sensorName = sensorFile.getName();
            sensorName.substring(0, sensorName.indexOf(".xml"));
            sensorNames.add(sensorName);
        }
        return sensorNames;
    }

    @Override
    public Collection<String> getPhenomenonNames() throws CstlServiceException {
        List<String> phenomenonNames = new ArrayList<String>();
        for (File phenomenonFile: phenomenonDirectory.listFiles()) {
            String phenomenonName = phenomenonFile.getName();
            phenomenonName.substring(0, phenomenonName.indexOf(".xml"));
            phenomenonNames.add(phenomenonName);
        }
        return phenomenonNames;
    }

    @Override
    public PhenomenonEntry getPhenomenon(String phenomenonName) throws CstlServiceException {
        File phenomenonFile = new File(phenomenonDirectory, phenomenonName + ".xml");
        if (phenomenonFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(phenomenonFile);
                if (obj instanceof PhenomenonEntry) {
                    return (PhenomenonEntry) obj;
                }
                throw new CstlServiceException("The file " + phenomenonFile + " does not contains an phenomenon Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + phenomenonFile, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + phenomenonFile + " does not exist", NO_APPLICABLE_CODE);
    }

    @Override
    public Collection<String> getFeatureOfInterestNames() throws CstlServiceException {
        List<String> foiNames = new ArrayList<String>();
        for (File foiFile: foiDirectory.listFiles()) {
            String foiName = foiFile.getName();
            foiName.substring(0, foiName.indexOf(".xml"));
            foiNames.add(foiName);
        }
        return foiNames;
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws CstlServiceException {
        File samplingFeatureFile = new File(foiDirectory, samplingFeatureName + ".xml");
        if (samplingFeatureFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(samplingFeatureFile);
                if (obj instanceof SamplingFeatureEntry) {
                    return (SamplingFeatureEntry) obj;
                }
                throw new CstlServiceException("The file " + samplingFeatureFile + " does not contains an foi Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + samplingFeatureFile, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + samplingFeatureFile + " does not exist", NO_APPLICABLE_CODE);
    }

    @Override
    public ObservationEntry getObservation(String identifier) throws CstlServiceException {
        File observationFile = new File(observationDirectory, identifier + ".xml");
        if (observationFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(observationFile);
                if (obj instanceof ObservationEntry) {
                    return (ObservationEntry) obj;
                }
                throw new CstlServiceException("The file " + observationFile + " does not contains an foi Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + observationFile, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + observationFile + " does not exist", NO_APPLICABLE_CODE);
    }

    @Override
    public AnyResult getResult(String identifier) throws CstlServiceException {
        File AnyResultFile = new File(resultDirectory, identifier + ".xml");
        if (AnyResultFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(AnyResultFile);
                if (obj instanceof AnyResult) {
                    return (AnyResult) obj;
                }
                throw new CstlServiceException("The file " + AnyResultFile + " does not contains an foi Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + AnyResultFile, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + AnyResultFile + " does not exist", NO_APPLICABLE_CODE);
    }

    @Override
    public ReferenceEntry getReference(String href) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNewObservationId() throws CstlServiceException {
        return observationIdBase + observationDirectory.list().length;
    }

    @Override
    public List<String> getEventTime() throws CstlServiceException {
        return Arrays.asList("undefined", "now");
    }

    @Override
    public void destroy() {
        
    }

    public String getInfos() {
        return "Constellation Filesystem O&M Reader 0.3";
    }

    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }

}
