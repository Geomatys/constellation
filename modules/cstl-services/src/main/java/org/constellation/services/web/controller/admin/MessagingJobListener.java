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
package org.constellation.services.web.controller.admin;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.constellation.admin.dto.TaskStatusDTO;
import org.constellation.sos.io.om2.OM2ResultEventDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Quartz job listener that register a geotk process listener each time the job is executed.
 * And send messages on websocket "/topic/taskevents*" topic.
 *
 * @author Quentin Boileau (Geomatys)
 */
@Named
@Singleton
public class MessagingJobListener  {

    @Inject
    private SimpMessagingTemplate template;

    @Inject
    private EventBus eventBus;

    @PostConstruct
    private void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onBusEvent(TaskStatusDTO taskStatus) {
        template.convertAndSend("/topic/taskevents", taskStatus);
        template.convertAndSend("/topic/taskevents/"+taskStatus.getTaskId(), taskStatus);
    }


    @Subscribe
    public void onBusEvent(OM2ResultEventDTO sosResult) {
        template.convertAndSend("/topic/sosevents/"+sosResult.getProcedureID(), sosResult);
    }
}
