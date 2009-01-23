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

package org.constellation.map.ws;

import org.constellation.ws.CstlEmbeddedService;

/**
 * A WMS instance which can be run with:
 * <pre>
 *     java -jar cstl-web-wms-0.3-SNAPSHOT.jar -port=9090 -duration=-1
 * </pre>
 * see the help for details.
 * <pre>
 *     java -jar cstl-web-wms-0.3-SNAPSHOT.jar -help
 * </pre>
 * @author Adrian Custer
 * @since 0.3
 *
 */
public class WMS extends CstlEmbeddedService {

	/**
	 * Constructor, calls through to the parent class.
	 * @param args
	 */
	public WMS(String[] args) {
		super(args);
		GrizzlyWebContainerProperties.put("com.sun.jersey.config.property.packages", "org.constellation.map.ws.rs");
	}

	/**
	 * The main method. 
	 * 
	 * @param args Command-line parameters.
	 */
	public static void main(String[] args) {
		
		new WMS(args).runREST();
		
		System.exit(0);

	}

}
