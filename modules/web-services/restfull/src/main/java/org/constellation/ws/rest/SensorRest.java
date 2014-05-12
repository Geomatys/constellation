/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.ws.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.SensorRecord;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SensorMLTree;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("/1/sensor/")
public class SensorRest {
    private static final Logger LOGGER = Logging.getLogger(SensorRest.class);
    
    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorList() {
        final List<SensorMLTree> values = new ArrayList<>();
        final List<SensorRecord> sensors = ConfigurationEngine.getSensors();
        for (final SensorRecord sensor : sensors) {
            final SensorMLTree t = new SensorMLTree(sensor.getIdentifier(), sensor.getType());
            final List<SensorMLTree> children = new ArrayList<>();
            final List<SensorRecord> records = ConfigurationEngine.getSensorChildren(sensor.getIdentifier());
            for (SensorRecord record : records) {
                children.add(new SensorMLTree(record.getIdentifier(), record.getType()));
            }
            t.setChildren(children);
            values.add(t);
        }
        final SensorMLTree result = SensorMLTree.buildTree(values);
        return Response.ok(result).build();
    }
    
    @PUT
    @Path("add")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response importSensor(final ParameterValues pv) {
        final String path = pv.get("path");
        // TODO add to database
        return Response.ok(null).build();
    }
}
