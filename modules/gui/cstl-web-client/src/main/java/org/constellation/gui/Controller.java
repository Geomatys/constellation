/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.PropertyType;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.constellation.gui.service.ServicesManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller {

    @Inject
    protected ServicesManager servicesManager;

    @Inject
    @Path("index.gtmpl")
    protected Template index;

    @Inject
    @Path("webservices.gtmpl")
    protected Template webServices;

    @Inject
    @Path("wmssuccess.gtmpl")
    org.constellation.gui.templates.wmssuccess success;

    @Inject
    protected ResourceBundle bundle;

    @View
    @Route("/")
    public Response index() {
        return index.ok().withMimeType("text/html");
    }

    @View
    @Route("/webservices")
    public Response  webservices() {
        return webServices.ok().withMimeType("text/html");
    }

    @Action
    @Route("/wms/success")
    public Response createWMSService(Service createdService, Contact serviceContact, AccessConstraint serviceConstraint,
                                     String v111, String v130, String keywords) throws IOException {

        List<String> versionList = new ArrayList<String>(0);
        if (v111 != null) {
            versionList.add(v111);
        }
        if (v130 != null) {
            versionList.add(v130);
        }
        createdService.setVersions(versionList);

        String[] keywordsArray = keywords.split(" ");
        List<String> keywordsList = Arrays.asList(keywordsArray);
        createdService.setKeywords(keywordsList);

        createdService.setServiceConstraints(serviceConstraint);
        createdService.setServiceContact(serviceContact);
        boolean created = servicesManager.createServices(createdService, "WMS");
        System.out.println(created);
        return Controller_.succeded(createdService, "WMS", versionList, created + "");
    }

    @View
    @Route("/succeded")
    public Response succeded(Service createdService, String type, List<String> versionList, String created){
        Boolean create = Boolean.parseBoolean(created);
        return success.with().service(createdService).type(type).versions(versionList).created(create).ok().withMimeType("text/html");
    }
}
