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
package org.constellation.ws.embedded;

import org.apache.sis.util.logging.Logging;
import org.constellation.ws.rs.CstlApplication;
import org.constellation.ws.rs.jackson.JacksonFeature;
import org.geotoolkit.console.CommandLine;
import org.geotoolkit.console.Option;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.env.AbstractEnvironment;

/**
 * An Abstract class to run the web service in an embedded container.
 * <p>
 * For now, the services are run only using the Grizzly web server. In the
 * future, we may look into working on the embedded Glassfish system.
 * </p>
 * <p>
 * Classes wishing to run an embedded service should extend this class. The
 * responsibilities of each extending class is to:
 * <ol>
 *   <li>Call this constructor with the arguments of the main method.
 * <pre>
 *     super(args);
 * </pre>
 *   </li>
 *   <li>For JAX-RS, REST based services, add parameters to the configuration
 *       {@code Map}.
 * <pre>
 *     map.put("com.sun.jersey.config.property.packages", "org.constellation.map.ws.rs");
 *     map.put("com.sun.jersey.config.property.packages", "org.constellation.coverage.ws.rs");
 *     map.put(TODO: get providers line from Cedric);
 * </pre>
 *   </li>
 *   <li>For JAX-WS, SOAP based services, set the reference of the Service
 *       Endpoint Interface.
 * <pre>
 *     serviceInstanceSOAP = new org.constellation.sos.ws.soap.SOSService();
 * </pre>
 *   </li>
 *   <li>Add a simple main, such as:
 * <pre>
 *    public static void main(String[] args) {
 *
 *        EXTENDOR sei = new EXTENDOR(args);
 *        if (useFacadeREST){
 *            sei.runREST();
 *        } else {
 *            sei.runSOAP();
 *        }
 *
 *        System.exit(0);
 *
 *    }
 * </pre>
 *   </li>
 * </ol>
 * </p>
 *
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public class CstlEmbeddedService extends CommandLine {

    /**
     * Logger for this service.
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");
    // THESE ARE INJECTED BY THE CommandLine CLASS
    //   TODO: these default values clobber main's args; fixed in Geotidy
    @Option
    protected Boolean useFacadeREST = true;       //Default value
    @Option
    protected String host = "localhost";//Default value
    @Option
    protected Integer port = 9090;       //Default value
    @Option
    protected Integer portsoap = 9191;
    @Option
    public Integer duration = 20 * 60 * 1000; //minutes*seconds*millseconds; set to <=0 to last until <enter>

    public boolean findAvailablePort = false;

    public Integer currentPort;

    public boolean ready = false;
    
    private URI uri;
    public String uriSuffix;
    final URI uriSoap;
    final DateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    /* ***********************************************************************
     *   CONCRETE CLASSES MUST: (see below)
     * ********************************************************************** */
    //FOR REST, COMPLETE THIS PARAMETER MAP
    //  Grizzly is used by the REST service. Extending classes need to add the
    //  package(s) containing the service(s) they desire to run and the
    //  providers on which those services depend, e.g. :
    //      map.put("com.sun.jersey.config.property.packages", "org.constellation.map.ws.rs");
    //      map.put("com.sun.jersey.config.property.packages", "org.constellation.coverage.ws.rs");
    //      map.put(TODO: get providers line from Cedric);
    //  below we add in one of the properties needed by all services.
    protected Map<String, Object> grizzlyWebContainerProperties = new HashMap<>();
    //FOR SOAP, DEFINE THIS REFERENCE:
    public final Map<String, Object> serviceInstanceSOAP = new HashMap<>();

    
    private HttpServer threadSelector;
    
    final List<Endpoint> eps = new ArrayList<>();
    
    //INCLUDE THIS MAIN.
