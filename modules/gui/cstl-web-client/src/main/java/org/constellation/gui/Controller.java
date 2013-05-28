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
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.constellation.gui.model.AccessConstraint;
import org.constellation.gui.model.Contact;
import org.constellation.gui.model.Service;
import org.constellation.gui.service.ServicesManager;

import javax.inject.Inject;
import java.io.IOException;
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
    protected ResourceBundle bundle;

    @View
    @Route("/")
    public void index() {
        index.render();
    }

    @View
    @Route("/webservices")
    public void webservices() {
        webServices.render();
    }

    @Action
    @Route("/wms/list")
    public Response createService(Service createdService, Contact serviceContact, AccessConstraint serviceConstraint) throws IOException {
        createdService.setServiceConstraints(serviceConstraint);
        createdService.setServiceContact(serviceContact);
        servicesManager.createServices(createdService);
        return Controller_.index();
    }
}
