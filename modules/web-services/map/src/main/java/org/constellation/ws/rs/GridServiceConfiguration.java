package org.constellation.ws.rs;

import org.constellation.configuration.LayerContext;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.CreateMapServiceDescriptor;
import org.constellation.process.service.GetConfigMapServiceDescriptor;
import org.constellation.process.service.SetConfigMapServiceDescriptor;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import java.io.File;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;

/**
 * WMS, WMTS, WFS and WCS {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public abstract class GridServiceConfiguration implements ServiceConfiguration {

    public void configureInstance(File instanceDirectory, Object configuration, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        if (configuration instanceof LayerContext) {
            if (instanceDirectory.isDirectory()) {
                if (instanceDirectory.listFiles().length == 0) {
                    //Create
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(CreateMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
                        inputs.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
                        inputs.parameter(CreateMapServiceDescriptor.CONFIG_NAME).setValue(configuration);
                        inputs.parameter(CreateMapServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);
                        inputs.parameter(CreateMapServiceDescriptor.CAPABILITIES_CONFIG).setValue(capabilitiesConfiguration);

                        final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                        process.call();

                    } catch (NoSuchIdentifierException ex) {
                        throw new CstlServiceException(ex);
                    } catch (ProcessException ex) {
                        throw new CstlServiceException(ex);
                    }

                } else {

                    //Update
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigMapServiceDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(SetConfigMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
                        inputs.parameter(SetConfigMapServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
                        inputs.parameter(SetConfigMapServiceDescriptor.CONFIG_NAME).setValue(configuration);
                        inputs.parameter(SetConfigMapServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);

                        final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                        process.call();

                    } catch (NoSuchIdentifierException ex) {
                        throw new CstlServiceException(ex);
                    } catch (ProcessException ex) {
                        throw new CstlServiceException(ex);
                    }
                }
            }
        } else {
            throw new CstlServiceException("The configuration Object is not an LayerContext object", INVALID_PARAMETER_VALUE);
        }
    }

    public Object getInstanceConfiguration(File instanceDirectory, String serviceType) throws CstlServiceException {
        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigMapServiceDescriptor.NAME);

            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(GetConfigMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(GetConfigMapServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
            in.parameter(GetConfigMapServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);

            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            final ParameterValueGroup ouptuts = proc.call();

            return ouptuts.parameter(GetConfigMapServiceDescriptor.CONFIG_NAME).getValue();

        } catch (NoSuchIdentifierException ex) {
            throw new CstlServiceException(ex);
        } catch (ProcessException ex) {
            throw new CstlServiceException(ex);
        }
    }

    public void basicConfigure(final File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        configureInstance(instanceDirectory, new LayerContext(), capabilitiesConfiguration, serviceType);
    }
}