//	public static void main(String[] args) {
//
//		EXTENDOR sei = new EXTENDOR(args);
//	    if (useFacadeREST){
//		    sei.runREST();
//      } else {
//          sei.runSOAP();
//      }
//
//		System.exit(0);
//
//	}

    /* ***********************************************************************
     *   CONCRETE CLASSES MUST HAVE: (see above)
     * ********************************************************************** */

    public CstlEmbeddedService(final String[] args) {
        this(args, (String)null);
    }
    /**
     * Constructor which passes the arguments for processing to the
     * CommandLine parent and sets the URI.
     * By default, only start the WMS and WCS services.
     * <p>
     * Extending classes using the REST facade should
     */
    public CstlEmbeddedService(final String[] args, String uriSuffix) {
        this(args, new String[] {
            "org.constellation.rest.api",
            "org.constellation.map.ws.rs",
            "org.constellation.coverage.ws.rs",
            "org.constellation.wfs.ws.rs",
            "org.constellation.wps.ws.rs",
            "org.constellation.sos.ws.rs",
            "org.constellation.sos.ws.rs.provider",
            "org.constellation.configuration.ws.rs",
            "org.constellation.metadata.ws.rs",
            "org.constellation.metadata.ws.rs.provider",
            "org.constellation.ws.rs.provider"
        }, uriSuffix);
    }

    public CstlEmbeddedService(final int port, final String[] args, final String[] providerPackages) {
        this(args, providerPackages);
        this.port = port;
        final String base = "http://" + host + "/";
        this.uri =  UriBuilder.fromUri(base).port(port).build();
    }

    public CstlEmbeddedService(final String[] args, final String[] providerPackages) {
        this(args, providerPackages, null);
    }
    /**
     * Constructor which passes the arguments for processing to the
     * CommandLine parent and sets the URI.
     * <p>
     * Extending classes using the REST facade should
     *
     * @param args The command line arguments.
     * @param providerPackages The packages for providers to start.
     * @param uriSuffix
     */
    public CstlEmbeddedService(final String[] args, final String[] providerPackages, final String uriSuffix) {
        super(null, args);

        final StringBuilder sb = new StringBuilder();
        final int length = providerPackages.length;
        for (int i=0; i<length; i++) {
            final String pack = providerPackages[i];
            sb.append(pack);
            if (i != length - 1) {
                sb.append(';');
            }
        }
        grizzlyWebContainerProperties.put(ServerProperties.PROVIDER_PACKAGES, sb.toString());

        this.uriSuffix = uriSuffix;
        
        final String base;
        if (uriSuffix == null) {
            base = "http://" + host + "/";
        } else {
            base = "http://" + host + "/" + uriSuffix + "/";
        }
        uri = UriBuilder.fromUri(base).port(port).build();
        uriSoap = UriBuilder.fromUri(base).port(portsoap).build();

    }

    private HttpServer buildThreadSelector() {
        HttpServer threadSelector = null;
        final String base         = "http://" + host + "/";
        URI currentUri            = uri;
        currentPort               = port;
        boolean working           = false;
        while (!working) {
            working = true;
            try {
                final ResourceConfig config = ResourceConfig.forApplication(new CstlApplication());
                System.setProperty(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME, "standard,derby");
                System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "standard,derby");
//                grizzlyWebContainerProperties.put(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME, "standard");
//                grizzlyWebContainerProperties.put(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "standard");
                config.addProperties(grizzlyWebContainerProperties);
                config.register(JacksonFeature.class);
                config.register(MultiPartFeature.class);
                config.register(RolesAllowedDynamicFeature.class);
                //ApplicationHandler handler = new ApplicationHandler(config);
                
                threadSelector = GrizzlyHttpServerFactory.createHttpServer(currentUri, config, true);
                
                
                
            } catch (ProcessingException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                if (findAvailablePort) {
                    working = false;
                    currentPort++;
                    currentUri = UriBuilder.fromUri(base).port(currentPort).build();
                }
            }
        }
        uri = currentUri;
        return threadSelector;
    }

    /**
     * Should be called by the {@code main()} method for any web service wishing
     * to implement a JAX-RS REST facade.
     */
    public void runREST() {

        grizzlyWebContainerProperties.put("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.PackagesResourceConfig");

        LOGGER.log(Level.INFO, "Starting grizzly server at: {0}", f.format(new Date()));

        threadSelector = buildThreadSelector();

        ready = true;
        LOGGER.log(Level.INFO, "Started Grizzly application server for: {0}", uri);
        LOGGER.log(Level.INFO, "The service definition file can be found at: {0}application.wadl", uri);

        stayAlive();
        shutdown();
        LOGGER.log(Level.INFO, "*Stopped grizzly server at: {0}.", f.format(new Date()));
    }

    /**
     * Should be called by the {@code main()} method for any web service wishing
     * to implement a JAX-WS SOAP facade.
     */
    public void runSOAP() {

        if (serviceInstanceSOAP.isEmpty()) {
            LOGGER.info("The SOAP Service Endpoint Instance was never defined.");
            System.exit(0);
        }

        LOGGER.log(Level.INFO, "Starting jax-ws server at: {0}", f.format(new Date()));

        eps.clear();
        for (Entry<String, Object> instance : serviceInstanceSOAP.entrySet()) {
            final String service = uriSoap.toString() + instance.getKey();
            Endpoint ep =  Endpoint.create(instance.getValue());
            ep.publish(service);
            eps.add(ep);
            LOGGER.log(Level.INFO, "Started jax-ws application server for: {0}", service);
            LOGGER.log(Level.INFO, "The service definition file can be found at: {0}?wsdl", service);
        }
        ready = true;

        stayAlive();
        shutdown();
        LOGGER.log(Level.INFO, "*Stopped jax-ws server at: {0}.", f.format(new Date()));
    }

    /**
     * Should be called by the {@code main()} method for any web service wishing
     * to implement a JAX-RS REST facade.
     */
    public void runAll() {

        /*grizzlyWebContainerProperties.put("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.PackagesResourceConfig");*/

        LOGGER.log(Level.INFO, "Starting grizzly server at: {0}", f.format(new Date()));

        threadSelector = buildThreadSelector();

        LOGGER.log(Level.INFO, "Started Grizzly application server for: {0}", uri);
        LOGGER.log(Level.INFO, "The service definition file can be found at: {0}application.wadl", uri);

        eps.clear();
        for (Entry<String, Object> instance : serviceInstanceSOAP.entrySet()) {
            final String service = uriSoap.toString() + instance.getKey();
            final Endpoint ep =  Endpoint.create(instance.getValue());
            ep.publish(service);
            eps.add(ep);

            LOGGER.log(Level.INFO, "Started jax-ws application server for: {0}", service);
            LOGGER.log(Level.INFO, "The service definition file can be found at: {0}?wsdl", service);
        }
        ready = true;

        stayAlive();
        LOGGER.log(Level.INFO, "*grizzly shutdown in progress");
        shutdown();
        LOGGER.log(Level.INFO, "*Stopped grizzly server at: {0}.", f.format(new Date()));
    }

    /**
     * Will keep either of the services alive either for the number of
     * milliseconds defined in the command-line parameter {@code duration} if
     * that value is greater than zero or until the user presses the
     * {@code <ENTER>} (or {@code <RETURN>}) key of the keyboard depending if
     * the duration time is greater than zero.
     *
     */
    public void stayAlive() {

        if (duration > 0) {
            //Survive 'duration' milliseconds
            LOGGER.log(Level.INFO, "  Service will stop in {0} minutes.", duration / (60 * 1000));
            try {
                Thread.sleep(duration);
            } catch (InterruptedException iex) {
                LOGGER.fine("The grizzly thread has received an interrupted request.");
                LOGGER.log(Level.INFO, "*Interrupted grizzly server at: {0}.", f.format(new Date()));
            }
        } else {
            //Listen and wait for <ENTER>
            LOGGER.info("  Hit <ENTER> to stop the service.");
            try {
                System.in.read();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) {
        new CstlEmbeddedService(args).runSOAP();
    }
    
    public void shutdown() {
        LOGGER.log(Level.INFO, "*grizzly shutdown in progress");
        if (threadSelector != null) {
            threadSelector.shutdownNow();
        }
        for (Endpoint ep : eps) {
            ep.stop();
        }
    }
}
