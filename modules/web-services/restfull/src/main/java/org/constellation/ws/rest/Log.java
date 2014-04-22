/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.constellation.configuration.ConfigDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogByService exposition.
 * 
 * @author olivier.nouguier@geomatys.com
 * 
 */
@Path("/1/log/")
public class Log {

    /**
     * File buffer size.
     */
    private static final int BUFFER_1024 = 1024;

    /**
     * Size of log file to read.
     */
    private static final int DEFAULT_LIMIT_4096 = 4096;

    /**
     * Slf4j logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Return a stream of last log (
     * 
     * @param serviceType
     * @param serviceId
     * @param offset
     * @param limit
     * @return
     */
    @GET
    @Path("{serviceType}/{serviceId}")
    @Produces({ MediaType.TEXT_PLAIN })
    @Consumes({ MediaType.APPLICATION_JSON })
    public StreamingOutput getLogByService(@PathParam("serviceType") final String serviceType,
            @PathParam("serviceId") final String serviceId, @QueryParam("o") final Integer offset,
            @QueryParam("l") final Integer limit) {

        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                java.nio.file.Path logfileRelative = Paths.get("logs", "cstl", serviceType, serviceId + "-service.log");
                java.nio.file.Path logFile = ConfigDirectory.getConfigDirectory().toPath().resolve(logfileRelative);
                if (Files.exists(logFile)) {
                    int toread = limit == null ? DEFAULT_LIMIT_4096 : limit;
                    if (toread < BUFFER_1024)
                        toread = 1024;
                    try (FileInputStream fileInputStream = new FileInputStream(logFile.toFile())) {

                        try (FileChannel fc = (FileChannel.open(logFile))) {
                            int nread;

                            long length = fc.size();
                            if (offset != null) {
                                if (offset > 0 && offset < length)
                                    fc.position(offset);
                            } else if (length > toread) {
                                fc.position(length - toread - 1);
                            }
                            ByteBuffer copy = ByteBuffer.allocate(1024);
                            do {
                                nread = fc.read(copy);
                                toread -= nread;
                                if (nread > 0 && copy.hasArray())
                                    output.write(copy.array(), 0, nread);
                                copy.rewind();
                            } while (nread > 0 && toread > 0);

                        } catch (IOException e) {
                            LOGGER.error("I/O Exception while reading: " + logfileRelative.toString(), e);
                        }

                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                        output.write(("No log file for " + serviceType + ", " + serviceId).getBytes());
                    }
                } else {
                    LOGGER.warn("No log file for " + serviceType + ", " + serviceId);
                    output.write(("No log file for " + serviceType + ", " + serviceId).getBytes());
                }
            }

        };
    }
}
