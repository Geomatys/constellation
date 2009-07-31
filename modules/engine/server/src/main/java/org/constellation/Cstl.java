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

package org.constellation;

import org.constellation.portrayal.PortrayalServiceIF;
import org.constellation.portrayal.internal.CstlPortrayalService;
import org.constellation.register.PrimitiveRegisterIF;
import org.constellation.register.internal.PrimitiveRegister;

/**
 * The root class of the Constellation server Engine, this class provides the
 * static fields and methods used by services.
 * <p>
 * <b>TODO:<b> This will obviously evolve to include dynamic registration; for 
 * now, we are merely trying to get the system functional.
 * </p>
 * 
 * @author Adrian Custer
 * @since 0.3
 *
 */
public final class Cstl {
	
	//We don't want any instances of this class.
	private Cstl(){}
	
	/**
	 * Provides access to the interface through which to interact with the 
	 * local register; the interface is currently experimental.
	 * 
	 * @return An implementation of the primitive {@link PrimitiveRegisterIF 
	 *           Register Interface}.
	 */
	public static PrimitiveRegisterIF getRegister() {
		return PrimitiveRegister.internalGetInstance();
	}
	
	/**
	 * Provides access to the portrayal service through which internal resources 
	 * can be rendered onto a two dimensional image.
	 * 
	 * @return An implementation of the {@link PortrayalSerivceIF Portrayal 
	 *           Service Interface}.
	 */
	public static PortrayalServiceIF getPortrayalService() {
		return CstlPortrayalService.internalGetInstance();
	}
	
}
