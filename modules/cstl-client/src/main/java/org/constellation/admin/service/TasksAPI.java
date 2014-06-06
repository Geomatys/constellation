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

package org.constellation.admin.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.StringList;
import org.constellation.dto.TaskStatus;
import org.geotoolkit.xml.parameter.ParameterDescriptorReader;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class TasksAPI {
    
    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     * Creates a {@link ProvidersAPI} instance.
     *
     * @param client the client to use
     */
    TasksAPI(final ConstellationClient client) {
        this.client = client;
    }
    
    public Map<String, TaskStatus> listTasks() throws HttpResponseException, IOException {
        String path = "task/listTasks";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Map.class);
    }
    
    public Collection<String> listProcess() throws HttpResponseException, IOException {
        String path = "task/listProcesses";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class).getList();
    }
    
    public Collection<String> listProcessForFactory(final String authorityCode) throws HttpResponseException, IOException {
        String path = "task/process/factory/" + authorityCode;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class).getList();
    }
    
    public Collection<String> listProcessFactories() throws HttpResponseException, IOException {
        String path = "task/listProcessFactories";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class).getList();
    }
    
    public GeneralParameterDescriptor getProcessDescriptor(final String authority, final String code) throws HttpResponseException, IOException, XMLStreamException, ClassNotFoundException {
        final Object object = client.get("task/process/descriptor/" + authority + "/" + "code", MediaType.APPLICATION_XML_TYPE).getEntity(Object.class);
        final ParameterDescriptorReader reader = new ParameterDescriptorReader();
        reader.setInput(object);
        reader.read();
        Object response = reader.getDescriptorsRoot();
        if (response instanceof GeneralParameterDescriptor) {
            return (GeneralParameterDescriptor) response;
        }
        return null;
    }
    
    public GeneralParameterValue getTaskParameters(final String id, ParameterDescriptorGroup descriptor) throws HttpResponseException, IOException, XMLStreamException {
        final Object object = client.get("taks/" + id, MediaType.APPLICATION_XML_TYPE).getEntity(Object.class);
        final ParameterValueReader reader = new ParameterValueReader(descriptor);
        reader.setInput(object);
        return reader.read();    
    }
    
    public boolean createTask(final String authority, final String code, final String id, final String title, final int step, final GeneralParameterValue parameters) throws IOException {
        final AcknowlegementType ack = client.post("task/" + id + "/" + authority + "/" + code + "/" + title + "/" + step, MediaType.APPLICATION_XML_TYPE, parameters).getEntity(AcknowlegementType.class);
        return "Success".equals(ack.getStatus());

    }
    
    public boolean updateTask(final String authority, final String code, final String id, final String title, final int step, final GeneralParameterValue parameters) throws IOException {
        final AcknowlegementType ack = client.put("task/" + id + "/" + authority + "/" + code + "/" + title + "/" + step, MediaType.APPLICATION_XML_TYPE, parameters).getEntity(AcknowlegementType.class);
        return "Success".equals(ack.getStatus());

    }
    
    public boolean deleteTask(final String id) throws IOException {
        final AcknowlegementType ack = client.delete("task/" + id, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return "Success".equals(ack.getStatus());
    }
}
