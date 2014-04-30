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
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.factory.OMFactory;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.observation.ObservationReader;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.TemporalPrimitive;

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

    protected final String phenomenonIdBase;
    
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

    public FileObservationReader(final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.phenomenonIdBase  = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        final File dataDirectory = configuration.getDataDirectory();
        if (dataDirectory != null && dataDirectory.exists()) {
            offeringDirectory            = new File(dataDirectory, "offerings");
            phenomenonDirectory          = new File(dataDirectory, "phenomenons");
            observationDirectory         = new File(dataDirectory, "observations");
            observationTemplateDirectory = new File(dataDirectory, "observationTemplates");
            sensorDirectory              = new File(dataDirectory, "sensors");
            foiDirectory                 = new File(dataDirectory, "features");
        } else {
            throw new DataStoreException("There is no data Directory");
        }
        if (MARSHALLER_POOL == null) {
            throw new DataStoreException("JAXB exception while initializing the file observation reader");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getOfferingNames(final String version) throws DataStoreException {
        final List<String> offeringNames = new ArrayList<>();
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
    public List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws DataStoreException {
        final List<ObservationOffering> offerings = new ArrayList<>();
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName, version));
        }
        return offerings;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOffering getObservationOffering(final String offeringName, final String version) throws DataStoreException {
        final File offeringVersionDir = new File(offeringDirectory, version); 
        if (offeringVersionDir.isDirectory()) {
            final File offeringFile = new File(offeringVersionDir, offeringName + FILE_EXTENSION);
            if (offeringFile.exists()) {
                try {
                    final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                    Object obj = unmarshaller.unmarshal(offeringFile);
                    MARSHALLER_POOL.recycle(unmarshaller);
                    if (obj instanceof JAXBElement) {
                        obj = ((JAXBElement)obj).getValue();
                    }
                    if (obj instanceof ObservationOffering) {
                        return (ObservationOffering) obj;
                    }
                    throw new DataStoreException("The file " + offeringFile + " does not contains an offering Object.");
                } catch (JAXBException ex) {
                    throw new DataStoreException("Unable to unmarshall The file " + offeringFile, ex);
                }
            }
        } else {
            throw new DataStoreException("Unsuported version:" + version);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws DataStoreException {
        final List<ObservationOffering> offerings = new ArrayList<>();
        if (offeringDirectory.exists()) {
            final File offeringVersionDir = new File(offeringDirectory, version); 
            if (offeringVersionDir.isDirectory()) {
                for (File offeringFile: offeringVersionDir.listFiles()) {
                    try {
                        final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                        Object obj = unmarshaller.unmarshal(offeringFile);
                        MARSHALLER_POOL.recycle(unmarshaller);
                        if (obj instanceof JAXBElement) {
                            obj = ((JAXBElement)obj).getValue();
                        }
                        if (obj instanceof ObservationOffering) {
                            offerings.add((ObservationOffering) obj);
                        } else {
                            throw new DataStoreException("The file " + offeringFile + " does not contains an offering Object.");
                        }
                    } catch (JAXBException ex) {
                        String msg = ex.getMessage();
                        if (msg == null && ex.getCause() != null) {
                            msg = ex.getCause().getMessage();
                        }
                        LOGGER.warning("Unable to unmarshall The file " + offeringFile + " cause:" + msg);
                    }
                }
            } else {
                throw new DataStoreException("Unsuported version:" + version);
            }
        }
        return offerings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getProcedureNames() throws DataStoreException {
        final List<String> sensorNames = new ArrayList<>();
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
    public Collection<String> getPhenomenonNames() throws DataStoreException {
        final List<String> phenomenonNames = new ArrayList<>();
        if (phenomenonDirectory.exists()) {
            for (File phenomenonFile: phenomenonDirectory.listFiles()) {
                String phenomenonName = phenomenonFile.getName();
                phenomenonName = phenomenonName.substring(0, phenomenonName.indexOf(FILE_EXTENSION));
                phenomenonNames.add(phenomenonName);
            }
        }
        return phenomenonNames;
    }

    @Override
    public Collection<String> getProceduresForPhenomenon(String observedProperty) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }

    @Override
    public Collection<String> getPhenomenonsForProcedure(String sensorID) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existPhenomenon(String phenomenonName) throws DataStoreException {
        // we remove the phenomenon id base
        if (phenomenonName.contains(phenomenonIdBase)) {
            phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
        }
        final File phenomenonFile = new File(phenomenonDirectory, phenomenonName + FILE_EXTENSION);
        return phenomenonFile.exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getFeatureOfInterestNames() throws DataStoreException {
        final List<String> foiNames = new ArrayList<>();
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
    public SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws DataStoreException {
        final File samplingFeatureFile = new File(foiDirectory, samplingFeatureName + FILE_EXTENSION);
        if (samplingFeatureFile.exists()) {
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(samplingFeatureFile);
                MARSHALLER_POOL.recycle(unmarshaller);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof SamplingFeature) {
                    return (SamplingFeature) obj;
                }
                throw new DataStoreException("The file " + samplingFeatureFile + " does not contains an foi Object.");
            } catch (JAXBException ex) {
                throw new DataStoreException("Unable to unmarshall The file " + samplingFeatureFile, ex);
            } 
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observation getObservation(final String identifier, final QName resultModel, final ResponseModeType mode, final String version) throws DataStoreException {
        File observationFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (!observationFile.exists()) {
            observationFile = new File(observationTemplateDirectory, identifier + FILE_EXTENSION);
        }
        if (observationFile.exists()) {
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(observationFile);
                MARSHALLER_POOL.recycle(unmarshaller);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof Observation) {
                    return (Observation) obj;
                }
                throw new DataStoreException("The file " + observationFile + " does not contains an observation Object.");
            } catch (JAXBException ex) {
                throw new DataStoreException("Unable to unmarshall The file " + observationFile, ex);
            }
        }
        throw new DataStoreException("The file " + observationFile + " does not exist");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult(final String identifier, final QName resultModel, final String version) throws DataStoreException {
        final File anyResultFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (anyResultFile.exists()) {
            
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(anyResultFile);
                MARSHALLER_POOL.recycle(unmarshaller);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof Observation) {
                    final Observation obs = (Observation) obj;
                    final DataArrayProperty arrayP = (DataArrayProperty) obs.getResult();
                    return arrayP.getDataArray();
                }
                throw new DataStoreException("The file " + anyResultFile + " does not contains an observation Object.");
            } catch (JAXBException ex) {
                throw new DataStoreException("Unable to unmarshall The file " + anyResultFile, ex);
            }
        }
        throw new DataStoreException("The file " + anyResultFile + " does not exist");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existProcedure(final String href) throws DataStoreException {
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
    public String getNewObservationId() throws DataStoreException {
        String obsID = null;
        boolean exist = true;
        int i = observationDirectory.list().length;
        while (exist) {
            obsID = observationIdBase + i;
            final File newFile = new File(observationDirectory, obsID);
            exist = newFile.exists();
            i++;
        }
        return obsID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws DataStoreException {
        return Arrays.asList("undefined", "now");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalPrimitive getFeatureOfInterestTime(final String samplingFeatureName, final String version) throws DataStoreException {
        throw new DataStoreException("The Filesystem implementation of SOS does not support GetFeatureofInterestTime");
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
    public List<ResponseModeType> getResponseModes() throws DataStoreException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResponseFormats() throws DataStoreException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }

    @Override
    public AbstractGeometry getSensorLocation(String sensorID, String version) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
}
