package org.constellation.metadata.ws.rs;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.CSWworker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ServiceConfiguration;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;

/**
 * CSW {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class CSWServiceConfiguration implements ServiceConfiguration {

    public Class getWorkerClass() {
        return CSWworker.class;
    }

    public void configureInstance(File instanceDirectory, Object configuration, Object o, String serviceType) throws CstlServiceException {
        if (configuration instanceof Automatic) {
            final File configurationFile = new File(instanceDirectory, "config.xml");
            try {
                final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                GenericDatabaseMarshallerPool.getInstance().release(marshaller);
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("The configuration Object is not an Automatic object", INVALID_PARAMETER_VALUE);
        }
    }

    public Object getInstanceConfiguration(File instanceDirectory, String serviceType) throws CstlServiceException {
        final File configurationFile = new File(instanceDirectory, "config.xml");
        if (configurationFile.exists()) {
            try {
                Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(configurationFile);
                GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                if (obj instanceof Automatic) {
                    return obj;
                } else {
                    throw new CstlServiceException("The config.xml file does not contain a Automatic object");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex);
            }
        } else {
            throw new CstlServiceException("Unable to find a file config.xml");
        }
    }

    public void basicConfigure(File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        configureInstance(instanceDirectory, new Automatic("filesystem", new BDD()), null, serviceType);
    }
}
