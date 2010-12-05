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
import org.constellation.sos.factory.AbstractSOSFactory;
import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
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
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationReader;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.AnyResult;
import org.geotoolkit.swe.xml.v101.AnyResultEntry;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileObservationReader implements ObservationReader {

     /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

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

    public FileObservationReader(Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(AbstractSOSFactory.OBSERVATION_ID_BASE);
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
    public Collection<String> getOfferingNames() throws CstlServiceException {
        final List<String> offeringNames = new ArrayList<String>();
        if (offeringDirectory.exists()) {
            for (File offeringFile: offeringDirectory.listFiles()) {
                String offeringName = offeringFile.getName();
                offeringName = offeringName.substring(0, offeringName.indexOf(FILE_EXTENSION));
                offeringNames.add(offeringName);
            }
        }
        return offeringNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException {
        final File offeringFile = new File(offeringDirectory, offeringName + FILE_EXTENSION);
        if (offeringFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                final Object obj = unmarshaller.unmarshal(offeringFile);
                if (obj instanceof ObservationOfferingEntry) {
                    return (ObservationOfferingEntry) obj;
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
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOfferingEntry> getObservationOfferings() throws CstlServiceException {
        final List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        if (offeringDirectory.exists()) {
            for (File offeringFile: offeringDirectory.listFiles()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                    final Object obj = unmarshaller.unmarshal(offeringFile);
                    if (obj instanceof ObservationOfferingEntry) {
                        offerings.add((ObservationOfferingEntry) obj);
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
    public PhenomenonEntry getPhenomenon(String phenomenonName) throws CstlServiceException {
        final File phenomenonFile = new File(phenomenonDirectory, phenomenonName + FILE_EXTENSION);
        if (phenomenonFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(phenomenonFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof PhenomenonEntry) {
                    return (PhenomenonEntry) obj;
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
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureName) throws CstlServiceException {
        final File samplingFeatureFile = new File(foiDirectory, samplingFeatureName + FILE_EXTENSION);
        if (samplingFeatureFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(samplingFeatureFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof SamplingFeatureEntry) {
                    return (SamplingFeatureEntry) obj;
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
    public ObservationEntry getObservation(String identifier, QName resultModel) throws CstlServiceException {
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
                if (obj instanceof ObservationEntry) {
                    return (ObservationEntry) obj;
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
    public AnyResult getResult(String identifier, QName resutModel) throws CstlServiceException {
        final File anyResultFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (anyResultFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(anyResultFile);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof ObservationEntry) {
                    final ObservationEntry obs = (ObservationEntry) obj;
                    final DataArrayPropertyType arrayP = (DataArrayPropertyType) obs.getResult();
                    return new AnyResultEntry(null, arrayP.getDataArray());
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
    public ReferenceEntry getReference(String href) throws CstlServiceException {
        return new ReferenceEntry(null, href);
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
    public AbstractTimePrimitiveType getFeatureOfInterestTime(String samplingFeatureName) throws CstlServiceException {
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
        return "Constellation Filesystem O&M Reader 0.6";
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
