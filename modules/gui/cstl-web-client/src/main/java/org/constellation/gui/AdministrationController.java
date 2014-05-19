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
package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.constellation.dto.Database;
import org.constellation.gui.service.ProviderManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * main adminstration part controller
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class AdministrationController {


    @Inject
    @Path("administration.gtmpl")
    Template administration;

    @Inject
    @Path("database.gtmpl")
    Template databasePage;

    @Inject
    private ProviderManager providerManager;

    @View
    @Route("/administration")
    public Response getAdministration(){
        return administration.ok().withMimeType("text/html");
    }

    @View
    @Route("/database/{type}")
    public Response getDatabase(final String type, Database database){
        if(database==null){
        }

        Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("type", type);
        parameters.put("database", database);
        return databasePage.with(parameters).ok().withMimeType("text/html");
    }

    @Action
    @Route("/update/database")
    public Response saveDatabase(String databaseType, Database database){
        switch (databaseType){
            case "administration":
                //TODO update database connection on constellation server
                break;
            default:
                providerManager.createProvider(database.getType(), "defaultDatabase", null, database.getProviderType(), database, "pgrasters");

        }
        return AdministrationController_.getDatabase(databaseType, database);
    }
}
