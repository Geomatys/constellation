package org.constellation.sos.ws.rs;

import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Layer;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.dto.Service;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.sos.ws.SOSworker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.ServiceConfiguration;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;

/**
 * SOS {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class SOSServiceConfiguration implements ServiceConfiguration {

    public Class getWorkerClass() {
        return SOSworker.class;
    }

    public void configureInstance(File instanceDirectory, Object configuration, Object o, String serviceType) throws CstlServiceException {
        if (configuration instanceof SOSConfiguration) {
            final File configurationFile = new File(instanceDirectory, "config.xml");
            try {
                Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                GenericDatabaseMarshallerPool.getInstance().recycle(marshaller);
            } catch(JAXBException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("The configuration Object is not a SOSConfiguration", INVALID_PARAMETER_VALUE);
        }
    }

    public Object getInstanceConfiguration(File instanceDirectory, String serviceType) throws CstlServiceException {
        final File configurationFile = new File(instanceDirectory, "config.xml");
        if (configurationFile.exists()) {
            try {
                final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                final Object obj = unmarshaller.unmarshal(configurationFile);
                GenericDatabaseMarshallerPool.getInstance().recycle(unmarshaller);
                if (obj instanceof SOSConfiguration) {
                    return obj;
                } else {
                    throw new CstlServiceException("The config.xml file does not contain a SOSConfiguration object");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex);
            }
        } else {
            throw new CstlServiceException("Unable to find a file config.xml");
        }
    }

    public void basicConfigure(File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        final SOSConfiguration baseConfig = new SOSConfiguration(new Automatic(null, new BDD()), new Automatic(null, new BDD()));
        baseConfig.setObservationReaderType(DataSourceType.FILESYSTEM);
        baseConfig.setObservationFilterType(DataSourceType.LUCENE);
        baseConfig.setObservationWriterType(DataSourceType.FILESYSTEM);
        baseConfig.setSMLType(DataSourceType.FILESYSTEM);
        configureInstance(instanceDirectory, baseConfig, capabilitiesConfiguration, serviceType);
    }

    public String getAbstract(File instanceDirectory) {
        //TODO
        return "";
    }

    public List<Layer> getlayersNumber(Worker worker) {
        //TODO
        return new ArrayList<Layer>(0);
    }
}
