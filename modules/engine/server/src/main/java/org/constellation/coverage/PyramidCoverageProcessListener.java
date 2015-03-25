/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.coverage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.admin.SpringHelper;
import org.constellation.api.TaskState;
import org.constellation.business.IProcessBusiness;
import org.constellation.engine.register.jooq.tables.pojos.Task;
import org.constellation.provider.DataProviders;
import org.geotoolkit.parameter.ParameterGroup;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listener for Pyramidal process loaded via {@link PyramidCoverageHelper}
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public class PyramidCoverageProcessListener implements ProcessListener {

    private static final Logger LOGGER = Logger.getLogger(PyramidCoverageProcessListener.class.getName());

    private String uuidTask;

    private final Integer userId;
    private final String path;
    private final String identifier;

    @Autowired
    private IProcessBusiness processBusiness;
    
    public PyramidCoverageProcessListener(final Integer userId, final String path, final String identifier) {
        SpringHelper.injectDependencies(this);
        this.userId = userId;
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public void started(final ProcessEvent processEvent) {
        //Create task on database (state : pending)
        uuidTask = UUID.randomUUID().toString();
//        processBusiness.writeTask(uuidTask, "pyramid", userId, System.currentTimeMillis());
    }

    @Override
    public void progressing(final ProcessEvent processEvent) {
        LOGGER.log(Level.FINEST, "nothing when fire processing");
    }

    @Override
    public void paused(final ProcessEvent processEvent) {
        LOGGER.log(Level.FINEST, "nothing when fire paused");
    }

    @Override
    public void resumed(final ProcessEvent processEvent) {
        LOGGER.log(Level.FINEST, "nothing when fire resumed");
    }

    @Override
    public void completed(final ProcessEvent processEvent) {
        //Update state (pass to completed) on database
        final Task pyramidTask = processBusiness.getTask(uuidTask);
        pyramidTask.setState(TaskState.SUCCEED.name());
        processBusiness.updateTask(pyramidTask);

        //update provider
        updateProvider();
    }

    /**
     *
     */
    private void updateProvider() {
    	final ParameterValueGroup oldSource = DataProviders.getInstance().getProvider(identifier).getSource();
        final ParameterDescriptorGroup descriptor = oldSource.getDescriptor();
        URL fileUrl = null;
        try {
            fileUrl = URI.create("file:"+path+"/tiles").toURL();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "unable to create url from path", e);
        }
        final ParameterValueGroup newSources = new ParameterGroup(descriptor);
        newSources.parameter("id").setValue(oldSource.parameter("id").getValue());
        newSources.parameter("providerType").setValue(oldSource.parameter("providerType").getValue());
        newSources.parameter("date").setValue(new Date());

        ParameterValueGroup xmlCoverageStoreParameters = newSources.groups("choice").get(0).
                addGroup("XMLCoverageStoreParameters");
        xmlCoverageStoreParameters.parameter("identifier").setValue("coverage-xml-pyramid");
        xmlCoverageStoreParameters.parameter("path").setValue(fileUrl);
        xmlCoverageStoreParameters.parameter("namespace").setValue("no namespace");
        DataProviders.getInstance().getProvider(identifier).updateSource(newSources);
    }

    @Override
    public void failed(final ProcessEvent processEvent) {
        //Update state (pass to failed) on database
        final Task pyramidTask = processBusiness.getTask(uuidTask);
        pyramidTask.setState(TaskState.FAILED.name());
        processBusiness.updateTask(pyramidTask);
    }
}
