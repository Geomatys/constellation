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
