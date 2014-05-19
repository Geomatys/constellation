/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.coverage;

import org.constellation.admin.dao.TaskRecord;
import org.constellation.provider.DataProviders;
import org.geotoolkit.parameter.ParameterGroup;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.admin.ConfigurationEngine;

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

    private String login;
    private String path;
    private String identifier;

    public PyramidCoverageProcessListener(final String login, final String path, final String identifier) {
        this.login = login;
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public void started(final ProcessEvent processEvent) {
        //Create task on database (state : pending)
        uuidTask = UUID.randomUUID().toString();
        ConfigurationEngine.writeTask(uuidTask, "pyramid", login);
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
        try {
            //Update state (pass to completed) on database
            final TaskRecord pyramidTask = ConfigurationEngine.getTask(uuidTask);
            pyramidTask.setState(TaskRecord.TaskState.SUCCEED);

            //update provider
            updateProvider();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to save task", e);
        }
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
        //Update state (pass to completed) on database
        try {
            final TaskRecord pyramidTask = ConfigurationEngine.getTask(uuidTask);
            pyramidTask.setState(TaskRecord.TaskState.FAILED);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Unable to save task", e);
        }
    }
}
