/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2008, Geomatys
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

package org.constellation.security;

/**
 * The fixed set of roles against which the security decisions are performed.
 * <p>
 * Note that to secure a different service, 
 * @author Adrian Custer (Geomatys
 * @since 0.3
 *
 */
public enum ROLE {
	PUBLIC("Public"),
	USER("BasicUser"),
	ADVANCED("AdvancedUser"),
	ADMIN("Administrator");
	
	private final String role;
	
	private ROLE(String role){
		this.role = role;
	}

    @Override
	public String toString(){
		return role;
	}
}
