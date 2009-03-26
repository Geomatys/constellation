/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.ws.embedded;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;
import javax.xml.ws.Endpoint;

import org.geotools.console.CommandLine;
import org.geotools.console.Option;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

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
 *     ServiceInstanceSOAP = org.constellation.sos.ws.soap.SOSService;
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
	private static final Logger LOGGER = Logger.getLogger("org.constellation.ws.embedded");

	// THESE ARE INJECTED BY THE CommandLine CLASS
	//   TODO: these default values clobber main's args; fixed in Geotidy
	@Option
	protected Boolean useFacadeREST = true;       //Default value
	@Option
	protected String  host          = "localhost";//Default value
	@Option
	protected Integer port          = 9090;       //Default value
    @Option
    protected Integer duration      = 20*60*1000; //minutes*seconds*millseconds; set to <=0 to last until <enter>
	
    
	final URI uri;
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
	protected Map<String,String> GrizzlyWebContainerProperties = new HashMap<String,String>();
	
	
	//FOR SOAP, DEFINE THIS REFERENCE:
	protected Object ServiceInstanceSOAP = null;
	
	
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
	
	
	
	/**
	 * Constructor which passes the arguments for processing to the 
	 * CommandLine parent and sets the URI.
	 * <p>
	 * Extending classes using the REST facade should 
	 */
	protected CstlEmbeddedService(String[] args){
		
		super(args);
		
		String base = "http://" + host + "/";
    	uri = UriBuilder.fromUri(base).port(port).build();
		
	}
	
	/**
	 * Should be called by the {@code main()} method for any web service wishing
	 * to implement a JAX-RS REST facade.
	 */
	protected void runREST() {
		
//        GrizzlyWebContainerProperties.put("com.sun.jersey.config.property.resourceConfigClass",
//        		                          "com.sun.jersey.api.core.PackagesResourceConfig");
        
        System.out.println("Starting grizzly server at: " + f.format(new Date()));

        SelectorThread threadSelector = null;
        try {
            if (GrizzlyWebContainerProperties.isEmpty()) {
                threadSelector = GrizzlyWebContainerFactory.create(uri);
            } else {
                threadSelector = GrizzlyWebContainerFactory.create(uri, GrizzlyWebContainerProperties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Started Grizzly application server for: " + uri);
        System.out.println("The service definition file can be found at: " + uri + "application.wadl");

        stayAlive();

        threadSelector.stopEndpoint();
        System.out.println("*Stopped grizzly server at: " + f.format(new Date()) + ".");
	}
	
	/**
	 * Should be called by the {@code main()} method for any web service wishing
	 * to implement a JAX-WS SOAP facade.
	 */
	protected void runSOAP() {
		
		if ( null == ServiceInstanceSOAP){
			System.out.println("The Service Endpoint Instance was never defined.");
			System.exit(0);
		}


        System.out.println("Starting jax-ws server at: " + f.format(new Date()));

        String service = uri.toString() + "pep";
        Endpoint ep = null;
        ep = Endpoint.publish(service, ServiceInstanceSOAP);
//        try {
//            ep = Endpoint.publish(service, ServiceInstanceSOAP);
//        } catch (JAXBException e) {
//            e.printStackTrace();
//        }

        System.out.println("Started jax-ws application server for: " + service);
        System.out.println("The service definition file can be found at: " + service + "?wsdl");

        stayAlive();

        ep.stop();
        System.out.println("*Stopped jax-ws server at: " + f.format(new Date()) + ".");
	}
	/**
	 * Will keep either of the services alive either for the number of 
	 * milliseconds defined in the command-line parameter {@code duration} if 
	 * that value is greater than zero or until the user presses the 
	 * {@code <ENTER>} (or {@code <RETURN>}) key of the keyboard depending if 
	 * the duration time is greater than zero.
	 * 
	 */
	protected void stayAlive() {
		
		if (duration>0) {
	        //Survive 'duration' milliseconds
	        System.out.println("  Service will stop in " + duration / (60 * 1000) + " minutes.");
	        try {
	            Thread.sleep(duration);
	        } catch (InterruptedException iex) {
	            LOGGER.fine("The grizzly thread has received an interrupted request.");
                System.out.println("*Stopped grizzly server at: " + f.format(new Date()) + ".");
	        }
		} else {
			//Listen and wait for <ENTER>
			System.out.println("  Hit <ENTER> to stop the service.");
	        try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    public static void main(String[] args) {
        new CstlEmbeddedService(args).runREST();
    }
}
