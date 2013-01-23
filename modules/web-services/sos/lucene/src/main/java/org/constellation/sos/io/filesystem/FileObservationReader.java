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


import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.sos.factory.OMFactory;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationReader;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.observation.xml.v100.ObservationType;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureType;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.swe.xml.AnyResult;
import org.geotoolkit.swe.xml.v101.AnyResultType;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sos.xml.ObservationOffering;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileObservationReader implements ObservationReader {

     /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;

    private File offeringDirectory;

    private File phenomenonDirectory;

    private File observationDirectory;

    private File observationTemplateDirectory;

    private File sensorDirectory;

    private File foiDirectory;

    private static final MarshallerPool MARSHALLER_POOL;
    static {
        MARSHALLER_POOL = SOSMarshallerPool.getInstance();
    }

    private static final String FILE_EXTENSION = ".xml";

    public FileObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        final File dataDirectory = configuration.getDataDirectory();
        if (dataDirectory != null && dataDirectory.exists()) {
            offeringDirectory            = new File(dataDirectory, "offerings");
            phenomenonDirectory          = new File(dataDirectory, "phenomenons");
            observationDirectory         = new File(dataDirectory, "observations");
            observationTemplateDirectory = new File(dataDirectory, "observationTemplates");
            sensorDirectory              = new File(dataDirectory, "sensors");
            foiDirectory                 = new File(dataDirectory, "features");
        } else {
            throw new CstlServiceException("There is no data Directory", NO_APPLICABLE_CODE);
        }
        if (MARSHALLER_POOL == null) {
            throw new CstlServiceException("JAXB exception while initializing the file observation reader", NO_APPLICABLE_CODE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getOfferingNames(final String version) throws CstlServiceException {
        final List<String> offeringNames = new ArrayList<String>();
        if (offeringDirectory.isDirectory()) {
            final File offeringVersionDir = new File(observationDirectory, version);
            if (offeringVersionDir.isDirectory()) {
                for (File offeringFile: offeringVersionDir.listFiles()) {
                    String offeringName = offeringFile.getName();
                    offeringName = offeringName.substring(0, offeringName.indexOf(FILE_EXTENSION));
                    offeringNames.add(offeringName);
                }
            }
        }
        return offeringNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws CstlServiceException {
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName, version));
        }
        return offerings;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOffering getObservationOffering(final String offeringName, final String version) throws CstlServiceException {
        final File offeringVersionDir = new File(offeringDirectory, version); 
        if (offeringVersionDir.isDirectory()) {
            final File offeringFile = new File(offeringVersionDir, offeringName + FILE_EXTENSION);
            if (offeringFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                    final Object obj = unmarshaller.unmarshal(offeringFile);
                    if (obj instanceof ObservationOffering) {
                        return (ObservationOffering) obj;
                    }
                    throw new CstlServiceException("The file " + offeringFile + " does not contains an offering Object.", NO_APPLICABLE_CODE);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Unable to unmarshall The file " + offeringFile, ex, NO_APPLICABLE_CODE);
                } finally {
                    if (unmarshaller != null) {
                        MARSHALLER_POOL.release(unmarshaller);
                    }
                }
            }
        } else {
            throw new CstlServiceException("Unsuported version:" + version);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException {
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        if (offeringDirectory.exists()) {
            final File offeringVersionDir = new File(offeringDirectory, version); 
            if (offeringVersionDir.isDirectory()) {
                for (File offeringFile: offeringVersionDir.listFiles()) {
                    Unmarshaller unmarshaller = null;
                    try {
                        unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                        Object obj = unmarshaller.unmarshal(offeringFile);
                        if (obj instanceof JAXBElement) {
                            obj = ((JAXBElement)obj).getValue();
                        }
                        if (obj instanceof ObservationOffering) {
                            offerings.add((ObservationOffering) obj);
                        } else {
                            throw new CstlServiceException("The file " + offeringFile + " does not contains an offering Object.", NO_APPLICABLE_CODE);
                        }
                    } catch (JAXBException ex) {
                        String msg = ex.getMessage();
                        if (msg == null && ex.getCause() != null) {
                            msg = ex.getCause().getMessage();
                        }
                        LOGGER.warning("Unable to unmarshall The file " + offeringFile + " cause:" + msg);
                    } finally {
                        if (unmarshaller != null) {
                            MARSHALLER_POOL.release(unmarshaller);
                        }
                    }
                }
            } else {
                throw new CstlServiceException("Unsuported version:" + version);
            }
        }
        return offerings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getProcedureNames() throws CstlServiceException {
        final List<String> sensorNames = new ArrayList<String>();
        if (sensorDirectory.exists()) {
            for (File sensorFile: sensorDirectory.listFiles()) {
                String sensorName = sensorFile.getName();
                sensorName = sensorName.substring(0, sensorName.indexOf(FILE_EXTENSION));
                sensorNames.add(sensorName);
            }
        }
        return sensorNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getPhenomenonNames() throws CstlServiceException {
        final List<String> phenomenonNames = new ArrayList<String>();
        if (phenomenonDirectory.exists()) {
            for (File phenomenonFile: phenomenonDirectory.listFiles()) {
                String phenomenonName = phenomenonFile.getName();
                phenomenonName = phenomenonName.substring(0, phenomenonName.indexOf(FILE_EXTENSION));
                phenomenonNames.add(phenomenonName);
            }
        }
        return phenomenonNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhenomenonType getPhenomenon(final String phenomenonName) throws CstlServiceException {
        final File phenomenonFile = new File(phenomenonDirectory, phenomenonName + FILE_EXTENSION);
        if (phenomenonFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(phenomenonFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof PhenomenonType) {
                    return (PhenomenonType) obj;
                }
                throw new CstlServiceException("The file " + phenomenonFile + " does not contains an phenomenon Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + phenomenonFile, ex, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    MARSHALLER_POOL.release(unmarshaller);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getFeatureOfInterestNames() throws CstlServiceException {
        final List<String> foiNames = new ArrayList<String>();
        if (foiDirectory.exists()) {
            for (File foiFile: foiDirectory.listFiles()) {
                String foiName = foiFile.getName();
                foiName = foiName.substring(0, foiName.indexOf(FILE_EXTENSION));
                foiNames.add(foiName);
            }
        }
        return foiNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeatureType getFeatureOfInterest(final String samplingFeatureName) throws CstlServiceException {
        final File samplingFeatureFile = new File(foiDirectory, samplingFeatureName + FILE_EXTENSION);
        if (samplingFeatureFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(samplingFeatureFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof SamplingFeatureType) {
                    return (SamplingFeatureType) obj;
                }
                throw new CstlServiceException("The file " + samplingFeatureFile + " does not contains an foi Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + samplingFeatureFile, ex, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    MARSHALLER_POOL.release(unmarshaller);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationType getObservation(final String identifier, final QName resultModel) throws CstlServiceException {
        File observationFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (!observationFile.exists()) {
            observationFile = new File(observationTemplateDirectory, identifier + FILE_EXTENSION);
        }
        if (observationFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(observationFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof ObservationType) {
                    return (ObservationType) obj;
                }
                throw new CstlServiceException("The file " + observationFile + " does not contains an observation Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + observationFile, ex, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    MARSHALLER_POOL.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + observationFile + " does not exist", NO_APPLICABLE_CODE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AnyResult getResult(final String identifier, final QName resutModel) throws CstlServiceException {
        final File anyResultFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (anyResultFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(anyResultFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof ObservationType) {
                    final ObservationType obs = (ObservationType) obj;
                    final DataArrayPropertyType arrayP = (DataArrayPropertyType) obs.getResult();
                    return new AnyResultType(null, arrayP.getDataArray());
                }
                throw new CstlServiceException("The file " + anyResultFile + " does not contains an observation Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + anyResultFile, ex, NO_APPLICABLE_CODE);
            } finally {
                if (unmarshaller != null) {
                    MARSHALLER_POOL.release(unmarshaller);
                }
            }
        }
        throw new CstlServiceException("The file " + anyResultFile + " does not exist", NO_APPLICABLE_CODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existProcedure(final String href) throws CstlServiceException {
        if (sensorDirectory.exists()) {
            for (File sensorFile: sensorDirectory.listFiles()) {
                String sensorName = sensorFile.getName();
                sensorName = sensorName.substring(0, sensorName.indexOf(FILE_EXTENSION));
                if (sensorName.equals(href)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewObservationId() throws CstlServiceException {
        return observationIdBase + observationDirectory.list().length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws CstlServiceException {
        return Arrays.asList("undefined", "now");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractTimePrimitiveType getFeatureOfInterestTime(final String samplingFeatureName) throws CstlServiceException {
        throw new CstlServiceException("The Filesystem implementation of SOS does not support GetFeatureofInterestTime");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // nothing to destroy
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Filesystem O&M Reader 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }

}
