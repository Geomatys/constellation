/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2013-2016 Geomatys.
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
package org.constellation.client;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import org.constellation.admin.dto.TaskStatusDTO;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.StringList;
import org.geotoolkit.xml.parameter.ParameterDescriptorReader;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 *
 */
public class TaskAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    TaskAPI(final ConstellationClient client) {
        this.client = client;
    }

    /**
     * path : /1/task/chain<br>
     * method : POST<br>
     * java : org.constellation.rest.api.TaskRest.createChain<br>
     */
    public void createChain(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/chain/{authority}/{code}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.TaskRest.deleteChain<br>
     */
    public void deleteChain(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/create<br>
     * method : POST<br>
     * java : org.constellation.rest.api.TaskRest.createParamsTask<br>
     */
    public void createParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/update<br>
     * method : POST<br>
     * java : org.constellation.rest.api.TaskRest.updateParamsTask<br>
     */
    public void updateParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/get/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.getParamsTask<br>
     */
    public GeneralParameterValue getParamsTask(final String id, ParameterDescriptorGroup descriptor) throws HttpResponseException, IOException, XMLStreamException {
        final Object object = client.get("task/params/get/" + id, MediaType.APPLICATION_XML_TYPE).getEntity(Object.class);
        final ParameterValueReader reader = new ParameterValueReader(descriptor);
        reader.setInput(object);
        return reader.read();
    }

    /**
     * path : /1/task/params/delete/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.deleteParamsTask<br>
     */
    public boolean deleteParamsTask(final String id) throws IOException {
        final AcknowlegementType ack = client.delete("task/params/delete/" + id, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return "Success".equals(ack.getStatus());
    }

    /**
     * path : /1/task/params/duplicate/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.duplicateParamsTask<br>
     */
    public void duplicateParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/execute/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.executeParamsTask<br>
     */
    public void executeParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/schedule/start/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.startScheduleParamsTask<br>
     */
    public void startScheduleParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/schedule/stop/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.stopScheduleParamsTask<br>
     */
    public void stopScheduleParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/list<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listParamsTask<br>
     */
    public void listParamsTask(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/params/list/{type}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listParamsTaskByType<br>
     */
    public void listParamsTaskByType(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/listProcesses<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listProcess<br>
     */
    public Collection<String> listProcess() throws HttpResponseException, IOException {
        String path = "task/listProcesses";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class).getList();
    }

    /**
     * path : /1/task/countProcesses<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.countAvailableProcess<br>
     */
    public void countAvailableProcess(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/process/factory/{authorityCode}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listProcessForFactory<br>
     */
    public Collection<String> listProcessForFactory(final String authorityCode) throws HttpResponseException, IOException {
        String path = "task/process/factory/" + authorityCode;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class).getList();
    }


    /**
     * path : /1/task//list/datasetRef<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.getDatasetProcessReferenceList<br>
     */
    public void getDatasetProcessReferenceList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task//list/serviceRef/domain/{domainId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.getServiceProcessReferenceList<br>
     */
    public void getServiceProcessReferenceList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task//list/styleRef<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.getStyleProcessReferenceList<br>
     */
    public void getStyleProcessReferenceList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task//list/userRef<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.getUserProcessReferenceList<br>
     */
    public void getUserProcessReferenceList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/listTasks<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listTasks<br>
     */
    public Map<String, TaskStatusDTO> listTasks() throws HttpResponseException, IOException {
        String path = "task/listTasks";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Map.class);
    }

    /**
     * path : /1/task/listRunningTasks/{id}/{limit}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listRunningTaskForTaskParameter<br>
     */
    public void listRunningTaskForTaskParameter(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/taskHistory/{id}/{limit}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.listHistoryForTaskParameter<br>
     */
    public void listHistoryForTaskParameter(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/task/process/descriptor/{authority}/{code}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.TaskRest.getProcessDescriptor<br>
     */
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

}
