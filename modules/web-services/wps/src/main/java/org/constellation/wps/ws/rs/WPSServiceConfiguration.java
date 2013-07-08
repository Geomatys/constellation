package org.constellation.wps.ws.rs;

import org.constellation.configuration.Layer;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.Processes;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.wps.ws.WPSWorker;
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
 * WPS {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class WPSServiceConfiguration implements ServiceConfiguration {


    public Class getWorkerClass() {
        return WPSWorker.class;
    }


    public void configureInstance(File instanceDirectory, Object configuration, Object o, String serviceType) throws CstlServiceException {
        if (configuration instanceof ProcessContext) {
            final File configurationFile = new File(instanceDirectory, "processContext.xml");
            try {
                Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                GenericDatabaseMarshallerPool.getInstance().release(marshaller);
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("The configuration Object is not a process context", INVALID_PARAMETER_VALUE);
        }
    }


    public Object getInstanceConfiguration(File instanceDirectory, String serviceType) throws CstlServiceException {
        final File configurationFile = new File(instanceDirectory, "processContext.xml");
        if (configurationFile.exists()) {
            try {
                final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                final Object obj = unmarshaller.unmarshal(configurationFile);
                GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                if (obj instanceof ProcessContext) {
                    return obj;
                } else {
                    throw new CstlServiceException("The processContext.xml file does not contain a ProcessContext object");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex);
            }
        } else {
            throw new CstlServiceException("Unable to find a file processContext.xml");
        }
    }


    public void basicConfigure(File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        configureInstance(instanceDirectory, new ProcessContext(new Processes(true)), capabilitiesConfiguration, serviceType);
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
